package org.example.model;

public record Move(int from, int to) {

    public static final int BAR = -1;
    public static final int OFF = -2;

    public Boolean isEnteringFromBar() {
        return from == BAR;
    }

    public Boolean isBearingOff() {
        return to == OFF;
    }
}
