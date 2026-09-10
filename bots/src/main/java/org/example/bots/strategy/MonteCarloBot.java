package org.example.bots.strategy;

import org.example.bots.Bot;
import org.example.model.*;
import org.example.rules.GameEngine;

import java.util.List;
import java.util.Random;

public class MonteCarloBot implements Bot {

    private static final int MAX_ROLLOUT_TURNS = 1000;

    private final GameEngine engine = new GameEngine();
    private final Bot basePolicy;
    private final int simulationsPerCandidate;
    private final Random random;

    public MonteCarloBot(int simulationsPerCandidate) {
        this(simulationsPerCandidate, new RandomBot(), new Random());
    }

    public MonteCarloBot(int simulationsPerCandidate, Bot basePolicy) {
        this(simulationsPerCandidate, basePolicy, new Random());
    }

    public MonteCarloBot(int simulationsPerCandidate, Bot basePolicy, Random random) {
        if (simulationsPerCandidate <= 0) {
            throw new IllegalArgumentException("Liczba symulacji musi być dodatnia");
        }
        this.simulationsPerCandidate = simulationsPerCandidate;
        this.basePolicy = basePolicy;
        this.random = random;
    }

    @Override
    public List<Move> chooseTurn(GameState state, List<List<Move>> availableTurns) {
        if (availableTurns.isEmpty()) {
            throw new IllegalArgumentException("availableTurn nie może być puste");
        }

        Player mover = state.currentPlayer();
        List<Move> bestTurn = availableTurns.getFirst();
        double bestScore = Double.NEGATIVE_INFINITY;

        for (List<Move> turn : availableTurns) {
            GameState resultState = applyAll(state, turn);
            double averageScore = estimateEquity(resultState, mover);
            if (averageScore > bestScore) {
                bestScore = averageScore;
                bestTurn = turn;
            }
        }

        return bestTurn;
    }

    private double estimateEquity(GameState resultState, Player mover) {
        double total = 0.0;
        for (int i = 0; i < simulationsPerCandidate; i++) {
            total += rollout(resultState, mover);
        }
        return total / simulationsPerCandidate;
    }

    private double rollout(GameState state, Player mover) {
        GameState current = state.withNextTurn(Dice.roll(random));

        for (int turnsPlayed = 0; turnsPlayed < MAX_ROLLOUT_TURNS; turnsPlayed++) {
            List<List<Move>> turns = engine.legalFullTurns(current);
            List<Move> chosen = basePolicy.chooseTurn(current, turns);

            GameState afterMoves = current;
            for (Move move : chosen) {
                afterMoves = engine.applyMove(afterMoves, move);
            }

            Player winner = engine.winnerOrNull(afterMoves.board());
            if (winner != null) {
                return score(afterMoves.board(), winner, mover);
            }

            current = afterMoves.withNextTurn(Dice.roll(random));
        }

        return 0.0;
    }

    private double score(Board board, Player winner, Player mover) {
        double magnitude = board.isBackgammon(winner) ? 3.0
                : board.isGammon(winner) ? 2.0
                : 1.0;
        return winner == mover ? magnitude : -magnitude;
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
        return "MonteCarloBot(N=" + simulationsPerCandidate + ")";
    }
}
