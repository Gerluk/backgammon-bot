package org.example.bots;

import org.example.model.Dice;
import org.example.model.GameState;
import org.example.model.Move;
import org.example.model.Player;
import org.example.rules.GameEngine;

import java.util.ArrayList;
import java.util.List;

public class ExpectiminimaxBot implements Bot {

    private record DiceOutcome(int die1, int die2, double probability) {}

    private static final List<DiceOutcome> DICE_OUTCOMES = buildDiceOutcomes();

    private static List<DiceOutcome> buildDiceOutcomes() {
        List<DiceOutcome> outcomes = new ArrayList<>();
        for (int d1 = 1; d1 <= 6; d1++) {
            for (int d2 = 1; d2 <= 6; d2++) {
                double probability = (d1 == d2) ? (1.0 / 36.0) : (2.0 / 36.0);
                outcomes.add(new DiceOutcome(d1, d2, probability));
            }
        }
        return List.copyOf(outcomes);
    }

    private final GameEngine engine = new GameEngine();
    private final BoardEvaluator evaluator;
    private final int depth;

    public ExpectiminimaxBot(int depth) {
        this(depth, new BoardEvaluator());
    }

    public ExpectiminimaxBot(int depth, BoardEvaluator evaluator) {
        if (depth < 0) {
            throw new IllegalArgumentException("Głębokość nie może być ujemna");
        }
        this.depth = depth;
        this.evaluator = evaluator;
    }

    @Override
    public List<Move> chooseTurn(GameState state, List<List<Move>> availableTurns) {
        if (availableTurns.isEmpty()) {
            throw new IllegalArgumentException("availableTurn nie może być puste");
        }

        boolean maximizing = state.currentPlayer() == Player.WHITE;

        List<Move> bestTurn = availableTurns.getFirst();
        double bestValue = maximizing ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        double alpha = Double.NEGATIVE_INFINITY;
        double beta = Double.POSITIVE_INFINITY;

        for (List<Move> turn : availableTurns) {
            GameState resultState = applyAll(state, turn);
            double value = chanceValue(resultState, depth, alpha, beta);

            if (maximizing ? value > bestValue : value < bestValue) {
                bestValue = value;
                bestTurn = turn;
            }

            if (maximizing) {
                alpha = Math.max(alpha, bestValue);
            } else {
                beta = Math.min(beta, bestValue);
            }
        }

        return bestTurn;
    }

    public double decisionValue(GameState state, int depthRemaining, double alpha, double beta) {
        if (depthRemaining == 0 || engine.winnerOrNull(state.board()) != null) {
            return evaluator.evaluate(state.board());
        }

        boolean maximizing = state.currentPlayer() == Player.WHITE;
        List<List<Move>> turns = engine.legalFullTurns(state);

        double value = maximizing ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        for (List<Move> turn : turns) {
            GameState resultState = applyAll(state, turn);
            double childValue = chanceValue(resultState, depthRemaining, alpha, beta);

            if (maximizing) {
                value = Math.max(value, childValue);
                if (value >= beta) {
                    return value;
                }
                alpha = Math.max(alpha, value);
            } else {
                value = Math.min(value, childValue);
                if (value <= alpha) {
                    return value;
                }
                beta = Math.min(beta, value);
            }
        }
        return value;
    }

    private double chanceValue(GameState resultState, int depthRemaining, double alpha, double beta) {
        if (depthRemaining == 0 || engine.winnerOrNull(resultState.board()) != null) {
            return evaluator.evaluate(resultState.board());
        }

        double expectedValue = 0.0;
        double remainingProbability = 1.0;

        for (DiceOutcome outcome : DICE_OUTCOMES) {
            GameState nextState = resultState.withNextTurn(new Dice(outcome.die1(), outcome.die2()));
            double value = decisionValue(nextState, depthRemaining - 1, alpha, beta);

            expectedValue += outcome.probability() * value;
            remainingProbability -= outcome.probability();

            double optimisticBound = expectedValue + remainingProbability * BoardEvaluator.MAX_VALUE;
            double pessimisticBound = expectedValue + remainingProbability * BoardEvaluator.MAX_VALUE;

            if (pessimisticBound >= beta) {
                return pessimisticBound;
            }
            if (optimisticBound <= alpha) {
                return optimisticBound;
            }
        }

        return expectedValue;
    }

    private GameState applyAll(GameState state, List<Move> moves) {
        GameState result = state;
        for (Move move : moves) {
            result = engine.applyMove(result, move);
        }
        return result;
    }

    @Override
    public String getName() {
        return "ExpectiminimaxBot(głębokość=" + depth + ")";
    }
}
