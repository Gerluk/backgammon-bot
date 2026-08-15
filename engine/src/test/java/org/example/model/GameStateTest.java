package org.example.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameStateTest {

    @Test
    void startingStateUsesInitialBoardSetup() {
        GameState state = GameState.startingState(Player.WHITE, new Dice(3, 5));

        assertEquals(2, state.board().checkersOf(Player.WHITE, 23));
        assertEquals(15, totalCheckers(state.board(), Player.WHITE));
        assertEquals(15, totalCheckers(state.board(), Player.BLACK));
        assertEquals(2, state.board().checkersOf(Player.BLACK, 0));
    }

    @Test
    void startingStateSetsGivenFirstPlayer() {
        GameState whiteFirst = GameState.startingState(Player.WHITE, new Dice(3, 5));
        GameState blackFirst = GameState.startingState(Player.BLACK, new Dice(3, 5));

        assertEquals(Player.WHITE, whiteFirst.currentPlayer());
        assertEquals(Player.BLACK, blackFirst.currentPlayer());
    }

    @Test
    void startingStateOrdinaryRollHasTwoRemainingDice() {
        GameState state = GameState.startingState(Player.WHITE, new Dice(3, 5));
        assertEquals(List.of(3, 5), state.remainingDice());
    }

    @Test
    void startingStateDoubleRollHasFourRemainingDice() {
        GameState state = GameState.startingState(Player.WHITE, new Dice(4, 4));
        assertEquals(List.of(4, 4, 4, 4), state.remainingDice());
    }

    @Test
    void hasRemainingDiceTrueWhenDicePresent() {
        GameState state = new GameState(Board.empty(), Player.WHITE, List.of(3));
        assertTrue(state.hasRemainingDice());
    }

    @Test
    void hasRemainingDiceFalseWhenEmpty() {
        GameState state = new GameState(Board.empty(), Player.WHITE, List.of());
        assertFalse(state.hasRemainingDice());
    }

    @Test
    void withNextTurnSwitchesPlayerToOpponent() {
        GameState state = new GameState(Board.initialSetup(), Player.WHITE, List.of());
        GameState next = state.withNextTurn(new Dice(2, 6));

        assertEquals(Player.BLACK, next.currentPlayer());
    }

    @Test
    void withNextTurnSwitchesBackAndForthCorrectly() {
        GameState state = new GameState(Board.initialSetup(), Player.BLACK, List.of());
        GameState next = state.withNextTurn(new Dice(2, 6));
        GameState nextNext = next.withNextTurn(new Dice(2, 6));

        assertEquals(Player.WHITE, next.currentPlayer());
        assertEquals(Player.BLACK, nextNext.currentPlayer());
    }

    @Test
    void withNextTurnSetsNewDice() {
        GameState state = new GameState(Board.initialSetup(), Player.WHITE, List.of(1, 2));
        GameState next = state.withNextTurn(new Dice(5, 5));

        assertEquals(List.of(5, 5, 5, 5), next.remainingDice());
    }

    @Test
    void withNextTurnPreservesSameBoardReference() {
        Board board = Board.initialSetup();
        GameState state = new GameState(board, Player.WHITE, List.of());
        GameState next = state.withNextTurn(new Dice(2, 6));

        assertSame(board, next.board());
    }

    @Test
    void recordAccessorsReturnConstructorValues() {
        Board board = Board.initialSetup();
        List<Integer> dice = List.of(6, 4);
        GameState state = new GameState(board, Player.BLACK, dice);

        assertSame(board, state.board());
        assertEquals(Player.BLACK, state.currentPlayer());
        assertEquals(dice, state.remainingDice());
    }

    @Test
    void sameComponentValuesAreEqual() {
        Board board = Board.initialSetup();
        GameState a = new GameState(board, Player.WHITE, List.of(3, 4));
        GameState b = new GameState(board, Player.WHITE, List.of(3, 4));

        assertEquals(a, b);
    }

    @Test
    void differentCurrentPlayerAreNotEqual() {
        Board board = Board.initialSetup();
        GameState a = new GameState(board, Player.WHITE, List.of(3, 4));
        GameState b = new GameState(board, Player.BLACK, List.of(3, 4));

        assertNotEquals(a, b);
    }

    @Test
    void differentRemainingDiceAreNotEqual() {
        Board board = Board.initialSetup();
        GameState a = new GameState(board, Player.WHITE, List.of(3, 4));
        GameState b = new GameState(board, Player.WHITE, List.of(3, 5));

        assertNotEquals(a, b);
    }

    private int totalCheckers(Board board, Player player) {
        int total = 0;
        for (int i = 0; i < Board.NUM_POINTS; i++) {
            total += board.checkersOf(player, i);
        }
        return total;
    }
}