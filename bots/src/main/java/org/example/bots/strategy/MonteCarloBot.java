package org.example.bots.strategy;

import org.example.bots.Bot;
import org.example.model.*;
import org.example.rules.GameEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.SplittableRandom;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class MonteCarloBot implements Bot {

    private static final int MAX_ROLLOUT_TURNS = 1000;

    private final GameEngine engine = new GameEngine();
    private final Function<Random, Bot> basePolicyFactory;
    private final String basePolicyName;
    private final int simulationsPerCandidate;
    private final int threads;
    private final Random random;
    private final ExecutorService executor;

    public MonteCarloBot(int simulationsPerCandidate) {
        this(simulationsPerCandidate, 1);
    }

    public MonteCarloBot(int simulationsPerCandidate, int threads) {
        this(simulationsPerCandidate, threads, RandomBot::new, new Random());
    }

    public MonteCarloBot(int simulationsPerCandidate, int threads,
                         Function<Random, Bot> basePolicyFactory, Random random) {
        if (simulationsPerCandidate <= 0) {
            throw new IllegalArgumentException("Liczba symulacji musi być dodatnia");
        }
        if (threads <= 0) {
            throw new IllegalArgumentException("Liczba wątków musi być dodatnia");
        }
        this.simulationsPerCandidate = simulationsPerCandidate;
        this.threads = threads;
        this.basePolicyFactory = basePolicyFactory;
        this.basePolicyName = basePolicyFactory.apply(new Random()).getName();
        this.random = random;
        this.executor = threads > 1 ? newPool(threads) : null;
    }

    @Override
    public List<Move> chooseTurn(GameState state, List<List<Move>> availableTurns) {
        if (availableTurns.isEmpty()) {
            throw new IllegalArgumentException("availableTurn nie może być puste");
        }

        Player mover = state.currentPlayer();
        int candidates = availableTurns.size();

        GameState[] resultStates = new GameState[candidates];
        for (int i = 0; i < candidates; i++) {
            resultStates[i] = applyAll(state, availableTurns.get(i));
        }

        int total = candidates * simulationsPerCandidate;
        long[] seeds = new long[total];
        SplittableRandom splitter = new SplittableRandom(random.nextLong());
        for (int i = 0; i < total; i++) {
            seeds[i] = splitter.nextLong();
        }

        double[] scores = runSimulation(resultStates, mover, seeds);

        int bestIndex = 0;
        double bestAvg = Double.NEGATIVE_INFINITY;
        for (int c = 0; c < candidates; c++) {
            double sum = 0.0;
            for (int s = 0; s < simulationsPerCandidate; s++) {
                sum += scores[c *  simulationsPerCandidate + s];
            }
            double avg = sum / simulationsPerCandidate;
            if (avg > bestAvg) {
                bestAvg = avg;
                bestIndex = c;
            }
        }
        return availableTurns.get(bestIndex);
    }

    private double[] runSimulation(GameState[] resultStates, Player mover, long[] seeds) {
        int total = seeds.length;
        double[] scores = new double[total];

        if (executor == null) {
            for (int t = 0; t < total; t++) {
                scores[t] = simulate(resultStates[t / simulationsPerCandidate], mover, seeds[t]);
            }
            return scores;
        }

        List<Callable<Void>> tasks = new ArrayList<>(total);
        for (int t = 0; t < total; t++) {
            final int index = t;
            tasks.add(() -> {
                scores[index] = simulate(resultStates[index / simulationsPerCandidate], mover, seeds[index]);
                return null;
            });
        }

        try {
            for (Future<Void> future : executor.invokeAll(tasks)) {
                future.get();
            }
        } catch (InterruptedException e) {
            throw new IllegalStateException("Przerwano symulacje Monte Carlo", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Błąd w symulacji Monte Carlo", e);
        }
        return scores;
    }

    private double simulate(GameState state, Player mover, long seed) {
        Random rng = new Random(seed);
        return rollout(state, mover, rng, basePolicyFactory.apply(rng));
    }

    private double rollout(GameState state, Player mover, Random rng, Bot basePolicy) {
        GameState current = state.withNextTurn(Dice.roll(rng));

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

            current = afterMoves.withNextTurn(Dice.roll(rng));
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

    private static ExecutorService newPool(int threads) {
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                threads, threads, 1L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
                runnable -> {
                    Thread thread =  new Thread(runnable, "montecarlo-sim");
                    thread.setDaemon(true);
                    return thread;
                });
        pool.allowCoreThreadTimeOut(true);
        return pool;
    }

    @Override
    public String getName() {
        return "MonteCarloBot(N=" + simulationsPerCandidate + ")";
    }
}
