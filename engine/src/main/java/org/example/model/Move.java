package org.example.model;

public record Move(int from, int to) {

    public static final int BAR = -1;
    public static final int OFF = -2;

    public boolean isEnteringFromBar() {
        return from == BAR;
    }

    public boolean isBearingOff() {
        return to == OFF;
    }
}
