package org.example.bots;

import org.example.model.*;
import org.example.rules.GameEngine;

import java.util.List;
import java.util.Random;

public class MatchRunner {
    private final GameEngine engine = new GameEngine();
    private final Bot whiteBot;
    private final Bot blackBot;
    private final Random diceRandom;

    private GameState state;
    private Player winner;

    public MatchRunner(Bot whiteBot, Bot blackBot, Random diceRandom) {
        this.whiteBot = whiteBot;
        this.blackBot = blackBot;
        this.diceRandom = diceRandom;
        this.state = GameState.startingState(Player.WHITE, Dice.roll(diceRandom));
    }

    public GameState currentState() {
        return state;
    }

    public boolean isGameOver() {
        return winner != null;
    }

    public Player winner() {
        return winner;
    }

    public List<Move> playNextTurn() {
        if (isGameOver()) {
            return List.of();
        }

        Bot currentBot = state.currentPlayer() == Player.WHITE ? whiteBot : blackBot;
        List<List<Move>> availableTurns = engine.legalFullTurns(state);
        List<Move> chosenTurn = currentBot.chooseTurn(state, availableTurns);

        GameState afterMoves = state;
        for (Move move : chosenTurn) {
            afterMoves = engine.applyMove(afterMoves, move);
        }

        Player possibleWinner = engine.winnerOrNull(afterMoves.board());
        if (possibleWinner != null) {
            winner = possibleWinner;
            state = afterMoves;
            return chosenTurn;
        }

        state = afterMoves.withNextTurn(Dice.roll(diceRandom));
        return chosenTurn;
    }
}
