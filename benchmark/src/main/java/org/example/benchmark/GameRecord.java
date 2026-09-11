package org.example.benchmark;

import org.example.model.Player;

public record GameRecord(
        String matchupId,
        int gameIndex,
        String sideALabel,
        String sideBLabel,
        Player sideAColor,
        String winnerSide,
        boolean gammon,
        boolean backgammon,
        int turnCount,
        double sideATotalDecisionMillis,
        int sideADecisionCount,
        double sideBTotalDecisionMillis,
        int sideBDecisionCount
) {}