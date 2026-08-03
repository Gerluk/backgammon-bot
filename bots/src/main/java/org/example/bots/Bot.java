package org.example.bots;

import org.example.model.GameState;
import org.example.model.Move;

import java.util.List;

public interface Bot {
    List<Move> chooseTurn(GameState state, List<List<Move>> availableTurns);
    String getName();
}
