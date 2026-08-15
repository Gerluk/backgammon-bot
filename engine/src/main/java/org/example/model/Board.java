package org.example.model;

import java.util.Arrays;

public final class Board {

    public static final int NUM_POINTS = 24;
    public static final int CHECKERS_PER_PLAYERS = 15;

    private final int[] points;
    private int whiteBar;
    private int blackBar;
    private int whiteBorneOff;
    private int blackBorneOff;

    private Board(int[] points, int whiteBar, int blackBar, int whiteBorneOff, int blackBorneOff) {
        this.points = points;
        this.whiteBar = whiteBar;
        this.blackBar = blackBar;
        this.whiteBorneOff = whiteBorneOff;
        this.blackBorneOff = blackBorneOff;
    }

    public static Board initialSetup() {
        int[] p = new int[NUM_POINTS];

        p[23] = 2;
        p[12] = 5;
        p[7] = 3;
        p[5] = 5;

        p[0] = -2;
        p[11] = -5;
        p[16] = -3;
        p[18] = -5;

        return new Board(p, 0, 0, 0, 0);
    }

    public static Board empty() {
        return new Board(new int[NUM_POINTS], 0, 0, 0, 0);
    }

    public static Board customPosition(int[] pointValues, int whiteBar, int blackBar, int whiteBorneOff, int blackBorneOff) {
        if (pointValues.length != NUM_POINTS) {
            throw new IllegalArgumentException("Tablica punktów musi mieć długość " + NUM_POINTS);
        }
        return new Board(Arrays.copyOf(pointValues, NUM_POINTS), whiteBar, blackBar, whiteBorneOff, blackBorneOff);
    }

    public Board copy() {
        return new Board(Arrays.copyOf(points, points.length), whiteBar, blackBar, whiteBorneOff, blackBorneOff);
    }

    public int pointValue(int index) {
        checkIndex(index);
        return points[index];
    }

    public int checkersOf(Player player, int index) {
        checkIndex(index);
        int value = points[index];
        if (player == Player.WHITE) {
            return Math.max(value, 0);
        } else {
            return Math.max(-value, 0);
        }
    }

    public boolean isEmpty(int index) {
        checkIndex(index);
        return points[index] == 0;
    }

    public boolean isBlockedFor(Player player, int index) {
        checkIndex(index);
        int value = points[index];
        if (player == Player.WHITE) {
            return value <= -2;
        } else {
            return value >= 2;
        }
    }

    public boolean isBlot(Player opponentOf, int index) {
        checkIndex(index);
        int value = points[index];
        if (opponentOf == Player.WHITE) {
            return value == 1;
        } else {
            return value == -1;
        }
    }

    public int barCount(Player player) {
        return player == Player.WHITE ? whiteBar : blackBar;
    }

    public int borneOffCount(Player player) {
        return player == Player.WHITE ? whiteBorneOff : blackBorneOff;
    }

    public void movePiece(Player player, int from, int to) {
        checkIndex(from);
        checkIndex(to);

        if (isBlot(player.opponent(), to)) {
            hitBlot(player.opponent(), to);
        }

        removeFrom(player, from);
        addTo(player, to);
    }

    public void enterFromBar(Player player, int entryPoint) {
        checkIndex(entryPoint);
        if (barCount(player) <= 0) {
            throw new IllegalStateException("Gracz " + player + " nie ma pionków na barze");
        }

        if (isBlot(player.opponent(), entryPoint)) {
            hitBlot(player.opponent(), entryPoint);
        }

        if (player == Player.WHITE) {
            whiteBar--;
        } else {
            blackBar--;
        }
        addTo(player, entryPoint);
    }

    public void bearOff(Player player, int from) {
        checkIndex(from);
        removeFrom(player, from);
        if (player == Player.WHITE) {
            whiteBorneOff++;
        } else {
            blackBorneOff++;
        }
    }

    public boolean allCheckersInHome(Player player) {
        if (barCount(player) > 0) {
            return false;
        }
        for (int i = 0; i < NUM_POINTS; i++) {
            if (!isInHome(player, i) && checkersOf(player, i) > 0) {
                return false;
            }
        }
        return true;
    }

    public boolean isInHome(Player player, int index) {
        checkIndex(index);
        return player == Player.WHITE ? (index <= 5) : (index >= 18);
    }

    public int furthestCheckerInHome(Player player) {
        if (player == Player.WHITE) {
            for (int i = 5; i >= 0; i--) {
                if (checkersOf(player, i) > 0) {
                    return i;
                }
            }
        } else {
            for (int i = 18; i <= 23; i++) {
                if (checkersOf(player, i) > 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    public boolean hasWon(Player player) {
        return borneOffCount(player) == CHECKERS_PER_PLAYERS;
    }

    public boolean isGammon(Player winner) {
        return borneOffCount(winner.opponent()) == 0;
    }

    public boolean isBackgammon(Player winner) {
        Player loser = winner.opponent();
        if (barCount(loser) > 0) {
            return true;
        }
        for (int i = 0; i < NUM_POINTS; i++) {
            if (isInHome(winner, i) && checkersOf(loser, i) > 0) {
                return true;
            }
        }
        return false;
    }

    private void hitBlot(Player opponent, int index) {
        removeFrom(opponent, index);
        if (opponent == Player.WHITE) {
            whiteBar++;
        } else {
            blackBar++;
        }
    }

    private void removeFrom(Player player, int index) {
        if (checkersOf(player, index) <= 0) {
            throw new IllegalStateException("Nie można zdjąć pionka gracza " + player + " z punktu " + index + " - nie ma tam żadnego jego pionka");
        }
        if (player == Player.WHITE) {
            points[index]--;
        } else {
            points[index]++;
        }
    }

    private void addTo(Player player, int index) {
        if (isBlockedFor(player, index)) {
            throw new IllegalStateException("Nie można dostawić pionka gracza " + player + " na punkt " + index + " - jest zablokowany przez przeciwnika");
        }
        if (player == Player.WHITE) {
            points[index]++;
        } else {
            points[index]--;
        }
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= NUM_POINTS) {
            throw new IndexOutOfBoundsException("Nieprawidłowy indeks punktu: " + index);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Bar: W=").append(whiteBar).append(" B=").append(blackBar).append("\n");
        for (int i = NUM_POINTS - 1; i >= 0; i--) {
            sb.append(i).append(": ").append(points[i]).append("\n");
        }
        sb.append("Borne off: W=").append(whiteBorneOff).append(" B=").append(blackBorneOff);
        return sb.toString();
    }
}
