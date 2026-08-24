package org.example.bots;

public enum ParticipantType {

    HUMAN("Człowiek", true),
    RANDOM_BOT("Random Bot", true),
    EXPECTIMINIMAX_BOT("Expectiminimax Bot", false),
    MONTE_CARLO_BOT("Monte Carlo Bot", false);

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
            case EXPECTIMINIMAX_BOT, MONTE_CARLO_BOT ->
                throw new UnsupportedOperationException(displayName + " nie jest jeszcze zaimplementowany");
        };
    }

    @Override
    public String toString() {
        return available ? displayName : displayName + " (wkrótce)";
    }
}
