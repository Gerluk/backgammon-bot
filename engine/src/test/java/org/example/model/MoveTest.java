package org.example.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MoveTest {

    @Test
    void constantsHaveExpectedValues() {
        assertEquals(-1, Move.BAR);
        assertEquals(-2, Move.OFF);
    }

    @Test
    void isEnteringFromBarTrueWhenFromIsBar() {
        Move move = new Move(Move.BAR, 5);
        assertTrue(move.isEnteringFromBar());
    }

    @Test
    void isEnteringFromBarFalseForOrdinaryMove() {
        Move move = new Move(10, 5);
        assertFalse(move.isEnteringFromBar());
    }

    @Test
    void isEnteringFromBarFalseWhenFromIsOff() {
        Move move = new Move(Move.OFF, 5);
        assertFalse(move.isEnteringFromBar());
    }

    @Test
    void isEnteringFromBarFalseForArbitraryNegativeValue() {
        Move move = new Move(-13, 5);
        assertFalse(move.isEnteringFromBar());
    }

    @Test
    void isBearingOffTrueWhenToIsOff() {
        Move move = new Move(3, Move.OFF);
        assertTrue(move.isBearingOff());
    }

    @Test
    void isBearingOffFalseWhenToIsBar() {
        Move move = new Move(3, Move.BAR);
        assertFalse(move.isBearingOff());
    }

    @Test
    void isBearingOffFalseForOrdinaryMove() {
        Move  move = new Move(10, 5);
        assertFalse(move.isBearingOff());
    }

    @Test
    void bothFlagsCanBeTrueSimultaneously() {
        Move move = new Move(Move.BAR, Move.OFF);
        assertTrue(move.isEnteringFromBar());
        assertTrue(move.isBearingOff());
    }

    @Test
    void sameFromAndToAreEqual() {
        assertEquals(new Move(3, 7),  new Move(3, 7));
    }

    @Test
    void differentFromAreNotEqual() {
        assertNotEquals(new Move(3, 7),  new Move(4, 7));
    }

    @Test
    void differentToAreNotEqual() {
        assertNotEquals(new Move(3, 7),  new Move(3, 8));
    }

    @Test
    void accessorsReturnCorrectValues() {
        Move move = new Move(12, 6);
        assertEquals(12, move.from());
        assertEquals(6, move.to());
    }

    @Test
    void bothFlagsFalseForOrdinaryMove() {
        Move move = new Move(12, 6);
        assertFalse(move.isBearingOff());
        assertFalse(move.isEnteringFromBar());
    }
}
