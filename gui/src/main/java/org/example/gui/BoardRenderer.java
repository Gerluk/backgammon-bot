package org.example.gui;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import org.example.model.Board;
import org.example.model.Move;
import org.example.model.Player;

import java.util.Set;

public class BoardRenderer {
    private static final double MARGIN = 20;
    private static final double BAR_WIDTH = 40;
    private static final Color POINT_COLOR_A = Color.web("#c9a26a");
    private static final Color POINT_COLOR_B = Color.web("#8a5a34");
    private static final Color BOARD_BACKGROUND = Color.web("#5c3a21");
    private static final Color BAR_COLOR = Color.web("#3e2414");
    private static final Color WHITE_CHECKER = Color.web("#f5f0e6");
    private static final Color BLACK_CHECKER = Color.web("#2b2b2b");
    private static final Color CHECKER_OUTLINE = Color.BLACK;
    private static final Color LABEL_COLOR = Color.web("#f0e6d2");

    public void render(GraphicsContext gc, Board board, double width, double height) {
        gc.setFill(BOARD_BACKGROUND);
        gc.fillRect(0, 0, width, height);

        Geometry g = geometry(width, height);

        gc.setFill(BAR_COLOR);
        gc.fillRect(g.barX0, g.topY, BAR_WIDTH, g.bottomY - g.topY);

        for (int index = 0; index < Board.NUM_POINTS; index++) {
            drawPointTriangle(gc, index, g);
        }

        for (int index = 0; index < Board.NUM_POINTS; index++) {
            int value = board.pointValue(index);
            if (value != 0) {
                Player owner = value > 0 ? Player.WHITE : Player.BLACK;
                int count = Math.abs(value);
                drawCheckerStack(gc, index, count, owner, g);
            }
        }

        drawBarCheckers(gc, board, g);
        drawPointLabels(gc, g);
    }

    public void renderHighlight(GraphicsContext gc, Set<Integer> indices, double width, double height, Color color) {
        Geometry g = geometry(width, height);
        gc.setStroke(color);
        gc.setLineWidth(3);

        for (int index : indices) {
            if (index == Move.BAR) {
                gc.strokeRect(g.barX0 + 2, g.topY + 2, BAR_WIDTH - 4, g.bottomY - g.topY - 4);
                continue;
            }
            double centerX = centerXForPoint(index, g);
            boolean isBottom = isBottomBar(index);
            double midY = (g.topY + g.bottomY) / 2;
            double regionTop = isBottom ? midY : g.topY;
            double regionBottom = isBottom ? g.bottomY : midY;
            gc.strokeRect(centerX - g.pointWidth / 2 + 2, regionTop + 2,
                    g.pointWidth - 4, regionBottom - regionTop - 4);
        }
    }

    public Integer pointIndexAt(double x, double y, double width, double height) {
        Geometry g = geometry(width, height);

        if (x >= g.barX0 && x <= g.barX0 + BAR_WIDTH && y >= g.topY && y <= g.bottomY) {
            return Move.BAR;
        }

        double midY = (g.topY + g.bottomY) / 2;
        for (int index = 0; index < Board.NUM_POINTS; index++) {
            double centerX = centerXForPoint(index, g);
            double left = centerX - g.pointWidth / 2;
            double right = centerX + g.pointWidth / 2;
            if (x < left || x > right) {
                continue;
            }
            boolean isBottom = isBottomBar(index);
            double regionTop = isBottom ? midY : g.topY;
            double regionBottom = isBottom ? g.bottomY : midY;
            if (y >= regionTop && y <= regionBottom) {
                return index;
            }
        }
        return null;
    }

    private record Geometry(double leftHalfX0, double rightHalfX0, double barX0, double pointWidth,
                            double topY, double bottomY, double triangleHeight) {}

    private Geometry geometry(double width, double height) {
        double halfWidth = (width - 2 * MARGIN - BAR_WIDTH) / 2;
        double pointWidth = halfWidth / 6;
        double leftHalfX0 = MARGIN;
        double rightHalfX0 = MARGIN + halfWidth + BAR_WIDTH;
        double barXO =  MARGIN + halfWidth;
        double topY = MARGIN;
        double bottomY = height - MARGIN;
        double triangleHeight = height / 2 -  MARGIN - 10;
        return new Geometry(leftHalfX0, rightHalfX0, barXO, pointWidth, topY, bottomY, triangleHeight);
    }

    private double centerXForPoint(int index, Geometry g) {
        double quadrantX0;
        double posInQuadrant;

        if (index <= 5) {
            quadrantX0 = g.rightHalfX0;
            posInQuadrant = 5 - index;
        } else if (index <= 11) {
            quadrantX0 = g.leftHalfX0;
            posInQuadrant = 11 -  index;
        } else if (index <= 17) {
            quadrantX0 = g.leftHalfX0;
            posInQuadrant = index - 12;
        } else {
            quadrantX0 = g.rightHalfX0;
            posInQuadrant = index - 18;
        }

        return quadrantX0 + posInQuadrant * g.pointWidth + g.pointWidth / 2;
    }

    private boolean isBottomBar(int index) {
        return index <= 11;
    }

    private void drawPointTriangle(GraphicsContext gc, int index, Geometry g) {
        boolean isBottom = isBottomBar(index);
        double centerX = centerXForPoint(index, g);
        double x = centerX - g.pointWidth / 2;
        double baseY = isBottom ? g.bottomY : g.topY;
        double apexY = isBottom ? g.bottomY - g.triangleHeight : g.topY + g.triangleHeight;

        gc.setFill(index % 2 == 0 ? POINT_COLOR_A : POINT_COLOR_B);
        gc. fillPolygon(
                new double[]{x, x + g.pointWidth, centerX},
                new double[]{baseY, baseY, apexY},
                3
        );
    }

    private void drawCheckerStack(GraphicsContext gc, int index, int count, Player owner, Geometry g) {
        boolean isBottom = isBottomBar(index);
        double centerX = centerXForPoint(index, g);
        double radius = g.pointWidth * 0.4;
        int maxDrawn = Math.min(count, 5);

        for (int i = 0; i < maxDrawn; i++) {
            double centerY = isBottom
                    ? g.bottomY - radius * 1.1 - i * radius * 1.8
                    : g.topY + radius * 1.1 + i * radius * 1.8;
            drawChecker(gc, centerX, centerY, radius, owner);
        }

        if (count > 5) {
            double textY = isBottom
                    ? g.bottomY - radius * 1.1 - (maxDrawn - 1) * radius * 1.8
                    : g.topY + radius * 1.1 + (maxDrawn - 1)  * radius * 1.8;
            gc.setFill(owner == Player.WHITE ? Color.BLACK : Color.WHITE);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setFont(Font.font(radius));
            gc.fillText("x" + count, centerX, textY + radius / 3);
        }
    }

    private void drawPointLabels(GraphicsContext gc, Geometry g) {
        gc.setFill(LABEL_COLOR);
        gc.setFont(Font.font("System", FontWeight.BOLD, 11));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        for (int index = 0; index < Board.NUM_POINTS; index++) {
            double centerX = centerXForPoint(index, g);
            double labelY = isBottomBar(index) ? g.bottomY + MARGIN / 2.0 : g.topY - MARGIN / 2.0;
            gc.fillText(String.valueOf(index + 1), centerX, labelY);
        }
    }

    private void drawBarCheckers(GraphicsContext gc, Board board, Geometry g) {
        double barCenterX = g.barX0 + BAR_WIDTH / 2;
        double radius = BAR_WIDTH * 0.35;
        double midY = (g.topY + g.bottomY) / 2;

        int whiteBar = board.barCount(Player.WHITE);
        for (int i = 0; i < whiteBar; i++) {
            drawChecker(gc, barCenterX, midY + 20 + i * radius * 1.8, radius, Player.WHITE);
        }

        int blackBar = board.barCount(Player.BLACK);
        for (int i = 0; i < blackBar; i++) {
            drawChecker(gc, barCenterX, midY - 20 - i * radius * 1.8, radius, Player.BLACK);
        }
    }

    private void drawChecker(GraphicsContext gc, double centerX, double centerY, double radius, Player owner) {
        gc.setFill(owner == Player.WHITE ? WHITE_CHECKER : BLACK_CHECKER);
        gc.fillOval(centerX - radius, centerY - radius, 2 * radius, 2 * radius);
        gc.setStroke(CHECKER_OUTLINE);
        gc.strokeOval(centerX - radius, centerY - radius, 2 * radius, 2 * radius);
    }
}
