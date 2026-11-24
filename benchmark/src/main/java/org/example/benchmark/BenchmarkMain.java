package org.example.benchmark;

import org.example.bots.Bot;
import org.example.bots.strategy.ExpectiminimaxBot;
import org.example.bots.strategy.MonteCarloBot;
import org.example.bots.strategy.RandomBot;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class BenchmarkMain {

    private static final int CHUNK_GAMES = 50;
    private static final int VS_RANDOM_GAMES = 200;
    private static final int DUEL_GAMES = 500;
    private static final int THREADS = Runtime.getRuntime().availableProcessors();
    private static final Path OUTPUT = Path.of("results", "benchmark-results.csv");

    private record Contestant(String label, Supplier<Bot> factory) {}

    private static Contestant expectiminimax(int depth) {
        return new Contestant("Expectiminimax(depth=" + depth + ")", () -> new ExpectiminimaxBot(depth));
    }

    private static Contestant monteCarlo(int sims) {
        return new Contestant("MonteCarlo(sims=" + sims + ")", () -> new MonteCarloBot(sims, THREADS));
    }

    public static void main(String[] args) throws IOException {
        SeriesRunner runner = new SeriesRunner();
        Contestant random = new Contestant("Random", RandomBot::new);

        List<Contestant> monteCarlos = List.of(
                monteCarlo(25), monteCarlo(50), monteCarlo(75), monteCarlo(100));

        for (Contestant bot : List.of(
                expectiminimax(1), expectiminimax(2), monteCarlos.getFirst(), monteCarlos.getLast())) {
            runMatchup(runner, bot, random, VS_RANDOM_GAMES);
        }

        for (int depth = 0; depth <= 3; depth++) {
            Contestant em = expectiminimax(depth);
            for (Contestant mc : monteCarlos) {
                runMatchup(runner, em, mc, DUEL_GAMES);
            }
        }

        System.out.println("Zakończono. Wyniki: " + OUTPUT.toAbsolutePath());
    }

    private static void runMatchup(SeriesRunner runner, Contestant a, Contestant b, int games) throws IOException {
        String matchupId = a.label() + " vs " + b.label();
        System.out.println("Uruchamiam: " + matchupId + " (" + games + " gier)");
        long start = System.nanoTime();

        for (int done = 0; done < games; done += CHUNK_GAMES) {
            int n = Math.min(CHUNK_GAMES, games - done);
            List<GameRecord> records = runner.runSeries(matchupId, a.label(), a.factory(), b.label(), b.factory(), n);
            CsvExporter.append(records, OUTPUT);
            double seconds = (System.nanoTime() - start) / 1_000_000_000.0;
            System.out.printf(" %d/%d gier, %.1f s od początku pojedynku%n", done + n, games, seconds);
        }
    }
}
