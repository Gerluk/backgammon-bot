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

    private static final int NUM_GAMES = 100;
    private static final int[] EXPECTIMINIMAX_DEPTHS = {0, 1, 2, 3};
    private static final int[] MONTE_CARLO_SIMULATIONS = {25, 50, 75, 100};

    public static void main(String[] args) throws IOException {
        SeriesRunner runner = new SeriesRunner();
        List<GameRecord> allRecords = new ArrayList<>();

        for (int depth : EXPECTIMINIMAX_DEPTHS) {
            String matchupId = "Expectiminimax(depth=" + depth + ") vs Random";
            allRecords.addAll(runTimedSeries(runner, matchupId,
                    "Expectiminimax(depth=" + depth + ")", () -> new ExpectiminimaxBot(depth),
                    "Random", RandomBot::new));
        }

        for (int sims : MONTE_CARLO_SIMULATIONS) {
            String matchupId = "MonteCarlo(sims=" + sims + ") vs Random";
            allRecords.addAll(runTimedSeries(runner, matchupId,
                    "MonteCarlo(sims=" + sims + ")", () -> new MonteCarloBot(sims),
                            "Random", RandomBot::new));
        }

        Path outputFile = Path.of("benchmark-results.csv");
        CsvExporter.export(allRecords, outputFile);
        System.out.println("Zapisano " + allRecords.size() + " gier do " + outputFile.toAbsolutePath());
    }

    private static List<GameRecord> runTimedSeries(SeriesRunner runner, String matchupId, String sideALabel,
                                                   Supplier<Bot> sideA, String sideBLabel, Supplier<Bot> sideB) {
        System.out.println("Uruchamiam: " + matchupId + " (" + NUM_GAMES + " gier)");
        long start = System.nanoTime();
        List<GameRecord> records = runner.runSeries(matchupId, sideALabel, sideA, sideBLabel, sideB, NUM_GAMES);
        double seconds = (System.nanoTime() - start) / 1_000_000_000.0;
        System.out.printf("   zakończono w %.1fs (%.2fs/grę)%n", seconds, seconds / NUM_GAMES);
        return records;
    }
}
