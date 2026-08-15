package org.example.gui;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import org.example.model.Board;
import org.example.model.Player;

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

        double halfWidth = (width - 2 * MARGIN - BAR_WIDTH) / 2;
        double pointWidth = halfWidth / 6;
        double leftHalfX0 = MARGIN;
        double rightHalfX0 = MARGIN + halfWidth + BAR_WIDTH;
        double barXO =  MARGIN + halfWidth;
        double topY = MARGIN;
        double bottomY = height - MARGIN;
        double triangleHeight = height / 2 -  MARGIN - 10;

        gc.setFill(BAR_COLOR);
        gc.fillRect(barXO, topY, BAR_WIDTH, bottomY - topY);

        for (int index = 0; index < Board.NUM_POINTS; index++) {
            drawPointTriangle(gc, index, leftHalfX0, rightHalfX0, pointWidth, topY, bottomY, triangleHeight);
        }

        for (int index = 0; index < Board.NUM_POINTS; index++) {
            int value = board.pointValue(index);
            if (value != 0) {
                Player owner = value > 0 ? Player.WHITE : Player.BLACK;
                int count = Math.abs(value);
                drawCheckerStack(gc, index, count, owner, leftHalfX0, rightHalfX0, pointWidth, topY, bottomY);
            }
        }

        drawBarCheckers(gc, board, barXO, topY, bottomY);
        drawPointLabels(gc, leftHalfX0, rightHalfX0, pointWidth, topY, bottomY);
    }

    private double centerXForPoint(int index, double leftHalfX0, double rightHalfX0, double pointWidth) {
        double quadrantX0;
        double posInQuadrant;

        if (index <= 5) {
            quadrantX0 = rightHalfX0;
            posInQuadrant = 5 - index;
        } else if (index <= 11) {
            quadrantX0 = leftHalfX0;
            posInQuadrant = 11 -  index;
        } else if (index <= 17) {
            quadrantX0 = leftHalfX0;
            posInQuadrant = index - 12;
        } else {
            quadrantX0 = rightHalfX0;
            posInQuadrant = index - 18;
        }

        return quadrantX0 + posInQuadrant * pointWidth + pointWidth / 2;
    }

    private boolean isBottomBar(int index) {
        return index <= 11;
    }

    private void drawPointTriangle(GraphicsContext gc, int index, double leftHalfX0, double rightHalfX0,
                                   double pointWidth, double topY, double bottomY, double triangleHeight) {
        boolean isBottom = isBottomBar(index);
        double centerX = centerXForPoint(index, leftHalfX0, rightHalfX0, pointWidth);
        double x = centerX - pointWidth / 2;
        double baseY = isBottom ? bottomY : topY;
        double apexY = isBottom ? bottomY - triangleHeight : topY +  triangleHeight;

        gc.setFill(index % 2 == 0 ? POINT_COLOR_A : POINT_COLOR_B);
        gc. fillPolygon(
                new double[]{x, x + pointWidth, x + pointWidth / 2},
                new double[]{baseY, baseY, apexY},
                3
        );
    }

    private void drawCheckerStack(GraphicsContext gc, int index, int count, Player owner, double leftHalfX0,
                                  double rightHalfX0, double pointWidth, double topY, double bottomY) {
        boolean isBottom = isBottomBar(index);
        double centerX = centerXForPoint(index, leftHalfX0, rightHalfX0, pointWidth);
        double radius = pointWidth * 0.4;
        int maxDrawn = Math.min(count, 5);

        for (int i = 0; i < maxDrawn; i++) {
            double centerY = isBottom
                    ? bottomY - radius * 1.1 - i * radius * 1.8
                    :  topY + radius * 1.1 + i * radius * 1.8;
            drawChecker(gc, centerX, centerY, radius, owner);
        }

        if (count > 5) {
            double textY = isBottom
                    ? bottomY - radius * 1.1 - (maxDrawn - 1) * radius * 1.8
                    : topY + radius * 1.1 + (maxDrawn - 1)  * radius * 1.8;
            gc.setFill(owner == Player.WHITE ? Color.BLACK : Color.WHITE);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setFont(Font.font(radius));
            gc.fillText("x" + count, centerX, textY + radius / 3);
        }
    }

    private void drawPointLabels(GraphicsContext gc, double leftHalfX0, double rightHalfX0,
                                 double pointWidth, double topY, double bottomY) {
        gc.setFill(LABEL_COLOR);
        gc.setFont(Font.font("System", FontWeight.BOLD, 11));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        for (int index = 0; index < Board.NUM_POINTS; index++) {
            double centerX = centerXForPoint(index, leftHalfX0, rightHalfX0, pointWidth);
            double labelY = isBottomBar(index) ? bottomY + MARGIN / 2.0 : topY - MARGIN / 2.0;
            gc.fillText(String.valueOf(index + 1), centerX, labelY);
        }
    }

    private void drawBarCheckers(GraphicsContext gc, Board board, double barX0, double topY, double bottomY) {
        double barCenterX = barX0 + BAR_WIDTH / 2;
        double radius = BAR_WIDTH * 0.35;
        double midY = (topY + bottomY) / 2;

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
