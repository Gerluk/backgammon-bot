package org.example.bots;

import org.example.bots.strategy.RandomBot;
import org.example.model.*;
import org.example.rules.GameEngine;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


public class MatchRunnerTest {

    // Pomocniczy bot deterministyczny wybierający pierwszą opcję
    private static class FirstChoiceBot implements Bot {
        @Override
        public List<Move> chooseTurn(GameState state, List<List<Move>> availableTurns) {
            return availableTurns.getFirst();
        }

        @Override
        public String getName() {
            return "FirstChoiceBot";
        }
    }

    // Pomocniczy bot deterministyczny wybierający ostatnią opcję
    private static class LastChoiceBot implements Bot {
        @Override
        public List<Move> chooseTurn(GameState state, List<List<Move>> availableTurns) {
            return availableTurns.getLast();
        }

        @Override
        public String getName() {
            return "LastChoiceBot";
        }
    }

    @Test
    void constructorStartsWithInitialBoardSetup() {
        MatchRunner runner = new MatchRunner(new RandomBot(), new RandomBot(), new Random(42));

        Board board = runner.currentState().board();

        assertEquals(2, board.checkersOf(Player.BLACK, 0));
        assertEquals(2, board.checkersOf(Player.WHITE, 23));
    }

    @Test
    void constructorStartsWithValidDiceCount() {
        MatchRunner runner = new MatchRunner(new RandomBot(), new RandomBot(), new Random(42));

        int diceCount = runner.currentState().remainingDice().size();
        assertTrue(diceCount == 2 || diceCount == 4);
    }

    @Test
    void gameNotOverInitially() {
        MatchRunner runner = new MatchRunner(new RandomBot(), new RandomBot(), new Random(42));

        assertFalse(runner.isGameOver());
        assertNull(runner.winner());
    }

    @Test
    void playNextTurnAppliesChosenMovesToBoard() {
        MatchRunner runner = new MatchRunner(new FirstChoiceBot(), new FirstChoiceBot(), new Random(42));
        GameState before = runner.currentState();
        List<Move> played = runner.playNextTurn();

        assertFalse(played.isEmpty());

        GameEngine engine = new GameEngine();
        GameState manuallyApplied = before;
        for (Move move : played) {
            manuallyApplied = engine.applyMove(manuallyApplied, move);
        }

        for (int i = 0; i < Board.NUM_POINTS; i++) {
            assertEquals(manuallyApplied.board().pointValue(i), runner.currentState().board().pointValue(i));
        }
    }

    @Test
    void playNextTurnSwitchesPlayerAfterTurn() {
        MatchRunner runner = new MatchRunner(new RandomBot(), new RandomBot(), new Random(42));
        Player first = runner.currentState().currentPlayer();
        runner.playNextTurn();

        if (!runner.isGameOver()) {
            assertEquals(first.opponent(), runner.currentState().currentPlayer());
        }
    }

    @Test
    void playNextTurnRollsNewDiceForNextPlayer() {
        MatchRunner runner = new MatchRunner(new RandomBot(), new RandomBot(), new Random(42));
        runner.playNextTurn();

        if (!runner.isGameOver()) {
            int diceCount = runner.currentState().remainingDice().size();
            assertTrue(diceCount == 2 || diceCount == 4);
        }
    }

    @Test
    void playNextTurnAlternatesPlayersOverMultipleTurns() {
        MatchRunner runner = new MatchRunner(new RandomBot(), new RandomBot(), new Random(42));
        Player expectedPlayer = runner.currentState().currentPlayer();

        for (int i = 0; i < 10; i++) {
            assertEquals(expectedPlayer, runner.currentState().currentPlayer());
            runner.playNextTurn();
            expectedPlayer = expectedPlayer.opponent();
        }
    }

    @Test
    void playNextTurnReturnsEmptyListWhenGameAlreadyOver() {
        MatchRunner runner = new MatchRunner(new RandomBot(), new RandomBot(), new Random(42));

        while (!runner.isGameOver()) {
            runner.playNextTurn();
        }

        List<Move> result = runner.playNextTurn();
        assertTrue(result.isEmpty());
    }

    @Test
    void playNextTurnDoesNotChangeStateFurtherWhileCalledRepeatedlyAfterGameOver() {
        MatchRunner runner = new MatchRunner(new RandomBot(), new RandomBot(), new Random(42));

        while (!runner.isGameOver()) {
            runner.playNextTurn();
        }

        Player winnerBefore = runner.winner();
        GameState stateBefore = runner.currentState();

        runner.playNextTurn();
        runner.playNextTurn();

        assertEquals(winnerBefore, runner.winner());
        assertEquals(stateBefore, runner.currentState());
    }

    @Test
    void playNextTurnUsesCorrectBotForCorrectPlayer() {
        GameEngine engine = new GameEngine();
        MatchRunner runner = new MatchRunner(new FirstChoiceBot(), new LastChoiceBot(), new Random(42));

        GameState beforeFirstTurn = runner.currentState();
        List<Move> expectedFirstTurn = expectedTurnFor(engine, beforeFirstTurn);
        runner.playNextTurn();
        GameState afterFirstTurn = runner.currentState();
        GameState expectedAfterFirst = applyAll(engine, beforeFirstTurn, expectedFirstTurn);

        for (int i = 0; i < Board.NUM_POINTS; i++) {
            assertEquals(expectedAfterFirst.board().pointValue(i), afterFirstTurn.board().pointValue(i));
        }

        if (runner.isGameOver()) {
            return;
        }

        List<Move> expectedSecondTurn = expectedTurnFor(engine, afterFirstTurn);
        runner.playNextTurn();
        GameState expectedAfterSecond = applyAll(engine, afterFirstTurn, expectedSecondTurn);

        for (int i = 0; i < Board.NUM_POINTS; i++) {
            assertEquals(expectedAfterSecond.board().pointValue(i), expectedAfterSecond.board().pointValue(i));
        }
    }

    private List<Move> expectedTurnFor(GameEngine engine, GameState state) {
        List<List<Move>> options = engine.legalFullTurns(state);
        return state.currentPlayer() == Player.WHITE ? options.getFirst() : options.getLast();
    }

    private GameState applyAll(GameEngine engine, GameState start, List<Move> moves) {
        GameState result = start;
        for (Move move : moves) {
            result = engine.applyMove(result, move);
        }
        return result;
    }

    @Test
    void matchRunnerWoksCorrectlyWhenSameBotInstanceUsedForBothPlayers() {
        RandomBot sharedBot = new RandomBot(new Random(42));
        MatchRunner runner = new MatchRunner(sharedBot, sharedBot, new Random(42));

        while (!runner.isGameOver()) {
            runner.playNextTurn();
        }

        assertTrue(runner.isGameOver());
        assertNotNull(runner.winner());
    }

    @Test
    void winnerHasAllFifteenCheckersBorneOff() {
        MatchRunner runner = new MatchRunner(new RandomBot(new Random(1)), new RandomBot(new Random(2)), new Random(42));

        while (!runner.isGameOver()) {
            runner.playNextTurn();
        }

        assertNotNull(runner.winner());
        assertTrue(runner.currentState().board().hasWon(runner.winner()));
        assertEquals(15, runner.currentState().board().borneOffCount(runner.winner()));
    }

    @Test
    void loserHasLessThanFifteenCheckersBorneOff() {
        MatchRunner runner = new MatchRunner(new RandomBot(new Random(1)), new RandomBot(new Random(2)), new Random(42));

        while (!runner.isGameOver()) {
            runner.playNextTurn();
        }

        assertNotNull(runner.winner());
        Player loser = runner.winner().opponent();
        assertTrue(runner.currentState().board().borneOffCount(loser) < 15);
    }

    @Test
    void totalCheckersSumsToFifteenPerPlayer() {
        MatchRunner runner = new MatchRunner(new RandomBot(new Random(1)), new RandomBot(new Random(2)), new Random(42));

        while (!runner.isGameOver()) {
            runner.playNextTurn();

            Board board = runner.currentState().board();
            for (Player player : Player.values()) {
                int total = board.barCount(player) + board.borneOffCount(player);
                for (int i = 0; i < Board.NUM_POINTS; i++) {
                    total += board.checkersOf(player, i);
                }

                assertEquals(15, total);
            }
        }
    }

    @Test
    void differentSeedsProduceDifferentGames() {
        RandomBot bot1 = new RandomBot(new Random(1));
        RandomBot bot2 = new RandomBot(new Random(2));

        MatchRunner runnerA = new MatchRunner(bot1, bot2, new Random(10));
        MatchRunner runnerB = new MatchRunner(bot1, bot2, new Random(11));

        while(!runnerA.isGameOver() && !runnerB.isGameOver()) {
            runnerA.playNextTurn();
            runnerB.playNextTurn();
        }

        assertNotEquals(runnerA.currentState(), runnerB.currentState());
        assertNotEquals(runnerA.currentState().board().toString(), runnerB.currentState().board().toString());
    }

    @Test
    void sameSeedProducesIdenticalGame() {

        MatchRunner runnerA = new MatchRunner(new RandomBot(new Random(1)), new RandomBot(new Random(2)), new Random(42));
        MatchRunner runnerB = new MatchRunner(new RandomBot(new Random(1)), new RandomBot(new Random(2)), new Random(42));

        while(!runnerA.isGameOver() && !runnerB.isGameOver()) {
            runnerA.playNextTurn();
            runnerB.playNextTurn();

            assertEquals(runnerA.currentState().board().toString(), runnerB.currentState().board().toString());
            assertEquals(runnerA.currentState().board().barCount(Player.WHITE),
                    runnerB.currentState().board().barCount(Player.WHITE));
            assertEquals(runnerA.currentState().board().barCount(Player.BLACK),
                    runnerB.currentState().board().barCount(Player.BLACK));
            assertEquals(runnerA.currentState().board().borneOffCount(Player.WHITE),
                    runnerB.currentState().board().borneOffCount(Player.WHITE));
            assertEquals(runnerA.currentState().board().borneOffCount(Player.BLACK),
                    runnerB.currentState().board().borneOffCount(Player.BLACK));
        }


    }
}
