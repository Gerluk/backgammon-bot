package org.example.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameEngineTest {

    private final GameEngine engine = new GameEngine();

    @Test
    void winnerOrNullReturnsNullWhenNobodyWon() {
        assertNull(engine.winnerOrNull(Board.initialSetup()));
    }

    @Test
    void winnerOrNullWhiteWhenWhiteBoreOffFifteen() {
        Board board = Board.customPosition(new int[Board.NUM_POINTS], 0, 0, 15, 0);
        assertEquals(Player.WHITE, engine.winnerOrNull(board));
    }

    @Test
    void winnerOrNullBlackWhenBlackBoreOffFifteen() {
        Board board = Board.customPosition(new int[Board.NUM_POINTS], 0, 0, 0, 15);
        assertEquals(Player.BLACK, engine.winnerOrNull(board));
    }

    @Test
    void winnerOrNullReturnsNullWhenBothCloseButNeitherComplete() {
        Board board = Board.customPosition(new int[Board.NUM_POINTS], 0, 0, 14, 14);
        assertNull(engine.winnerOrNull(board));
    }

    @Test
    void legalMovesForStepInitialPositionWhiteWithDieSix() {
        GameState state = new GameState(Board.initialSetup(), Player.WHITE, List.of(6));

        List<Move> moves = engine.legalMovesForStep(state, 6);

        assertTrue(moves.contains(new Move(23, 17)));
        assertTrue(moves.contains(new Move(12, 6)));
        assertTrue(moves.contains(new Move(7, 1)));
        assertEquals(3, moves.size());
    }

    @Test
    void legalMovesForStepInitialPositionBlackWithDieSix() {
        GameState state = new GameState(Board.initialSetup(), Player.BLACK, List.of(6));

        List<Move> moves = engine.legalMovesForStep(state, 6);

        assertTrue(moves.contains(new Move(0, 6)));
        assertTrue(moves.contains(new Move(11, 17)));
        assertTrue(moves.contains(new Move(16, 22)));
        assertEquals(3, moves.size());
    }

    @Test
    void legalMovesForStepNoOwnCheckersReturnsEmptyList() {
        GameState state = new GameState(Board.empty(), Player.WHITE, List.of(4));
        assertTrue(engine.legalMovesForStep(state, 4).isEmpty());
    }

    @Test
    void legalMovesForStepRespectsBlockedPoints() {
        int[] points = new int[Board.NUM_POINTS];
        points[10] = 1;
        points[5] = -2;
        GameState state = new GameState(Board.customPosition(points, 0, 0, 0, 0), Player.WHITE, List.of(5));

        assertTrue(engine.legalMovesForStep(state, 5).isEmpty());
    }

    @Test
    void legalMovesForStepAllowsMoveToOwnOccupiedPoint() {
        int[] points = new int[Board.NUM_POINTS];
        points[10] = 1;
        points[5] = 3;
        GameState state = new GameState(Board.customPosition(points, 0, 0, 0, 0), Player.WHITE, List.of(5));

        List<Move> moves = engine.legalMovesForStep(state, 5);
        assertEquals(2, moves.size());
        assertTrue(moves.contains(new Move(10, 5)));
    }

    @Test
    void legalMovesForStepAllowsHittingBlot() {
        int[] points = new int[Board.NUM_POINTS];
        points[10] = 1;
        points[5] = -1;
        GameState state = new GameState(Board.customPosition(points, 0, 0, 0, 0), Player.WHITE, List.of(5));

        List<Move> moves = engine.legalMovesForStep(state, 5);
        assertEquals(1, moves.size());
        assertEquals(new Move(10, 5), moves.getFirst());
    }

    @Test
    void legalMovesForStepMultipleCheckersOnSamePointProducesSingleMoveOption() {
        int[] points = new int[Board.NUM_POINTS];
        points[10] = 3;
        GameState state = new GameState(Board.customPosition(points, 0, 0, 0, 0), Player.WHITE, List.of(4));

        List<Move> moves = engine.legalMovesForStep(state, 4);
        assertEquals(1, moves.size());
    }

    @Test
    void legalMovesForStepDieValueOneMovesSinglePoint() {
        int[] points = new int[Board.NUM_POINTS];
        points[10] = 1;
        GameState state = new GameState(Board.customPosition(points, 0, 0, 0, 0), Player.WHITE, List.of(1));

        List<Move> moves = engine.legalMovesForStep(state, 1);
        assertEquals(new Move(10, 9), moves.getFirst());
    }

    @Test
    void legalMovesForStepWhiteNeverMovesBackward() {
        int[] points = new int[Board.NUM_POINTS];
        points[10] = 1;
        points[15] = 1;
        GameState state = new GameState(Board.customPosition(points, 0, 0, 0, 0), Player.WHITE, List.of(4));

        List<Move> moves = engine.legalMovesForStep(state, 4);

        assertEquals(2, moves.size());
        assertTrue(moves.stream().allMatch(m -> m.to() < m.from()));
    }

    @Test
    void legalMovesForStepBlackNeverMovesBackward() {
        int[] points = new int[Board.NUM_POINTS];
        points[10] = -1;
        points[5] = -1;
        GameState state = new GameState(Board.customPosition(points, 0, 0, 0, 0), Player.BLACK, List.of(4));

        List<Move> moves = engine.legalMovesForStep(state, 4);

        assertEquals(2, moves.size());
        assertTrue(moves.stream().allMatch(m -> m.to() > m.from()));
    }

    @Test
    void legalMovesForStepEntryFromBarWhiteAlwaysEntersInOpponentsHome() {
        Board board = Board.customPosition(new int[Board.NUM_POINTS], 1, 0, 0, 0);
        for (int die = 1; die <= 6; die++) {
            GameState state = new GameState(board, Player.WHITE, List.of(die));
            List<Move> moves = engine.legalMovesForStep(state, die);
            assertEquals(1, moves.size());
            assertEquals(24 - die, moves.getFirst().to());
        }
    }

    @Test
    void legalMovesForStepEntryFromBarBlackAlwaysEntersInOpponentsHome() {
        Board board = Board.customPosition(new int[Board.NUM_POINTS], 0, 1, 0, 0);
        for (int die = 1; die <= 6; die++) {
            GameState state = new GameState(board, Player.BLACK, List.of(die));
            List<Move> moves = engine.legalMovesForStep(state, die);
            assertEquals(1, moves.size());
            assertEquals(die - 1, moves.getFirst().to());
        }
    }

    @Test
    void legalMovesForStepPlayerWithCheckerOnBarMustEnterFirst() {
        int[] points = new int[Board.NUM_POINTS];
        points[10] = 1;
        GameState state = new GameState(Board.customPosition(points, 1, 0, 0, 0), Player.WHITE, List.of(3));

        List<Move> moves = engine.legalMovesForStep(state, 3);

        assertEquals(1, moves.size());
        assertTrue(moves.getFirst().isEnteringFromBar());
    }

    @Test
    void legalMovesForStepEntryFromBarBlockedWhenEntryPointOccupiedByOpponent() {
        int[] points = new int[Board.NUM_POINTS];
        points[21] = -2;
        GameState state = new GameState(Board.customPosition(points, 1, 0, 0, 0), Player.WHITE, List.of(3));

        assertTrue(engine.legalMovesForStep(state, 3).isEmpty());
    }

    @Test
    void legalMovesForStepEntryFromBarCanHitBlot() {
        int[] points = new int[Board.NUM_POINTS];
        points[21] = -1;
        GameState state = new GameState(Board.customPosition(points, 1, 0, 0, 0), Player.WHITE, List.of(3));

        List<Move> moves = engine.legalMovesForStep(state, 3);
        assertEquals(1, moves.size());
        assertEquals(new Move(Move.BAR, 21), moves.getFirst());
    }

    @Test
    void legalMovesForStepMultipleCheckersOnBarOnlyOneEntryOption() {
        Board board = Board.customPosition(new int[Board.NUM_POINTS], 3, 0, 0, 0);
        GameState state = new GameState(board, Player.WHITE, List.of(4));

        List<Move> moves = engine.legalMovesForStep(state, 4);
        assertEquals(1, moves.size());
    }

    @Test
    void legalMovesForStepBarIgnoresAllOtherCheckersRegardlessOfCount() {
        int[] points = new int[Board.NUM_POINTS];
        points[10] = 5;
        points[15] = 3;
        points[20] = 2;
        GameState state = new GameState(Board.customPosition(points, 1, 0, 0, 0), Player.WHITE, List.of(4));

        List<Move> moves = engine.legalMovesForStep(state, 4);
        assertEquals(1, moves.size());
        assertTrue(moves.getFirst().isEnteringFromBar());
    }

    @Test
    void legalMovesForStepBearOffNotAllowedWhenCheckerOutsideHome() {
        int[] points = new int[Board.NUM_POINTS];
        points[5] = 1;
        points[10] = 1;
        GameState state = new GameState(Board.customPosition(points, 0, 0, 0, 0), Player.WHITE, List.of(6));

        List<Move> moves = engine.legalMovesForStep(state, 6);
        assertTrue(moves.stream().noneMatch(Move::isBearingOff));
    }

    @Test
    void legalMovesForStepBearOffNotAllowedWhenCheckerOnBar() {
        int[] points = new int[Board.NUM_POINTS];
        points[5] = 1;
        GameState state = new GameState(Board.customPosition(points, 1, 0, 0, 0), Player.WHITE, List.of(6));

        List<Move> moves = engine.legalMovesForStep(state, 6);
        assertTrue(moves.getFirst().isEnteringFromBar());
    }

    @Test
    void legalMovesForStepBearOffExactDieValue() {
        int[] points = new int[Board.NUM_POINTS];
        points[5] = 1;
        GameState state = new GameState(Board.customPosition(points, 0, 0, 0, 0), Player.WHITE, List.of(6));

        List<Move> moves = engine.legalMovesForStep(state, 6);
        assertEquals(1, moves.size());
        assertEquals(new Move(5, Move.OFF), moves.getFirst());
    }

    @Test
    void legalMovesForStepBearOffExactDieValueAllSixHomePoints() {
        for (int point = 0; point <= 5; point++) {
            int[] points = new int[Board.NUM_POINTS];
            points[point] = 1;
            int expectedDie = point + 1;
            GameState state = new GameState(Board.customPosition(points, 0, 0, 0, 0), Player.WHITE, List.of(expectedDie));

            List<Move> moves = engine.legalMovesForStep(state, expectedDie);
            assertEquals(new Move(point, Move.OFF), moves.getFirst());
        }
    }

    @Test
    void legalMovesForStepBearOffNoPieceOnExactPointPrefersOrdinaryMove() {
        int[] points = new int[Board.NUM_POINTS];
        points[5] = 1;
        GameState state = new GameState(Board.customPosition(points, 0, 0, 0, 0), Player.WHITE, List.of(4));

        List<Move> moves = engine.legalMovesForStep(state, 4);
        assertEquals(1, moves.size());
        assertEquals(new Move(5, 1), moves.getFirst());
    }

    @Test
    void legalMovesForStepBearOffOvershootAllowedWhenNoHigherChecker() {
        int[] points = new int[Board.NUM_POINTS];
        points[3] = 1;
        GameState state = new GameState(Board.customPosition(points, 0, 0, 0, 0), Player.WHITE, List.of(6));

        List<Move> moves = engine.legalMovesForStep(state, 6);
        assertEquals(1, moves.size());
        assertEquals(new Move(3, Move.OFF), moves.getFirst());
    }

    @Test
    void legalMovesForStepBearOffOvershootNotAllowedWhenHigherCheckerExists() {
        int[] points = new int[Board.NUM_POINTS];
        points[3] = 1;
        points[5] = 1;
        GameState state = new GameState(Board.customPosition(points, 0, 0, 0, 0), Player.WHITE, List.of(6));

        List<Move> moves = engine.legalMovesForStep(state, 6);
        assertEquals(1, moves.size());
        assertEquals(new Move(5, Move.OFF), moves.getFirst());
    }

    @Test
    void legalMovesForStepBearOffBlackPlayerExactDieValue() {
        int[] points = new int[Board.NUM_POINTS];
        points[18] = -1;
        GameState state = new GameState(Board.customPosition(points, 0, 0, 0, 0), Player.BLACK, List.of(6));

        List<Move> moves = engine.legalMovesForStep(state, 6);
        assertEquals(1, moves.size());
        assertEquals(new Move(18, Move.OFF), moves.getFirst());
    }

    @Test
    void legalMovesForStepBearOffBlackPlayerOvershootAllowed() {
        int[] points = new int[Board.NUM_POINTS];
        points[20] = -1;
        GameState state = new GameState(Board.customPosition(points, 0, 0, 0, 0), Player.BLACK, List.of(6));

        List<Move> moves = engine.legalMovesForStep(state, 6);
        assertEquals(1, moves.size());
        assertEquals(new Move(20, Move.OFF), moves.getFirst());
    }

    @Test
    void legalMovesForStepBothBearOffAndOrdinaryMoveOfDifferentCheckerCoexist() {
        int[] points = new int[Board.NUM_POINTS];
        points[3] = 1;
        points[5] = 1;
        GameState state = new GameState(Board.customPosition(points, 0, 0, 0, 0), Player.WHITE, List.of(4));

        List<Move> moves = engine.legalMovesForStep(state, 4);

        assertEquals(2, moves.size());
        assertTrue(moves.contains(new Move(3, Move.OFF)));
        assertTrue(moves.contains(new Move(5, 1)));
    }

    @Test
    void legalMovesForStepBearOffAllFifteenInHomeDieMatchesEmptyPointWithLowerCheckersOnly() {
        int[] points = new int[Board.NUM_POINTS];
        points[1] = 5;
        GameState state = new GameState(Board.customPosition(points, 0, 0, 0, 0), Player.WHITE, List.of(6));

        List<Move> moves = engine.legalMovesForStep(state, 6);
        assertEquals(1, moves.size());
        assertEquals(new Move(1, Move.OFF), moves.getFirst());
    }

    @Test
    void applyMoveOrdinaryMoveUpdatesBoardAndKeepsSamePlayerAndDice() {
        GameState state = new GameState(Board.initialSetup(), Player.WHITE, List.of(6, 4));

        GameState next = engine.applyMove(state, new Move(23, 17));

        assertEquals(1, next.board().checkersOf(Player.WHITE, 23));
        assertEquals(1, next.board().checkersOf(Player.WHITE, 17));
        assertEquals(Player.WHITE, next.currentPlayer());
        assertEquals(List.of(6, 4), next.remainingDice());
    }

    @Test
    void applyMoveDoesNotMutateOriginalState() {
        GameState state = new GameState(Board.initialSetup(), Player.WHITE, List.of(6, 4));

        engine.applyMove(state, new Move(23, 17));

        assertEquals(2, state.board().checkersOf(Player.WHITE, 23));
        assertEquals(0, state.board().checkersOf(Player.WHITE, 17));
    }

    @Test
    void applyMoveEntryFromBarUpdatesBoardCorrectly() {
        int[] points = new int[Board.NUM_POINTS];
        GameState state = new GameState(Board.customPosition(points, 1, 0, 0, 0), Player.WHITE, List.of(3));

        GameState next = engine.applyMove(state, new Move(Move.BAR, 2));

        assertEquals(0, next.board().barCount(Player.WHITE));
        assertEquals(1, next.board().checkersOf(Player.WHITE, 2));
    }

    @Test
    void applyMoveBearingOffUpdatesBoardCorrectly() {
        int[] points = new int[Board.NUM_POINTS];
        points[5] = 1;
        GameState state = new GameState(Board.customPosition(points, 0, 0, 0, 0), Player.WHITE, List.of(6));

        GameState next = engine.applyMove(state, new Move(5, Move.OFF));

        assertEquals(0, next.board().checkersOf(Player.WHITE, 5));
        assertEquals(1, next.board().borneOffCount(Player.WHITE));
    }

    @Test
    void applyMoveHittingBlotSendsOpponentToBar() {
        int[] points = new int[Board.NUM_POINTS];
        points[10] = 1;
        points[5] = -1;
        GameState state = new GameState(Board.customPosition(points, 0, 0, 0, 0), Player.WHITE, List.of(5));

        GameState next = engine.applyMove(state, new Move(10, 5));

        assertEquals(1, next.board().barCount(Player.BLACK));
        assertEquals(0, next.board().checkersOf(Player.BLACK, 5));
        assertEquals(1, next.board().checkersOf(Player.WHITE, 5));
    }

    @Test
    void applyMoveIllegalMoveThrowsException() {
        GameState state = new GameState(Board.empty(), Player.WHITE, List.of(4));
        assertThrows(IllegalStateException.class, () -> engine.applyMove(state, new Move(10, 6)));
    }

    @Test
    void legalFullTurnsBothDiceUsableProducesLengthTwoSequences() {
        GameState state = new GameState(Board.initialSetup(), Player.WHITE, List.of(6, 4));

        List<List<Move>> turns = engine.legalFullTurns(state);

        assertFalse(turns.isEmpty());
        assertTrue(turns.stream().allMatch(t -> t.size() == 2));
    }

    @Test
    void legalFullTurnsMustChooseOrderThatAllowsBothDice() {
        int[] points = new int[Board.NUM_POINTS];
        points[10] = 1;
        points[6] = -2;
        GameState state = new GameState(Board.customPosition(points, 0, 0, 0, 0), Player.WHITE, List.of(6, 4));

        List<List<Move>> turns = engine.legalFullTurns(state);

        assertEquals(1, turns.size());
        List<Move> onlyTurn = turns.getFirst();
        assertEquals(2, onlyTurn.size());
        assertEquals(new Move(10, 4), onlyTurn.getFirst());
        assertEquals(new Move(4, 0), onlyTurn.get(1));
    }

    @Test
    void legalFullTurnsOnlyOneCheckerMovableMustUseHigherDie() {
        int[] points = new int[Board.NUM_POINTS];
        points[20] = 1;
        points[10] = -2;
        GameState state = new GameState(Board.customPosition(points, 0, 0, 0, 0), Player.WHITE, List.of(6, 4));

        List<List<Move>> turns = engine.legalFullTurns(state);

        assertEquals(1, turns.size());
        List<Move> onlyTurn = turns.getFirst();
        assertEquals(1, onlyTurn.size());
        assertEquals(new Move(20, 14), onlyTurn.getFirst());
    }

    @Test
    void legalFullTurnsHigherDieRuleFallsBackToLowerWhenHigherTotallyUnusable() {
        int[] points = new int[Board.NUM_POINTS];
        points[20] = 1;
        points[14] = -2;
        points[11] = -2;
        GameState state = new GameState(Board.customPosition(points, 0, 0, 0, 0), Player.WHITE, List.of(6, 3));

        List<List<Move>> turns = engine.legalFullTurns(state);

        assertEquals(1, turns.size());
        List<Move> onlyTurn = turns.getFirst();
        assertEquals(1, onlyTurn.size());
        assertEquals(new Move(20, 17), onlyTurn.getFirst());
    }

    @Test
    void legalFullTurnsDoublesUseFourSteps() {
        int[] points = new int[Board.NUM_POINTS];
        points[20] = 4;
        GameState state = new GameState(Board.customPosition(points, 0, 0, 0, 0), Player.WHITE, List.of(3, 3, 3, 3));

        List<List<Move>> turns = engine.legalFullTurns(state);

        assertFalse(turns.isEmpty());
        assertEquals(4, turns.getFirst().size());
    }

    @Test
    void legalFullTurnsDoublesPartiallyBlockedUsesAsManyAsPossible() {
        int[] points = new int[Board.NUM_POINTS];
        points[10] = 1;
        points[1] = -2;
        GameState state = new GameState(Board.customPosition(points, 0, 0, 0, 0), Player.WHITE, List.of(3, 3, 3, 3));

        List<List<Move>> turns = engine.legalFullTurns(state);

        int maxLength = turns.stream().mapToInt(List::size).max().orElse(-1);
        assertEquals(2, maxLength);
    }

    @Test
    void legalFullTurnsNoLegalMoveAtAllReturnsEmptySequence() {
        int[] points = new int[Board.NUM_POINTS];
        points[10] = 1;
        points[4] = -2;
        points[6] = -2;
        GameState state = new GameState(Board.customPosition(points, 0, 0, 0, 0), Player.WHITE, List.of(6, 4));

        List<List<Move>> turns = engine.legalFullTurns(state);

        assertEquals(1, turns.size());
        assertTrue(turns.getFirst().isEmpty());
    }

    @Test
    void legalFullTurnsPlayerOnBarNoEntryPossibleReturnsEmptySequence() {
        int[] points = new int[Board.NUM_POINTS];
        points[23] = -2;
        points[22] = -2;
        GameState state = new GameState(Board.customPosition(points, 1, 0, 0, 0), Player.WHITE, List.of(1, 2));

        List<List<Move>> turns = engine.legalFullTurns(state);

        assertEquals(1, turns.size());
        assertTrue(turns.getFirst().isEmpty());
    }

    @Test
    void legalFullTurnsPlayerOnBarEntersWithFirstDieThenContinuesIfSecondDiePlayable() {
        int[] points = new int[Board.NUM_POINTS];
        points[22] = -2;
        GameState state = new GameState(Board.customPosition(points, 1, 0, 0, 0), Player.WHITE, List.of(1, 2));

        List<List<Move>> turns = engine.legalFullTurns(state);

        int maxLength = turns.stream().mapToInt(List::size).max().orElse(-1);
        assertTrue(maxLength >= 1);
        assertTrue(turns.stream().anyMatch(t -> !t.isEmpty() && t.getFirst().equals(new Move(Move.BAR, 23))));
    }

    @Test
    void legalFullTurns_bearOff_playerCanChooseNotToBearOff() {
        int[] points = new int[Board.NUM_POINTS];
        points[3] = 1;
        points[5] = 1;
        GameState state = new GameState(Board.customPosition(points, 0, 0, 0, 0), Player.WHITE, List.of(4, 3));

        List<List<Move>> turns = engine.legalFullTurns(state);
        int maxLength = turns.stream().mapToInt(List::size).max().orElse(0);
        assertEquals(2, maxLength);

        boolean someSequenceBearsOffPoint4Checker = false;
        boolean someSequenceLeavesItOnBoard = false;

        for (List<Move> turn : turns) {
            if (turn.size() != maxLength) {
                continue;
            }

            GameState result = state;
            for (Move move : turn) {
                result = engine.applyMove(result, move);
            }

            if (result.board().borneOffCount(Player.WHITE) == 1) {
                someSequenceBearsOffPoint4Checker = true;
            } else {
                assertEquals(0, result.board().borneOffCount(Player.WHITE));
                someSequenceLeavesItOnBoard = true;
            }
        }

        assertTrue(someSequenceBearsOffPoint4Checker);
        assertTrue(someSequenceLeavesItOnBoard);
    }

    @Test
    void legalFullTurnsCombiningBothDiceOnSameCheckerProducesLongerMove() {
        int[] points = new int[Board.NUM_POINTS];
        points[10] = 1;
        GameState state = new GameState(Board.customPosition(points, 0, 0, 0, 0), Player.WHITE, List.of(3, 2));

        List<List<Move>> turns = engine.legalFullTurns(state);

        assertTrue(turns.stream().anyMatch(t ->
                t.size() == 2 && t.getFirst().from() == 10 && t.get(1).to() == 5));
    }

    @Test
    void legalFullTurnsLastPlayerCheckerBearingOffEndsGameEarlyStillReturnsPartialTurn() {
        int[] points = new int[Board.NUM_POINTS];
        points[0] = 1;
        GameState state = new GameState(Board.customPosition(points, 0, 0, 14, 0), Player.WHITE, List.of(1, 1, 1, 1));

        assertDoesNotThrow(() -> engine.legalFullTurns(state));
    }
}