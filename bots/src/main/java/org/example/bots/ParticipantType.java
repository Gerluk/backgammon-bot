package org.example.bots;

import org.example.bots.strategy.ExpectiminimaxBot;
import org.example.bots.strategy.MonteCarloBot;
import org.example.bots.strategy.RandomBot;

public enum ParticipantType {

    HUMAN("Człowiek", true),
    RANDOM_BOT("Random Bot", true),
    EXPECTIMINIMAX_BOT("Expectiminimax Bot", true),
    MONTE_CARLO_BOT("Monte Carlo Bot", true);

    private final String displayName;
    private final boolean available;

    ParticipantType(String displayName, boolean available) {
        this.displayName = displayName;
        this.available = available;
    }

    public String displayName() {
        return displayName;
    }

    public boolean isAvailable() {
        return available;
    }

    public PlayerConfig createConfig() {
        if (!available) {
            throw new UnsupportedOperationException(displayName + " nie jest jeszcze zaimplementowany");
        }
        return switch (this) {
            case HUMAN -> PlayerConfig.human();
            case RANDOM_BOT -> PlayerConfig.bot(new RandomBot());
            case EXPECTIMINIMAX_BOT -> PlayerConfig.bot(new ExpectiminimaxBot(1));
            case MONTE_CARLO_BOT -> PlayerConfig.bot(new MonteCarloBot(50));
        };
    }

    @Override
    public String toString() {
        return available ? displayName : displayName + " (wkrótce)";
    }
}
