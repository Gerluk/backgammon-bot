package org.example.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerTest {

    @Test
    void opponentOfWhiteIsBlack() {
        assertEquals(Player.BLACK, Player.WHITE.opponent());
    }

    @Test
    void opponentOfBlackIsWhite() {
        assertEquals(Player.WHITE, Player.BLACK.opponent());
    }

    @Test
    void opponentAppliedTwiceReturnsOriginal() {
        assertEquals(Player.WHITE, Player.WHITE.opponent().opponent());
        assertEquals(Player.BLACK, Player.BLACK.opponent().opponent());
    }

    @Test
    void opponentNeverEqualsItself() {
        for (Player player : Player.values()) {
            assertNotEquals(player, player.opponent());
        }
    }

    @Test
    void valuesContainsExactlyTwoPlayers() {
        assertEquals(2, Player.values().length);
    }

    @Test
    void valueOfParsesCorrectly() {
        assertEquals(Player.BLACK, Player.valueOf("BLACK"));
        assertEquals(Player.WHITE, Player.valueOf("WHITE"));
    }

    @Test
    void valueOfThrowsForInvalidName() {
        assertThrows(IllegalArgumentException.class, () -> Player.valueOf("RED"));
    }
}
