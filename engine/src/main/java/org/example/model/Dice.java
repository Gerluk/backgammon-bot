package org.example.model;

import java.util.List;
import java.util.Random;

public record Dice(int die1, int die2) {

    public Dice {
        if (die1 < 1 || die1 > 6 || die2 < 1 || die2 > 6) {
            throw new IllegalArgumentException("Wartości kości muszą być w zakresie 1-6");
        }
    }

    public static Dice roll(Random random) {
        return new Dice(random.nextInt(1, 7), random.nextInt(1, 7));
    }

    public boolean isDouble() {
        return die1 == die2;
    }

    public List<Integer> availableSteps() {
        if (isDouble()) {
            return List.of(die1, die1, die1, die1);
        } else {
            return List.of(die1, die2);
        }
    }
}
