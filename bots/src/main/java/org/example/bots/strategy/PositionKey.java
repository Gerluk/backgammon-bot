package org.example.bots.strategy;

import org.example.model.Board;
import org.example.model.Player;

final class PositionKey {

    private static final int OFFSET = 32;

    private PositionKey() {}

    static String of(Board board) {
        char[] key = new char[Board.NUM_POINTS + 4];
        for (int i = 0; i < Board.NUM_POINTS; i++) {
            key[i] = (char) (board.pointValue(i) + OFFSET);
        }
        key[Board.NUM_POINTS] = (char) (board.barCount(Player.WHITE));
        key[Board.NUM_POINTS + 1] = (char) (board.barCount(Player.BLACK));
        key[Board.NUM_POINTS + 2] = (char) (board.borneOffCount(Player.WHITE));
        key[Board.NUM_POINTS + 3] = (char) (board.borneOffCount(Player.BLACK));
        return new String(key);
    }
}
