package org.example.bots;

public final class PlayerConfig {

    private final Bot bot;

    private PlayerConfig(Bot bot) {
        this.bot = bot;
    }

    public static PlayerConfig human() {
        return new PlayerConfig(null);
    }

    public static PlayerConfig bot(Bot bot) {
        if (bot == null) {
            throw new IllegalArgumentException("Bot nie może być null");
        }
        return new PlayerConfig(bot);
    }

    public boolean isHuman() {
        return bot == null;
    }

    public boolean isBot() {
        return bot != null;
    }

    public Bot getBot() {
        if (bot == null) {
            throw new IllegalStateException("Ta konfiguracja reprezentuje człowieka");
        }
        return bot;
    }

    public String displayName() {
        return isHuman() ? "Człowiek" : getBot().getName();
    }
}
