package org.example.benchmark;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CsvExporter {

    private static final String HEADER = String.join(",",
            "matchupId", "gameIndex", "sideALabel", "sideBLabel", "sideAColor", "winnerSide",
            "gammon", "backgammon", "turnCount",
            "sideATotalDecisionMillis", "sideADecisionCount", "sideAAvgDecisionMillis",
            "sideBTotalDecisionMillis", "sideBDecisionCount", "sideBAvgDecisionMillis"
    );

    public static void export(List<GameRecord> records, Path outputFile) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
            writer.write(HEADER);
            writer.newLine();
            for (GameRecord record : records) {
                writer.write(toCsvRow(record));
                writer.newLine();
            }
        }
    }

    private static String toCsvRow(GameRecord r) {
        double sideAAvg = r.sideADecisionCount() == 0 ? 0.0 : r.sideATotalDecisionMillis() / r.sideADecisionCount();
        double sideBAvg = r.sideBDecisionCount() == 0 ? 0.0 : r.sideBTotalDecisionMillis() / r.sideBDecisionCount();

        return String.join(",",
                escape(r.matchupId()),
                String.valueOf(r.gameIndex()),
                escape(r.sideALabel()),
                escape(r.sideBLabel()),
                r.sideAColor().toString(),
                r.winnerSide(),
                String.valueOf(r.gammon()),
                String.valueOf(r.backgammon()),
                String.valueOf(r.turnCount()),
                String.valueOf(r.sideATotalDecisionMillis()),
                String.valueOf(r.sideADecisionCount()),
                String.valueOf(sideAAvg),
                String.valueOf(r.sideBTotalDecisionMillis()),
                String.valueOf(r.sideBDecisionCount()),
                String.valueOf(sideBAvg)
        );
    }

    private static String escape(String value) {
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
