package org.example.rules;

import org.example.model.Board;
import org.example.model.GameState;
import org.example.model.Move;
import org.example.model.Player;

import java.util.ArrayList;
import java.util.List;

public class GameEngine {
    public Player winnerOrNull(Board board) {
        if (board.hasWon(Player.WHITE)) {
            return Player.WHITE;
        }
        if (board.hasWon(Player.BLACK)) {
            return Player.BLACK;
        }
        return null;
    }

    public List<Move> legalMovesForStep(GameState state, int dieValue) {
        Board board = state.board();
        Player player = state.currentPlayer();
        List<Move> moves = new ArrayList<>();

        if (board.barCount(player) > 0) {
            int entryPoint = entryPointFromBar(player, dieValue);
            if (!board.isBlockedFor(player, entryPoint)) {
                moves.add(new Move(Move.BAR, entryPoint));
            }
            return moves;
        }

        boolean canBearOff = board.allCheckersInHome(player);

        for (int from = 0; from < Board.NUM_POINTS; from++) {
            if (board.checkersOf(player, from) == 0) {
                continue;
            }
            int to = destinationPoint(player, from, dieValue);
            if (isOnBoard(to)) {
                if (!board.isBlockedFor(player, to)) {
                    moves.add(new Move(from, to));
                }
            } else if (canBearOff) {
                if (isExactBearOff(player, from, dieValue)) {
                    moves.add(new Move(from, Move.OFF));
                } else if (isOvershootBearOff(player, from, dieValue, board)) {
                    moves.add(new Move(from, Move.OFF));
                }
            }
        }

        return moves;
    }

    private boolean isExactBearOff(Player player, int from, int dieValue) {
        int distanceFromEdge = player == Player.WHITE ? (from + 1) : (24 - from);
        return distanceFromEdge == dieValue;
    }

    private boolean isOvershootBearOff(Player player, int from, int dieValue, Board board) {
        int distanceFromEdge = player == Player.WHITE ? (from + 1) : (24 - from);
        if (dieValue <= distanceFromEdge) {
            return false;
        }
        int furthest = board.furthestCheckerInHome(player);
        return furthest == from;
    }

    public List<List<Move>> legalFullTurns(GameState state) {
        List<Integer> dice = state.remainingDice();
        boolean isDouble = dice.size() == 4;

        List<List<Move>> allSequences = new ArrayList<>();

        if (isDouble) {
            collectSequences(state, dice, allSequences, new ArrayList<>());
        } else {
            List<Integer> order1 = dice;
            List<Integer> order2 = List.of(dice.get(1), dice.get(0));

            collectSequences(state, order1, allSequences, new ArrayList<>());
            collectSequences(state, order2, allSequences, new ArrayList<>());
        }

        int maxLength = allSequences.stream().mapToInt(List::size).max().orElse(0);
        List<List<Move>> maximal = allSequences.stream()
                .filter(seq -> seq.size() == maxLength)
                .distinct()
                .toList();

        if (!isDouble && maxLength == 1) {
            int higherDie = Math.max(dice.get(1), dice.get(0));
            List<List<Move>> withHigherDie = maximal.stream()
                    .filter(seq -> usesDieValue(state, seq.getFirst(), higherDie))
                    .toList();
            if (!withHigherDie.isEmpty()) {
                return withHigherDie;
            }
        }

        return maximal;
    }

    private void collectSequences(GameState state, List<Integer> diceOrder, List<List<Move>> results, List<Move> movesSoFar) {
        if (movesSoFar.size() == diceOrder.size()) {
            results.add(new ArrayList<>(movesSoFar));
            return;
        }

        int dieValue = diceOrder.get(movesSoFar.size());
        List<Move> options = legalMovesForStep(state, dieValue);

        if (options.isEmpty()) {
            results.add(new ArrayList<>(movesSoFar));
            return;
        }

        for (Move move : options) {
            GameState nextState = applyMove(state, move);
            movesSoFar.add(move);
            collectSequences(nextState, diceOrder, results, movesSoFar);
            movesSoFar.removeLast();
        }
    }

    private boolean usesDieValue(GameState state, Move move, int dieValue) {
        Player player = state.currentPlayer();
        if (move.isEnteringFromBar()) {
            return entryPointFromBar(player, dieValue) == move.to();
        }
        if (move.isBearingOff()) {
            return isExactBearOff(player, move.from(), dieValue) || isOvershootBearOff(player, move.from(), dieValue, state.board());
        }
        return destinationPoint(player, move.from(), dieValue) == move.to();
    }

    public GameState applyMove(GameState state, Move move) {
        Board newBoard = state.board().copy();
        Player player = state.currentPlayer();

        if (move.isEnteringFromBar()) {
            newBoard.enterFromBar(player, move.to());
        } else if (move.isBearingOff()) {
            newBoard.bearOff(player, move.from());
        } else {
            newBoard.movePiece(player, move.from(), move.to());
        }

        return new GameState(newBoard, player, state.remainingDice());
    }

    private int entryPointFromBar(Player player, int dieValue) {
        return player == Player.WHITE ? (24 - dieValue) : (dieValue - 1);
    }

    private int destinationPoint(Player player, int from, int dieValue) {
        return player == Player.WHITE ? (from - dieValue) : (from + dieValue);
    }

    private boolean isOnBoard(int index) {
        return index >= 0 && index < Board.NUM_POINTS;
    }
}
