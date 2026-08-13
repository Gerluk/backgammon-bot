package org.example.gui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.bots.MatchRunner;
import org.example.bots.RandomBot;
import org.example.model.GameState;
import org.example.model.Move;
import org.example.model.Player;

import java.util.List;
import java.util.Random;

public class MainApp extends Application {
    private static final double CANVAS_WIDTH = 900;
    private static final double CANVAS_HEIGHT = 600;

    private final BoardRenderer renderer = new BoardRenderer();
    private MatchRunner matchRunner;
    private Timeline timeline;

    private Canvas canvas;
    private Label statusLabel;
    private Label turnCountLabel;
    private Label borneOffLabel;
    private int turnCount = 0;

    @Override
    public void start(Stage stage) {
        canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);

        statusLabel = new Label("Naciśnij \"Start\", aby rozpocząć rozgrywkę");
        turnCountLabel = new Label("Tura: 0");
        borneOffLabel = new Label("Zdjęte: białe 0 / czarne 0");

        Button startButton = new Button("Start");
        Button stopButton = new Button("Stop");
        startButton.setOnAction(e -> startNewGame());
        stopButton.setOnAction(e -> stopGame());

        VBox infoPanel = new VBox(10, statusLabel, turnCountLabel, borneOffLabel, startButton, stopButton);
        infoPanel.setPadding(new Insets(15));
        infoPanel.setAlignment(Pos.TOP_LEFT);
        infoPanel.setPrefWidth(220);

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
        statusLabel.setText("Rozgrywka w toku...");

        timeline = new Timeline(new KeyFrame(Duration.millis(500), e -> playOneTurn()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void stopGame() {
        if (timeline != null) {
            timeline.stop();
        }
        statusLabel.setText("Zatrzymano");
    }

    private void playOneTurn() {
        if (matchRunner.isGameOver()) {
            timeline.stop();
            Player winner = matchRunner.winner();
            statusLabel.setText("Koniec gry, zwycięzca: " + winner);
            return;
        }

        GameState stateBeforeTurn = matchRunner.currentState();
        Player mover = stateBeforeTurn.currentPlayer();

        List<Move> movesPlayed = matchRunner.playNextTurn();
        turnCount++;

        GraphicsContext gc = canvas.getGraphicsContext2D();
        renderer.render(gc, matchRunner.currentState().board(), CANVAS_WIDTH, CANVAS_HEIGHT);

        turnCountLabel.setText("Tura: " + turnCount);
        borneOffLabel.setText(String.format("Zdjęte: białe %d / czarne %d",
                matchRunner.currentState().board().borneOffCount(Player.WHITE),
                matchRunner.currentState().board().borneOffCount(Player.BLACK)));
        statusLabel.setText(mover + " zagrał: " + movesPlayed);

        if (matchRunner.isGameOver()) {
            timeline.stop();
            statusLabel.setText("Koniec gry, zwycięzca: " + matchRunner.winner());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
