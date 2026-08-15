package org.example.gui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.bots.MatchRunner;
import org.example.bots.RandomBot;
import org.example.model.Board;
import org.example.model.GameState;
import org.example.model.Move;
import org.example.model.Player;

import java.util.List;
import java.util.Random;

public class MainApp extends Application {
    private static final double CANVAS_WIDTH = 900;
    private static final double CANVAS_HEIGHT = 600;
    private static final double INFO_PANEL_WIDTH = 320;

    private final BoardRenderer renderer = new BoardRenderer();
    private MatchRunner matchRunner;
    private Timeline timeline;

    private Canvas canvas;
    private Label statusLabel;
    private Label turnCountLabel;
    private Label borneOffLabel;
    private Label diceLabel;
    private ListView<String> moveLog;
    private ObservableList<String> moveLogItems;
    private int turnCount = 0;

    @Override
    public void start(Stage stage) {
        canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);

        statusLabel = new Label("Naciśnij \"Start\", aby rozpocząć rozgrywkę");
        statusLabel.setWrapText(true);
        turnCountLabel = new Label("Tura: 0");
        borneOffLabel = new Label("Zdjęte: białe 0 / czarne 0");
        diceLabel = new Label("Kości: ");

        Button startButton = new Button("Start");
        Button pauseButton = new Button("Pauza");
        Button resumeButton = new Button("Wznów");
        Button nextTurnButton = new Button("Następna tura");

        startButton.setOnAction(e -> startNewGame());
        pauseButton.setOnAction(e -> pauseGame());
        resumeButton.setOnAction(e -> resumeGame());
        nextTurnButton.setOnAction(e -> playOneTurnManually());

        HBox autoplayButtons = new HBox(8, startButton, pauseButton, resumeButton);
        HBox manualButtons = new HBox(8, nextTurnButton);

        moveLogItems = FXCollections.observableArrayList();
        moveLog = new ListView<>(moveLogItems);
        moveLog.setPrefHeight(300);
        moveLog.setCellFactory(list -> new WrappingLabelCell());
        VBox.setVgrow(moveLog, Priority.ALWAYS);

        Label logHeader = new Label("Historia ruchów:");

        VBox infoPanel = new VBox(10, statusLabel, turnCountLabel, borneOffLabel, diceLabel,
                autoplayButtons, manualButtons, logHeader, moveLog);
        infoPanel.setPadding(new Insets(15));
        infoPanel.setAlignment(Pos.TOP_LEFT);
        infoPanel.setPrefWidth(INFO_PANEL_WIDTH);

        BorderPane root = new BorderPane();
        root.setCenter(canvas);
        root.setRight(infoPanel);

        drawEmptyStartingBoard();

        Scene scene = new Scene(root, CANVAS_WIDTH + infoPanel.getPrefWidth(), CANVAS_HEIGHT);
        stage.setTitle("Backgammon Bot - podgląd rozgrywki");
        stage.setScene(scene);
        stage.show();
    }

    private void drawEmptyStartingBoard() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        renderer.render(gc, org.example.model.Board.initialSetup(), CANVAS_WIDTH, CANVAS_HEIGHT);
    }

    private void startNewGame() {
        if (timeline != null) {
            timeline.stop();
        }

        matchRunner = new MatchRunner(new RandomBot(), new RandomBot(), new Random());
        turnCount = 0;
        moveLogItems.clear();
        statusLabel.setText("Rozgrywka w toku (automatycznie)...");
        diceLabel.setText("Kości: -");

        redrawBoard();
        updateInfoLabels();

        timeline = new Timeline(new KeyFrame(Duration.millis(750), e -> playOneTurnAuto()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void pauseGame() {
        if (timeline != null) {
            timeline.stop();
        }
        if (matchRunner != null && !matchRunner.isGameOver()) {
            statusLabel.setText("Zatrzymano na turze " + turnCount);
        }
    }

    private void resumeGame() {
        if (matchRunner == null || matchRunner.isGameOver()) {
            return;
        }
        statusLabel.setText("Rozgrywka wznowiona (automatycznie)...");
        timeline = new Timeline(new KeyFrame(Duration.millis(750), e -> playOneTurnAuto()));
        timeline.play();
    }

    private void playOneTurn() {
        GameState stateBeforeTurn = matchRunner.currentState();
        Player mover = stateBeforeTurn.currentPlayer();
        List<Integer> diceForTurn = stateBeforeTurn.remainingDice();

        List<Move> movesPlayed = matchRunner.playNextTurn();
        turnCount++;

        redrawBoard();
        updateInfoLabels();
        diceLabel.setText("Kości: " + formatDice(diceForTurn));
        appendLogEntry(formatTurnLogEntry(turnCount, mover, diceForTurn, movesPlayed));

        if (matchRunner.isGameOver()) {
            if (timeline != null) {
                timeline.stop();
            }
            statusLabel.setText("Koniec gry, zwycięzca: " + formatWinnerDescription());
        }
    }

    private void playOneTurnManually() {
        if (timeline != null) {
            timeline.stop();
        }
        if (matchRunner == null || matchRunner.isGameOver()) {
            return;
        }
        playOneTurn();
        if (!matchRunner.isGameOver()) {
            statusLabel.setText("Tryb ręczny - tura " + turnCount);
        }
    }

    private void playOneTurnAuto() {
        if (matchRunner.isGameOver()) {
            timeline.stop();
            return;
        }
        playOneTurn();
    }

    private void redrawBoard() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        renderer.render(gc, matchRunner.currentState().board(), CANVAS_WIDTH, CANVAS_HEIGHT);
    }

    private void updateInfoLabels() {
        turnCountLabel.setText("Tura: " + turnCount);
        borneOffLabel.setText(String.format("Zdjęte: białe %d / czarne %d",
                matchRunner.currentState().board().borneOffCount(Player.WHITE),
                matchRunner.currentState().board().borneOffCount(Player.BLACK)));
    }

    private void appendLogEntry(String entry) {
        moveLogItems.add(entry);
        moveLog.scrollTo(moveLogItems.size() - 1);
    }

    private String formatWinnerDescription() {
        Player winner = matchRunner.winner();
        Board board = matchRunner.currentState().board();

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
