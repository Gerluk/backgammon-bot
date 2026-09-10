package org.example.gui;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.bots.*;
import org.example.bots.strategy.ExpectiminimaxBot;
import org.example.bots.strategy.MonteCarloBot;
import org.example.model.*;

import java.util.Random;
import java.util.Set;

public class MainApp extends Application {
    private static final double CANVAS_WIDTH = 900;
    private static final double CANVAS_HEIGHT = 600;
    private static final double INFO_PANEL_WIDTH = 350;

    private static final Color HIGHLIGHT_SOURCE = Color.LIMEGREEN;
    private static final Color HIGHLIGHT_SELECTED = Color.GOLD;
    private static final Color HIGHLIGHT_DESTINATION = Color.TOMATO;
    private static final String BEAR_OFF_HIGHLIGHT_STYLE =
            "-fx-border-color: tomato; -fx-border-width: 3; -fx-border-radius: 4; -fx-background-radius: 4;";

    private final BoardRenderer renderer = new BoardRenderer();

    private StackPane rootStack;
    private BorderPane gameView;
    private VBox menuView;

    private Canvas canvas;
    private Label statusLabel;
    private Label turnCountLabel;
    private Label borneOffLabel;
    private Label diceLabel;
    private ListView<String> moveLog;
    private ObservableList<String> moveLogItems;
    private Button bearOffButton;
    private HBox autoplayControls;

    private ComboBox<ParticipantType> whiteSelector;
    private ComboBox<ParticipantType> blackSelector;
    private Label menuErrorLabel;
    private Spinner<Integer> whiteDepth;
    private Spinner<Integer> blackDepth;
    private Spinner<Integer> whiteSimulations;
    private Spinner<Integer> blackSimulations;
    private VBox whiteParamsBox;
    private VBox blackParamsBox;

    private GameController controller;
    private boolean bothBots;
    private boolean autoplayPaused;
    private PauseTransition pendingBotPause;

    @Override
    public void start(Stage stage) {
        menuView = buildMenuView();
        gameView = buildGameView();
        gameView.setVisible(false);
        gameView.setManaged(false);

        rootStack = new StackPane(menuView, gameView);

        Scene scene = new Scene(rootStack, CANVAS_WIDTH + INFO_PANEL_WIDTH, CANVAS_HEIGHT + 80);
        stage.setTitle("Backgammon");
        stage.setScene(scene);
        stage.show();
    }

    private VBox buildMenuView() {
        Label title = new Label("Backgammon");
        title.setFont(Font.font("System", 28));

        whiteSelector = new ComboBox<>(FXCollections.observableArrayList(ParticipantType.values()));
        whiteSelector.getSelectionModel().select(ParticipantType.RANDOM_BOT);
        blackSelector = new ComboBox<>(FXCollections.observableArrayList(ParticipantType.values()));
        blackSelector.getSelectionModel().select(ParticipantType.RANDOM_BOT);

        whiteDepth = new Spinner<>(0, 4, 1);
        whiteDepth.setEditable(true);
        whiteDepth.setPrefWidth(70);

        blackDepth = new Spinner<>(0, 4, 1);
        blackDepth.setEditable(true);
        blackDepth.setPrefWidth(70);

        whiteSimulations = new Spinner<>(5, 500, 50, 5);
        whiteSimulations.setEditable(true);
        whiteSimulations.setPrefWidth(80);

        blackSimulations = new Spinner<>(5, 500, 50, 5);
        blackSimulations.setEditable(true);
        blackSimulations.setPrefWidth(80);

        whiteParamsBox = new VBox(4);
        blackParamsBox = new VBox(4);
        whiteParamsBox.setAlignment(Pos.CENTER);
        blackParamsBox.setAlignment(Pos.CENTER);

        whiteSelector.valueProperty().addListener((obs, old, val) -> updateParamsVisibility(val, whiteParamsBox));
        blackSelector.valueProperty().addListener((obs, old, val) -> updateParamsVisibility(val, blackParamsBox));
        updateParamsVisibility(whiteSelector.getValue(), whiteParamsBox);
        updateParamsVisibility(blackSelector.getValue(), blackParamsBox);

        VBox whiteBox = new VBox(6, new Label("Białe:"), whiteSelector, whiteParamsBox);
        VBox blackBox = new VBox(6, new Label("Czarne: "), blackSelector, blackParamsBox);
        whiteBox.setAlignment(Pos.CENTER);
        blackBox.setAlignment(Pos.CENTER);

        Label vsLabel = new Label("VS");
        vsLabel.setFont(Font.font("System", 20));

        HBox selectors = new HBox(24, whiteBox, vsLabel, blackBox);
        selectors.setAlignment(Pos.CENTER);

        menuErrorLabel = new Label();
        menuErrorLabel.setStyle("-fx-text-fill: #bf2a1b;");

        Button startGameButton = new Button("Rozpocznij grę");
        startGameButton.setOnAction(e -> attemptStartGame());

        VBox menu = new VBox(24, title, selectors, menuErrorLabel, startGameButton);
        menu.setAlignment(Pos.CENTER);
        menu.setPadding(new Insets(40));
        return menu;
    }

    private void updateParamsVisibility(ParticipantType type, VBox paramsBox) {
        boolean isWhiteBox = paramsBox == whiteParamsBox;
        Spinner<Integer> depthSpinner = isWhiteBox ? whiteDepth : blackDepth;
        Spinner<Integer> simsSpinner = isWhiteBox ? whiteSimulations : blackSimulations;

        paramsBox.getChildren().clear();
        if (type == ParticipantType.EXPECTIMINIMAX_BOT) {
            paramsBox.getChildren().addAll(new Label("Głębokość:"), depthSpinner);
        } else if (type == ParticipantType.MONTE_CARLO_BOT) {
            paramsBox.getChildren().addAll(new Label("Symulacje:"), simsSpinner);
        }

        boolean show = type == ParticipantType.EXPECTIMINIMAX_BOT || type == ParticipantType.MONTE_CARLO_BOT;
        paramsBox.setVisible(show);
        paramsBox.setManaged(show);
    }

    private void attemptStartGame() {
        ParticipantType white = whiteSelector.getValue();
        ParticipantType black = blackSelector.getValue();

        if (!white.isAvailable() || !black.isAvailable()) {
            menuErrorLabel.setText("Wybrany typ uczestnika nie jest jeszcze zaimplementowany");
            return;
        }

        menuErrorLabel.setText("");
        startGame(
                buildConfig(white, whiteDepth, whiteSimulations),
                buildConfig(black, blackDepth, blackSimulations));
    }

    private PlayerConfig buildConfig(ParticipantType type, Spinner<Integer> depth, Spinner<Integer> sims) {
        if (type == ParticipantType.EXPECTIMINIMAX_BOT) {
            return PlayerConfig.bot(new ExpectiminimaxBot(depth.getValue()));
        }
        if (type == ParticipantType.MONTE_CARLO_BOT) {
            return PlayerConfig.bot(new MonteCarloBot(sims.getValue()));
        }
        return type.createConfig();
    }

    private BorderPane buildGameView() {
        canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        canvas.setOnMouseClicked(e -> onBoardClicked(e.getX(), e.getY()));

        statusLabel = new Label();
        statusLabel.setWrapText(true);
        turnCountLabel = new Label("Tura: 0");
        borneOffLabel = new Label("Zdjęte: białe 0 / czarne 0");
        diceLabel = new Label("Kości: -");

        Button pauseButton = new Button("Pauza");
        Button resumeButton = new Button("Wznów");
        Button nextTurnButton = new Button("Następna tura");
        pauseButton.setOnAction(e -> pauseAutoplay());
        resumeButton.setOnAction(e -> resumeAutoplay());
        nextTurnButton.setOnAction(e -> stepSingleBotTurn());
        autoplayControls = new HBox(8, pauseButton, resumeButton, nextTurnButton);

        bearOffButton = new Button("Zdejmij pionek");
        bearOffButton.setDisable(true);
        bearOffButton.setOnAction(e -> {
            if (controller != null && controller.selectedFrom() != null) {
                controller.chooseDestination(Move.OFF);
            }
        });

        Button newGameButton = new Button("Nowa gra (powrót do menu)");
        newGameButton.setOnAction(e -> returnToMenu());

        moveLogItems = FXCollections.observableArrayList();
        moveLog = new ListView<>(moveLogItems);
        moveLog.setPrefHeight(250);
        moveLog.setCellFactory(list -> new WrappingLabelCell());
        VBox.setVgrow(moveLog, Priority.ALWAYS);
        Label logHeader = new Label("Historia ruchów:");

        VBox infoPanel = new VBox(10,
                statusLabel, turnCountLabel, borneOffLabel, diceLabel,
                new Separator(),
                autoplayControls, bearOffButton, newGameButton,
                new Separator(),
                logHeader, moveLog);
        infoPanel.setPadding(new Insets(15));
        infoPanel.setAlignment(Pos.TOP_LEFT);
        infoPanel.setPrefWidth(INFO_PANEL_WIDTH);

        BorderPane pane = new BorderPane();
        pane.setCenter(canvas);
        pane.setRight(infoPanel);
        return pane;
    }

    private void startGame(PlayerConfig whiteConfig, PlayerConfig blackConfig) {
        stopPendingBotPause();

        bothBots = whiteConfig.isBot() && blackConfig.isBot();
        autoplayPaused = false;
        autoplayControls.setVisible(bothBots);
        autoplayControls.setManaged(bothBots);
        moveLogItems.clear();

        controller = new GameController(whiteConfig, blackConfig, new Random());
        controller.setOnStateChanged(this::refreshUI);
        controller.setOnLogEntry(this::appendLogEntry);

        showGameView();
        controller.start();
    }

    private void returnToMenu() {
        stopPendingBotPause();
        controller = null;
        showMenuView();
    }

    private void showGameView() {
        menuView.setVisible(false);
        menuView.setManaged(false);
        gameView.setVisible(true);
        gameView.setManaged(true);
    }

    private void showMenuView() {
        gameView.setVisible(false);
        gameView.setManaged(false);
        menuView.setVisible(true);
        menuView.setManaged(true);
    }

    private void pauseAutoplay() {
        autoplayPaused = true;
        stopPendingBotPause();
        if (controller != null) {
            statusLabel.setText("Zatrzymano (tura " + controller.turnCount() + ")");
        }
    }

    private void resumeAutoplay() {
        autoplayPaused = false;
        maybeAdvanceBotTurn();
    }

    private void stepSingleBotTurn() {
        autoplayPaused = true;
        stopPendingBotPause();
        if (controller != null) {
            controller.playPendingBotTurn();
        }
    }

    private void maybeAdvanceBotTurn() {
        stopPendingBotPause();
        if (controller == null || controller.isGameOver() || !controller.isBotTurnPending()) {
            return;
        }
        if (bothBots && autoplayPaused) {
            return;
        }
        pendingBotPause = new PauseTransition(Duration.millis(bothBots ? 750 : 500));
        pendingBotPause.setOnFinished(e -> controller.playPendingBotTurn());
        pendingBotPause.play();
    }

    private void stopPendingBotPause() {
        if (pendingBotPause != null) {
            pendingBotPause.stop();
            pendingBotPause = null;
        }
    }

    private void onBoardClicked(double x, double y) {
        if (controller == null || controller.isGameOver()) {
            return;
        }
        if (controller.configFor(controller.currentPlayer()).isBot()) {
            return;
        }

        Integer clicked = renderer.pointIndexAt(x, y, CANVAS_WIDTH, CANVAS_HEIGHT);
        if (clicked == null) {
            return;
        }

        Integer selected = controller.selectedFrom();

        if (selected == null) {
            controller.selectSource(clicked);
        } else if (clicked.equals(selected)) {
            controller.deselectSource();
        } else if (controller.validDestinationsFrom(selected).contains(clicked)) {
            controller.chooseDestination(clicked);
        } else if (controller.validSourcePoints().contains(clicked)) {
            controller.selectSource(clicked);
        }
    }

    private void refreshUI() {
        redrawBoardWithHighlights();

        Board board = controller.board();
        turnCountLabel.setText("Tura: " + controller.turnCount());
        borneOffLabel.setText(String.format("Zdjęte: białe %d / czarne %d",
                board.borneOffCount(Player.WHITE), board.borneOffCount(Player.BLACK)));
        diceLabel.setText("Kości: " + controller.diceText());
        statusLabel.setText(controller.statusText());

        maybeAdvanceBotTurn();
    }

    private void redrawBoardWithHighlights() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        renderer.render(gc, controller.board(), CANVAS_WIDTH, CANVAS_HEIGHT);

        if (controller.isGameOver() || controller.configFor(controller.currentPlayer()).isBot()) {
            setBearOffAvailable(false);
            return;
        }

        Integer selected = controller.selectedFrom();
        if (selected == null) {
            renderer.renderHighlight(gc, controller.validSourcePoints(), CANVAS_WIDTH, CANVAS_HEIGHT, HIGHLIGHT_SOURCE);
            setBearOffAvailable(false);
        } else {
            renderer.renderHighlight(gc, Set.of(selected), CANVAS_WIDTH, CANVAS_HEIGHT, HIGHLIGHT_SELECTED);
            Set<Integer> destinations = controller.validDestinationsFrom(selected);
            renderer.renderHighlight(gc, destinations, CANVAS_WIDTH, CANVAS_HEIGHT, HIGHLIGHT_DESTINATION);
            setBearOffAvailable(destinations.contains(Move.OFF));
        }
    }

    private void setBearOffAvailable(boolean available) {
        bearOffButton.setDisable(!available);
        bearOffButton.setStyle(available ? BEAR_OFF_HIGHLIGHT_STYLE : "");
    }

    private void appendLogEntry(String entry) {
        moveLogItems.add(entry);
        moveLog.scrollTo(moveLogItems.size() - 1);
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
