package org.example.bots;

import org.example.model.*;
import org.example.rules.GameEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GameController {

    private final GameEngine engine = new GameEngine();
    private final PlayerConfig whiteConfig;
    private final PlayerConfig blackConfig;
    private final Random diceRandom;

    private GameState state;
    private List<List<Move>> currentTurnCandidates;
    private final List<Move> movesMadeThisTurn = new ArrayList<>();
    private Player turnStartMover;
    private List<Integer> turnStartDice;
    private Integer selectedFrom;
    private Player winner;
    private int turnCount = 0;

    private Runnable onStateChanged;
    private Consumer<String> onLogEntry;

    public GameController(PlayerConfig whiteConfig, PlayerConfig blackConfig, Random diceRandom) {
        this.whiteConfig = whiteConfig;
        this.blackConfig = blackConfig;
        this.diceRandom = diceRandom;
    }

    public void setOnStateChanged(Runnable onStateChanged) {
        this.onStateChanged = onStateChanged;
    }

    public void setOnLogEntry(Consumer<String> onLogEntry) {
        this.onLogEntry = onLogEntry;
    }

    public void start() {
        state = GameState.startingState(Player.WHITE, Dice.roll(diceRandom));
        turnCount = 0;
        winner = null;
        beginTurn();
    }

    public Board board() {
        return state.board();
    }

    public Player currentPlayer() {
        return state.currentPlayer();
    }

    public PlayerConfig configFor(Player player) {
        return player == Player.WHITE ? whiteConfig : blackConfig;
    }

    public int turnCount() {
        return turnCount;
    }

    public String diceText() {
        return formatDice(state.remainingDice());
    }

    public boolean isGameOver() {
        return winner != null;
    }

    public String winnerDescription() {
        if (winner == null) {
            throw new IllegalStateException("Gra jeszcze się nie zakończyła");
        }
        Board board = state.board();
        if (board.isBackgammon(winner)) {
            return winner + " (backgammon!)";
        }
        if (board.isGammon(winner)) {
            return winner + " (gammon!)";
        }
        return winner.toString();
    }

    public String statusText() {
        if (isGameOver()) {
            return "Koniec gry, zwycięzca: " + winnerDescription();
        }
        PlayerConfig config = configFor(state.currentPlayer());
        if (config.isHuman()) {
            return state.currentPlayer() + " (" + config.displayName() + ") - kliknij podświetlony pionek";
        }
        return state.currentPlayer() + " (" + config.displayName() +") myśli...";
    }

    public boolean isBotTurnPending() {
        return !isGameOver() && configFor(state.currentPlayer()).isBot();
    }

    public void playPendingBotTurn() {
        if (!isBotTurnPending()) {
            return;
        }
        Bot bot = configFor(state.currentPlayer()).getBot();
        List<Move> chosen = bot.chooseTurn(state, currentTurnCandidates);
        applyFullTurn(chosen);
    }

    public Set<Integer> validSourcePoints() {
        if (isGameOver()) {
            return Set.of();
        }
        int prefixLen = movesMadeThisTurn.size();
        return currentTurnCandidates.stream()
                .filter(seq -> seq.size() > prefixLen && sameMovePrefix(seq, movesMadeThisTurn))
                .map(seq -> seq.get(prefixLen).from())
                .collect(Collectors.toSet());
    }

    public Set<Integer> validDestinationsFrom(int fromIndex) {
        int prefixLen = movesMadeThisTurn.size();
        return currentTurnCandidates.stream()
                .filter(seq -> seq.size() > prefixLen && sameMovePrefix(seq, movesMadeThisTurn))
                .map(seq -> seq.get(prefixLen))
                .filter(move -> move.from() == fromIndex)
                .map(Move::to)
                .collect(Collectors.toSet());
    }

    public Integer selectedFrom() {
        return selectedFrom;
    }

    public void selectSource(int index) {
        if (isGameOver() || configFor(state.currentPlayer()).isBot()) {
            return;
        }
        if (validSourcePoints().contains(index)) {
            selectedFrom = index;
            fireStateChanged();
        }
    }

    public void deselectSource() {
        if (selectedFrom != null) {
            selectedFrom = null;
            fireStateChanged();
        }
    }

    public void chooseDestination(int to) {
        if (selectedFrom == null || !validDestinationsFrom(selectedFrom).contains(to)) {
            return;
        }

        Move move = new Move(selectedFrom, to);
        state = engine.applyMove(state, move);
        movesMadeThisTurn.add(move);
        selectedFrom = null;

        int prefixLen = movesMadeThisTurn.size();
        boolean moreMovesAvailable = currentTurnCandidates.stream()
                .anyMatch(seq -> seq.size() > prefixLen && sameMovePrefix(seq, movesMadeThisTurn));

        if (moreMovesAvailable) {
            fireStateChanged();
        } else {
            turnCount++;
            log(formatTurnLogEntry(turnCount, turnStartMover, turnStartDice, movesMadeThisTurn));
            winner = engine.winnerOrNull(state.board());
            if (winner != null) {
                fireStateChanged();
                return;
            }
            advanceTurn();
        }
    }

    private void beginTurn() {
        winner = engine.winnerOrNull(state.board());
        if (winner != null) {
            fireStateChanged();
            return;
        }

        currentTurnCandidates = engine.legalFullTurns(state);
        movesMadeThisTurn.clear();
        selectedFrom = null;
        turnStartMover = state.currentPlayer();
        turnStartDice = state.remainingDice();

        if (isPassForced()) {
            turnCount++;
            log(formatTurnLogEntry(turnCount, turnStartMover, turnStartDice, List.of()));
            advanceTurn();
            return;
        }

        fireStateChanged();
    }

    private boolean isPassForced() {
        return currentTurnCandidates.size() == 1 && currentTurnCandidates.getFirst().isEmpty();
    }

    private void applyFullTurn(List<Move> moves) {
        GameState result = state;
        for (Move move : moves) {
            result = engine.applyMove(result, move);
        }
        state = result;
        turnCount++;
        log(formatTurnLogEntry(turnCount, turnStartMover, turnStartDice, moves));

        winner = engine.winnerOrNull(state.board());
        if (winner != null) {
            fireStateChanged();
            return;
        }
        advanceTurn();
    }

    private void advanceTurn() {
        state = state.withNextTurn(Dice.roll(diceRandom));
        beginTurn();
    }

    private boolean sameMovePrefix(List<Move> sequence, List<Move> prefix) {
        if (sequence.size() < prefix.size()) {
            return false;
        }
        for (int i = 0; i < prefix.size(); i++) {
            if (!sequence.get(i).equals(prefix.get(i))) {
                return false;
            }
        }
        return true;
    }

    private void fireStateChanged() {
        if (onStateChanged != null) {
            onStateChanged.run();
        }
    }

    private void log(String entry) {
        if (onLogEntry != null) {
            onLogEntry.accept(entry);
        }
    }

    private static String formatDice(List<Integer> dice) {
        if (dice.isEmpty()) {
            return "-";
        }
        if (dice.size() == 4) {
            return dice.getFirst() + "-" + dice.getFirst() + " (dublet)";
        }
        return dice.getFirst() + "-" + dice.get(1);
    }

    private static String formatTurnLogEntry(int turnNumber, Player mover, List<Integer> dice, List<Move> moves) {
        String movesText = moves.isEmpty() ? "brak możliwych ruchów (pas)" : formatMoves(moves);
        return String.format("Tura %d - %s (kości: %s): %s", turnNumber, mover, formatDice(dice), movesText);
    }

    private static String formatMoves(List<Move> moves) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < moves.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(formatMove(moves.get(i)));
        }
        return sb.toString();
    }

    private static String formatMove(Move move) {
        return pointLabel(move.from()) + "/" + pointLabel(move.to());
    }

    private static String pointLabel(int index) {
        if (index == Move.BAR) {
            return "bar";
        }
        if (index == Move.OFF) {
            return "off";
        }
        return String.valueOf(index + 1);
    }
}
