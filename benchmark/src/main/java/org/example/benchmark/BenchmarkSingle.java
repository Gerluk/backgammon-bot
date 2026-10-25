package org.example.benchmark;

import org.example.bots.Bot;
import org.example.bots.MatchRunner;
import org.example.bots.strategy.ExpectiminimaxBot;
import org.example.bots.strategy.RandomBot;
import org.example.model.Board;
import org.example.model.Player;

import java.util.Random;

public class BenchmarkSingle {

    private static final int DEPTH = 3;

    public static void main(String[] args) {
        Bot rawA = new ExpectiminimaxBot(DEPTH);
        Bot rawB = new RandomBot();

        TimingBot botA = new TimingBot(rawA);
        TimingBot botB = new TimingBot(rawB);

        MatchRunner runner = new MatchRunner(botA, botB, new Random());

        int turnCount = 0;
        long startTime = System.currentTimeMillis();

        System.out.println("Test jednej partii");
        System.out.println(botA.getName() + " vs " + botB.getName());

        while (!runner.isGameOver()) {
            runner.playNextTurn();
            turnCount++;

            if (turnCount % 10 == 0) {
                double elapsedTime = (System.currentTimeMillis() - startTime) / 1000.0;

                System.out.printf("Tura: %d | Czas całkowity: %.2f s\n", turnCount, elapsedTime);
            }
        }

        double elapsedTime = (System.currentTimeMillis() - startTime) / 1000.0;

        Player winner = runner.winner();
        Board board = runner.currentState().board();

        System.out.println("Zwycięzca: " + winner);
        System.out.println("Liczba tur: " + turnCount);
        System.out.println("Czas partii: " + elapsedTime);

        System.out.printf("Czas decyzji bota A: %.2f s (%d decyzji)\n", botA.totalNanos() / 1000000000.0, botA.decisionCount());
        System.out.printf("Czas decyzji bota B: %.2f s (%d decyzji)\n", botB.totalNanos() / 1000000000.0, botB.decisionCount());
        System.out.printf("Gammon/Backgammon: " + board.isGammon(winner) + "/" + board.isBackgammon(winner));
    }
}
