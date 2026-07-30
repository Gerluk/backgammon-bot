package org.example.model;

import java.util.List;

public record GameState(
        Board board,
        Player currentPlayer,
        List<Integer> remainingDice
) {

    public static GameState startingState(Player firstPlayer, Dice dice) {
        return new GameState(Board.initialSetup(), firstPlayer, dice.availableSteps());
    }

    public boolean hasRemainingDice() {
        return !remainingDice.isEmpty();
    }

    public GameState withNextTurn(Dice nextDice) {
        return new GameState(board, currentPlayer.opponent(), nextDice.availableSteps());
    }
}
