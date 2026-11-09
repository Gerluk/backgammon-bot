package org.example.bots.strategy;

import org.example.model.*;
import org.example.rules.GameEngine;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class ExpectiminimaxPruningTest {

    private final GameEngine engine = new GameEngine();
    private final BoardEvaluator evaluator = new BoardEvaluator();

    private double refDecision(GameState state, int depth) {
        if (depth == 0 || engine.winnerOrNull(state.board()) != null) {
            return evaluator.evaluate(state.board());
        }
        boolean max = state.currentPlayer() == Player.WHITE;
        double best = max ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        for (List<Move> turn : engine.legalFullTurns(state)) {
            double v = refChance(applyAll(state, turn), depth);
            best = max ? Math.max(best, v) : Math.min(best, v);
        }
        return best;
    }

    private double refChance(GameState state, int depth) {
        if (depth == 0 || engine.winnerOrNull(state.board()) != null) {
            return evaluator.evaluate(state.board());
        }
        double sum = 0.0;
        for (int d1 = 1; d1 <= 6; d1++) {
            for (int d2 = 1; d2 <= d1; d2++) {
                double p = (d1 == d2) ? 1.0 / 36.0 : 2.0 / 36.0;
                sum += p * refDecision(state.withNextTurn(new Dice(d1, d2)), depth - 1);
            }
        }
        return sum;
    }

    private GameState applyAll(GameState state, List<Move> moves) {
        GameState result = state;
        for (Move m : moves) {
            result = engine.applyMove(result, m);
        }
        return result;
    }

    private GameState randomPosition(long seed) {
        Random rng = new Random(seed);
        GameState state = GameState.startingState(Player.WHITE, Dice.roll(rng));
        int turns = rng.nextInt(0, 40);
        for (int i = 0; i < turns; i++) {
            List<List<Move>> options = engine.legalFullTurns(state);
            GameState after = applyAll(state, options.get(rng.nextInt(options.size())));
            if (engine.winnerOrNull(after.board()) != null) {
                break;
            }
            state = after.withNextTurn(Dice.roll(rng));
        }
        return state;
    }

    private void assertBotMatchesReference(int depth, int positions) {
        ExpectiminimaxBot bot = new ExpectiminimaxBot(depth);
        boolean white;
        for (long seed = 0; seed < positions; seed++) {
            GameState state = randomPosition(seed);
            if (engine.winnerOrNull(state.board()) != null) {
                continue;
            }
            white = state.currentPlayer() == Player.WHITE;
            List<List<Move>> turns = engine.legalFullTurns(state);

            double best = white ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
            for (List<Move> t : turns) {
                double v = refChance(applyAll(state, t), depth);
                best = white ? Math.max(best, v) : Math.min(best, v);
            }

            List<Move> chosen = bot.chooseTurn(state, turns);
            double chosenValue = refChance(applyAll(state, chosen), depth);

            assertEquals(best, chosenValue, 1e-9,
                    "Pozycja seed=" + seed + ": bot wybrał ruch o wartości " + chosenValue + ", a najlepszy ma " + best);
        }
    }

    @Test
    void depth1MatchesUnprunedReference() {
        assertBotMatchesReference(1, 300);
    }

    @Test
    void depth2MatchesUnprunedReference() {
        assertBotMatchesReference(2, 300);
    }
}