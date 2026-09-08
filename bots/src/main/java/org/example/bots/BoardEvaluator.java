package org.example.bots;

import org.example.model.Board;
import org.example.model.Player;
import org.example.rules.GameEngine;

public class BoardEvaluator {

    private static final double PIP_WEIGHT = 1.0;
    private static final double BAR_WEIGHT = 10.0;
    private static final double BLOT_WEIGHT = 1.5;
    private static final double POINT_WEIGHT = 2.0;
    private static final double HOME_POINT_BONUS = 1.5;

    private static final double WIN_SCORE = 1000;
    private static final double GAMMON_SCORE = 2000;
    private static final double BACKGAMMON_SCORE = 3000;

    public static final double MAX_VALUE = BACKGAMMON_SCORE;
    public static final double MIN_VALUE = -BACKGAMMON_SCORE;

    private final GameEngine engine = new GameEngine();

    public double evaluate(Board board) {
        Player winner = engine.winnerOrNull(board);
        if (winner != null) {
            return terminalScore(board, winner);
        }

        double pipDiff = engine.pipCount(board, Player.BLACK) - engine.pipCount(board, Player.WHITE);
        double barDiff = board.barCount(Player.BLACK) - board.barCount(Player.WHITE);
        double blotDiff = countBlots(board, Player.BLACK) - countBlots(board, Player.WHITE);
        double pointDiff = countPoints(board, Player.WHITE) - countPoints(board, Player.BLACK);
        double homeDiff = countHomePoints(board, Player.WHITE) - countHomePoints(board, Player.BLACK);

        return PIP_WEIGHT * pipDiff
                + BAR_WEIGHT * barDiff
                + BLOT_WEIGHT * blotDiff
                + POINT_WEIGHT * pointDiff
                + HOME_POINT_BONUS * homeDiff;
    }

    private double terminalScore(Board board, Player winner) {
        double score = board.isBackgammon(winner) ? BACKGAMMON_SCORE
                : board.isGammon(winner) ? GAMMON_SCORE
                : WIN_SCORE;
        return winner == Player.WHITE ? score : -score;
    }

    private int countBlots(Board board, Player player) {
        int count = 0;
        for (int i = 0; i < Board.NUM_POINTS; i++) {
            if (board.isBlot(player, i)) {
                count++;
            }
        }
        return count;
    }

    private int countPoints(Board board, Player player) {
        int count = 0;
        for (int i = 0; i < Board.NUM_POINTS; i++) {
            if (board.checkersOf(player, i) >= 2) {
                count++;
            }
        }
        return count;
    }

    private int countHomePoints(Board board, Player player) {
        int count = 0;
        for (int i = 0; i < Board.NUM_POINTS; i++) {
            if (board.isInHome(player, i) && board.checkersOf(player, i) >= 2) {
                count++;
            }
        }
        return count;
    }
}
