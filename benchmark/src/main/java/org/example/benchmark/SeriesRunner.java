package org.example.benchmark;

import org.example.bots.Bot;
import org.example.bots.MatchRunner;
import org.example.model.Board;
import org.example.model.Player;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class SeriesRunner {

    public List<GameRecord> runSeries(String matchupId,
                                      String sideALabel, Supplier<Bot> sideASupplier,
                                      String sideBLabel, Supplier<Bot> sideBSupplier,
                                      int numGames) {
        return IntStream.range(0, numGames)
                .parallel()
                .mapToObj(gameIndex -> playOneGame(
                        matchupId, gameIndex, sideALabel, sideASupplier, sideBLabel, sideBSupplier))
                .toList();
    }

    private GameRecord playOneGame(String matchupId, int gameIndex, String sideALabel, Supplier<Bot> sideASupplier,
                                   String sideBLabel, Supplier<Bot> sideBSupplier) {
        TimingBot timedA = new TimingBot(sideASupplier.get());
        TimingBot timedB = new TimingBot(sideBSupplier.get());

        Player sideAColor = (gameIndex % 2 == 0) ? Player.WHITE : Player.BLACK;
        Bot whiteBot = sideAColor == Player.WHITE ? timedA : timedB;
        Bot blackBot = sideAColor == Player.WHITE ? timedB : timedA;

        MatchRunner runner = new MatchRunner(whiteBot, blackBot, new Random());

        int turnCount = 0;
        while (!runner.isGameOver()) {
            runner.playNextTurn();
            turnCount++;
        }

        Player winner = runner.winner();
        Board board = runner.currentState().board();
        String winnerSide = (winner == sideAColor) ? "A" : "B";

        return new GameRecord(
                matchupId, gameIndex, sideALabel, sideBLabel, sideAColor, winnerSide,
                board.isGammon(winner), board.isBackgammon(winner), turnCount,
                nanosToMillis(timedA.totalNanos()), timedA.decisionCount(),
                nanosToMillis(timedB.totalNanos()), timedB.decisionCount()
        );
    }

        private static double nanosToMillis(long nanos) {
            return nanos / 1_000_000.0;
    }
}
