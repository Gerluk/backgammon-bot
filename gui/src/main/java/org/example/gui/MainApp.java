package org.example.gui;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.bots.Bot;
import org.example.bots.MatchRunner;
import org.example.bots.RandomBot;
import org.example.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class MainApp extends Application {
    private static final double CANVAS_WIDTH = 900;
    private static final double CANVAS_HEIGHT = 600;
    private static final double INFO_PANEL_WIDTH = 350;

    private static final Color HIGHLIGHT_SOURCE = Color.LIMEGREEN;
    private static final Color HIGHLIGHT_SELECTED = Color.GOLD;
    private static final Color HIGHLIGHT_DESTINATION = Color.TOMATO;

    private final BoardRenderer renderer = new BoardRenderer();
    private final GameEngine engine = new GameEngine();

    private Canvas canvas;
    private Label statusLabel;
    private Label turnCountLabel;
    private Label borneOffLabel;
    private Label diceLabel;
    private ListView<String> moveLog;
    private ObservableList<String> moveLogItems;
    private int turnCount = 0;
    private Button bearOffButton;

    private MatchRunner matchRunner;
    private Timeline timeline;

    private boolean humanMode = false;
    private Player humanColor;
    private final Bot opponentBot = new RandomBot();
    private Random humanDiceRandom;
    private GameState humanGameState;
    private List<List<Move>> currentTurnCandidates;
    private List<Move> movesMadeThisTurn = new ArrayList<>();
    private GameState turnStartStateForLog;
    private Integer selectedFromIndex;

    @Override
    public void start(Stage stage) {
        canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        canvas.setOnMouseClicked(e -> onBoardClicked(e.getX(), e.getY()));

        statusLabel = new Label("Wybierz tryb rozgrywki poniżej");
        statusLabel.setWrapText(true);
        turnCountLabel = new Label("Tura: 0");
        borneOffLabel = new Label("Zdjęte: białe 0 / czarne 0");
        diceLabel = new Label("Kości: ");

        Button startButton = new Button("Start");
        Button pauseButton = new Button("Pauza");
        Button resumeButton = new Button("Wznów");
        Button nextTurnButton = new Button("Następna tura");

        startButton.setOnAction(e -> startBotVsBotGame());
        pauseButton.setOnAction(e -> pauseBotVsBotGame());
        resumeButton.setOnAction(e -> resumeBotVsBotGame());
        nextTurnButton.setOnAction(e -> playOneTurnManually());

        HBox autoplayButtons = new HBox(8, startButton, pauseButton, resumeButton);
        HBox manualButtons = new HBox(8, nextTurnButton);
        Label botVsBotHeader = new Label("Tryb: Bot vs Bot");

        Button playWhiteButton = new Button("Graj białymi");
        Button playBlackButton = new Button("Graj czarnymi");
        playWhiteButton.setOnAction(e -> startHumanGame(Player.WHITE));
        playBlackButton.setOnAction(e -> startHumanGame(Player.BLACK));

        bearOffButton = new Button("Zdejmij pionek");
        bearOffButton.setDisable(true);
        bearOffButton.setOnAction(e -> handleDestinationChosen(Move.OFF));

        HBox humanModeButtons = new HBox(8, playWhiteButton, playBlackButton);
        Label humanVsBotHeader = new Label("Tryb: Człowiek vs Bot");

        moveLogItems = FXCollections.observableArrayList();
        moveLog = new ListView<>(moveLogItems);
        moveLog.setPrefHeight(300);
        moveLog.setCellFactory(list -> new WrappingLabelCell());
        VBox.setVgrow(moveLog, Priority.ALWAYS);
        Label logHeader = new Label("Historia ruchów:");

        VBox infoPanel = new VBox(10, statusLabel, turnCountLabel, borneOffLabel, diceLabel,
                new Separator(),
                botVsBotHeader, autoplayButtons, manualButtons,
                new Separator(),
                humanVsBotHeader, humanModeButtons, bearOffButton,
                new Separator(),
                logHeader, moveLog);
        infoPanel.setPadding(new Insets(15));
        infoPanel.setAlignment(Pos.TOP_LEFT);
        infoPanel.setPrefWidth(INFO_PANEL_WIDTH);

        BorderPane root = new BorderPane();
        root.setCenter(canvas);
        root.setRight(infoPanel);

        redrawBoard(Board.initialSetup());

        Scene scene = new Scene(root, CANVAS_WIDTH + INFO_PANEL_WIDTH, CANVAS_HEIGHT + 80);
        stage.setTitle("Backgammon");
        stage.setScene(scene);
        stage.show();
    }

    private void startBotVsBotGame() {
        humanMode = false;
        if (timeline != null) {
            timeline.stop();
        }

        matchRunner = new MatchRunner(new RandomBot(), new RandomBot(), new Random());
        turnCount = 0;
        moveLogItems.clear();
        statusLabel.setText("Rozgrywka w toku (automatycznie)...");
        diceLabel.setText("Kości: -");

        redrawBoard(matchRunner.currentState().board());
        updateInfoLabels(matchRunner.currentState().board());

        timeline = new Timeline(new KeyFrame(Duration.millis(750), e -> playOneTurnAuto()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void pauseBotVsBotGame() {
        if (timeline != null) {
            timeline.stop();
        }
        if (matchRunner != null && !matchRunner.isGameOver()) {
            statusLabel.setText("Zatrzymano na turze " + turnCount);
        }
    }

    private void resumeBotVsBotGame() {
        if (humanMode || matchRunner == null || matchRunner.isGameOver()) {
            return;
        }
        statusLabel.setText("Rozgrywka wznowiona (automatycznie)...");
        timeline = new Timeline(new KeyFrame(Duration.millis(750), e -> playOneTurnAuto()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void playOneBotVsBotTurn() {
        GameState stateBeforeTurn = matchRunner.currentState();
        Player mover = stateBeforeTurn.currentPlayer();
        List<Integer> diceForTurn = stateBeforeTurn.remainingDice();

        List<Move> movesPlayed = matchRunner.playNextTurn();
        turnCount++;

        redrawBoard(matchRunner.currentState().board());
        updateInfoLabels(matchRunner.currentState().board());
        diceLabel.setText("Kości: " + formatDice(diceForTurn));
        appendLogEntry(formatTurnLogEntry(turnCount, mover, diceForTurn, movesPlayed));

        if (matchRunner.isGameOver()) {
            if (timeline != null) {
                timeline.stop();
            }
            statusLabel.setText("Koniec gry, zwycięzca: " + formatWinnerDescription(matchRunner.currentState().board(),
                    matchRunner.winner()));
        }
    }

    private void playOneTurnManually() {
        humanMode = false;
        if (timeline != null) {
            timeline.stop();
        }
        if (matchRunner == null || matchRunner.isGameOver()) {
            return;
        }
        playOneBotVsBotTurn();
        if (!matchRunner.isGameOver()) {
            statusLabel.setText("Tryb ręczny - tura " + turnCount);
        }
    }

    private void playOneTurnAuto() {
        if (matchRunner.isGameOver()) {
            timeline.stop();
            return;
        }
        playOneBotVsBotTurn();
    }

    private void startHumanGame(Player color) {
        if (timeline != null) {
            timeline.stop();
        }
        humanMode = true;
        humanColor = color;
        humanDiceRandom = new Random();
        turnCount = 0;
        moveLogItems.clear();
        movesMadeThisTurn = new ArrayList<>();
        selectedFromIndex = null;

        humanGameState = GameState.startingState(Player.WHITE, Dice.roll(humanDiceRandom));

        statusLabel.setText("Grasz jako " + color);
        diceLabel.setText("Kości: " + formatDice(humanGameState.remainingDice()));
        updateInfoLabels(humanGameState.board());
        advanceHumanFlow();
    }

    private void advanceHumanFlow() {
        Player winner = engine.winnerOrNull(humanGameState.board());
        if (winner != null) {
            statusLabel.setText("Koniec gry, zwycięzca: " + formatWinnerDescription(humanGameState.board(), winner));
            redrawBoard(humanGameState.board());
            return;
        }

        if (humanGameState.currentPlayer() == humanColor) {
            currentTurnCandidates = engine.legalFullTurns(humanGameState);
            movesMadeThisTurn = new ArrayList<>();
            selectedFromIndex = null;
            turnStartStateForLog = humanGameState;

            if (isPassForced(currentTurnCandidates)) {
                turnCount++;
                appendLogEntry(formatTurnLogEntry(turnCount, humanColor, humanGameState.remainingDice(), List.of()));
                advanceToNextHumanTurn();
                return;
            }

            statusLabel.setText("Twój ruch - kliknij podświetlony pionek");
            redrawHumanBoard();
        } else {
            statusLabel.setText(humanGameState.currentPlayer() + " (bot) myśli...");
            redrawHumanBoard();
            PauseTransition pause =  new PauseTransition(Duration.millis(500));
            pause.setOnFinished(event -> playBotTurnInHumanMode());
            pause.play();
        }
    }

    private boolean isPassForced(List<List<Move>> candidates) {
        return candidates.size() == 1 && candidates.getFirst().isEmpty();
    }

    private void playBotTurnInHumanMode() {
        GameState before = humanGameState;
        Player mover = before.currentPlayer();
        List<Integer> dice = before.remainingDice();

        List<List<Move>> options = engine.legalFullTurns(before);
        List<Move> chosen = opponentBot.chooseTurn(before, options);

        GameState after = before;
        for (Move move : chosen) {
            after = engine.applyMove(after, move);
        }
        humanGameState = after;
        turnCount++;
        appendLogEntry(formatTurnLogEntry(turnCount, mover, dice, chosen));

        Player winner = engine.winnerOrNull(humanGameState.board());
        if (winner != null) {
            redrawBoard(humanGameState.board());
            updateInfoLabels(humanGameState.board());
            statusLabel.setText("Koniec gry, zwycięzca: " + formatWinnerDescription(humanGameState.board(), winner));
            return;
        }

        advanceToNextHumanTurn();
    }

    private void advanceToNextHumanTurn() {
        humanGameState = humanGameState.withNextTurn(Dice.roll(humanDiceRandom));
        diceLabel.setText("Kości: " + formatDice(humanGameState.remainingDice()));
        redrawBoard(humanGameState.board());
        updateInfoLabels(humanGameState.board());
        advanceHumanFlow();
    }

    private void onBoardClicked(double x, double y) {
        if (!humanMode || humanGameState.currentPlayer() != humanColor) {
            return;
        }
        if (engine.winnerOrNull(humanGameState.board()) != null) {
            return;
        }

        Integer clicked = renderer.pointIndexAt(x, y, CANVAS_WIDTH, CANVAS_HEIGHT);
        if (clicked == null) {
            return;
        }

        if (selectedFromIndex == null) {
            if (validSourcePoints().contains(clicked)) {
                selectedFromIndex = clicked;
                redrawHumanBoard();
            }
            return;
        }

        if (clicked.equals(selectedFromIndex)) {
            selectedFromIndex = null;
            redrawHumanBoard();
            return;
        }

        if (validDestinationsFrom(selectedFromIndex).contains(clicked)) {
            handleDestinationChosen(clicked);
            return;
        }

        if (validSourcePoints().contains(clicked)) {
            selectedFromIndex = clicked;
            redrawHumanBoard();
        }
    }

    private void handleDestinationChosen(int destinationIndex) {
        if (selectedFromIndex == null || !validDestinationsFrom(selectedFromIndex).contains(destinationIndex)) {
            return;
        }

        Move move = new Move(selectedFromIndex, destinationIndex);
        humanGameState = engine.applyMove(humanGameState, move);
        movesMadeThisTurn.add(move);
        selectedFromIndex = null;

        redrawBoard(humanGameState.board());
        updateInfoLabels(humanGameState.board());

        int prefixLen = movesMadeThisTurn.size();
        boolean moreMovesAvailable = currentTurnCandidates.stream()
                .anyMatch(seq -> seq.size() > prefixLen && sameMovePrefix(seq, movesMadeThisTurn));

        if (moreMovesAvailable) {
            redrawHumanBoard();
        } else {
            finalizeHumanTurn();
        }
    }

    private void finalizeHumanTurn() {
        turnCount++;
        appendLogEntry(formatTurnLogEntry(turnCount, humanColor, humanGameState.remainingDice(), movesMadeThisTurn));

        Player winner =  engine.winnerOrNull(humanGameState.board());
        if (winner != null) {
            statusLabel.setText("Koniec gry, zwycięzca: " + formatWinnerDescription(humanGameState.board(), winner));
            redrawBoard(humanGameState.board());
            return;
        }

        advanceToNextHumanTurn();
    }

    private Set<Integer> validSourcePoints() {
        int prefixLen = movesMadeThisTurn.size();
        return currentTurnCandidates.stream()
                .filter(seq -> seq.size() > prefixLen && sameMovePrefix(seq, movesMadeThisTurn))
                .map(seq -> seq.get(prefixLen).from())
                .collect(Collectors.toSet());
    }

    private Set<Integer> validDestinationsFrom(int fromIndex) {
        int prefixLen = movesMadeThisTurn.size();
        return currentTurnCandidates.stream()
                .filter(seq -> seq.size() > prefixLen && sameMovePrefix(seq, movesMadeThisTurn))
                .map(seq -> seq.get(prefixLen))
                .filter(move -> move.from() == fromIndex)
                .map(Move::to)
                .collect(Collectors.toSet());
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

    private void redrawHumanBoard() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        renderer.render(gc, humanGameState.board(), CANVAS_WIDTH, CANVAS_HEIGHT);

        if (humanGameState.currentPlayer() != humanColor) {
            bearOffButton.setDisable(false);
            return;
        }

        if (selectedFromIndex == null) {
            renderer.renderHighlight(gc, validSourcePoints(), CANVAS_WIDTH, CANVAS_HEIGHT, HIGHLIGHT_SOURCE);
            bearOffButton.setDisable(true);
        } else {
            renderer.renderHighlight(gc, Set.of(selectedFromIndex),  CANVAS_WIDTH, CANVAS_HEIGHT, HIGHLIGHT_SELECTED);
            Set<Integer> destinations = validDestinationsFrom(selectedFromIndex);
            renderer.renderHighlight(gc, destinations, CANVAS_WIDTH, CANVAS_HEIGHT, HIGHLIGHT_DESTINATION);
            bearOffButton.setDisable(!destinations.contains(Move.OFF));
        }
    }

    private void redrawBoard(Board board) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        renderer.render(gc, board, CANVAS_WIDTH, CANVAS_HEIGHT);
    }

    private void updateInfoLabels(Board board) {
        turnCountLabel.setText("Tura: " + turnCount);
        borneOffLabel.setText(String.format("Zdjęte: białe %d / czarne %d",
               board.borneOffCount(Player.WHITE), board.borneOffCount(Player.BLACK)));
    }

    private void appendLogEntry(String entry) {
        moveLogItems.add(entry);
        moveLog.scrollTo(moveLogItems.size() - 1);
    }

    private String formatWinnerDescription(Board board, Player winner) {
        if (board.isBackgammon(winner)) {
            return winner + " (backgammon!)";
        }
        if (board.isGammon(winner)) {
            return winner + " (gammon!)";
        }
        return winner.toString();
    }

    private String formatDice(List<Integer> dice) {
        if (dice.isEmpty()) {
            return "-";
        }
        if (dice.size() == 4) {
            return dice.getFirst() + "-" + dice.getFirst() + " (dublet)";
        }
        return dice.getFirst() + "-" + dice.get(1);
    }

    private String formatTurnLogEntry(int turnNumber, Player mover, List<Integer> dice, List<Move> moves) {
        String movesText = moves.isEmpty() ? "brak możliwych ruchów (pas)" : formatMoves(moves);
        return String.format("Tura %d - %s (kości: %s): %s", turnNumber, mover, formatDice(dice), movesText);
    }

    private String formatMoves(List<Move> moves) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < moves.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(formatMove(moves.get(i)));
        }
        return sb.toString();
    }

    private String formatMove(Move move) {
        return pointLabel(move.from()) + "/" + pointLabel(move.to());
    }

    private String pointLabel(int index) {
        if (index == Move.BAR) {
            return "bar";
        }
        if (index == Move.OFF) {
            return "off";
        }
        return String.valueOf(index + 1);
    }

    private static class WrappingLabelCell extends ListCell<String> {
        private final Label label = new Label();

        WrappingLabelCell() {
            label.setWrapText(true);
            label.prefWidthProperty().bind(widthProperty().subtract(25));
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                label.setText(item);
                setGraphic(label);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
