package org.example.model;

public enum Player {
    WHITE,
    BLACK;

    public Player opponent() {
        return this == WHITE ? BLACK : WHITE;
    }
}
