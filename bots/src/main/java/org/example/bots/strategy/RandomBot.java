package org.example.bots.strategy;

import org.example.bots.Bot;
import org.example.model.GameState;
import org.example.model.Move;

import java.util.List;
import java.util.Random;

public class RandomBot implements Bot {
    private final Random random;

    public RandomBot() {
        this(new Random());
    }

    public RandomBot(Random random) {
        this.random = random;
    }

    @Override
    public List<Move> chooseTurn(GameState state, List<List<Move>> availableTurns) {
        if  (availableTurns.isEmpty()) {
            throw new IllegalStateException("availableTurns nie może być puste");
        }
        int index = random.nextInt(availableTurns.size());
        return availableTurns.get(index);
    }

    @Override
    public String getName() {
        return "RandomBot";
    }
}
