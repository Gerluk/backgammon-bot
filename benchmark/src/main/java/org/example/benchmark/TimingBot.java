package org.example.benchmark;

import org.example.bots.Bot;
import org.example.model.GameState;
import org.example.model.Move;

import java.util.List;

public class TimingBot implements Bot {

    private final Bot delegate;
    private long totalNanos = 0;
    private int decisionCount = 0;

    public TimingBot(Bot delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<Move> chooseTurn(GameState state, List<List<Move>> availableTurns) {
        long start = System.nanoTime();
        List<Move> result = delegate.chooseTurn(state, availableTurns);
        totalNanos += System.nanoTime() - start;
        decisionCount++;
        return result;
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    public long totalNanos() {
        return totalNanos;
    }

    public int decisionCount() {
        return decisionCount;
    }
}
