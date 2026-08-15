package org.example.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class DiceTest {

    @Test
    void constructorAcceptsMinimumValidValues() {
        Dice dice = new Dice(1, 1);
        assertEquals(1, dice.die1());
        assertEquals(1, dice.die2());
    }

    @Test
    void constructorAcceptsMaximumValidValues() {
        Dice dice = new Dice(6, 6);
        assertEquals(6, dice.die1());
        assertEquals(6, dice.die2());
    }

    @Test
    void constructorAcceptsAllValidCombinations() {
        for (int a = 1; a <= 6; a++) {
            for (int b = 1; b <= 6; b++) {
                int finalA = a;
                int finalB = b;
                assertDoesNotThrow(() -> new Dice(finalA, finalB));
            }
        }
    }

    @Test
    void constructorThrowsWhenDie1TooLow() {
        assertThrows(IllegalArgumentException.class, () -> new Dice(0, 3));
    }

    @Test
    void constructorThrowsWhenDie1TooHigh() {
        assertThrows(IllegalArgumentException.class, () -> new Dice(7, 3));
    }

    @Test
    void constructorThrowsWhenDie2TooLow() {
        assertThrows(IllegalArgumentException.class, () -> new Dice(3, 0));
    }

    @Test
    void constructorThrowsWhenDie2TooHigh() {
        assertThrows(IllegalArgumentException.class, () -> new Dice(3, 7));
    }

    @Test
    void constructorThrowsWhenBothDiceInvalid() {
        assertThrows(IllegalArgumentException.class, () -> new Dice(0, 100));
    }

    @Test
    void constructorThrowsWithNegativeValues() {
        assertThrows(IllegalArgumentException.class, () -> new Dice(-1, 3));
    }

    @Test
    void isDoubleTrueWhenDiceEqual() {
        assertTrue(new Dice(4, 4).isDouble());
    }

    @Test
    void isDoubleFalseWhenDiceDiffer() {
        assertFalse(new Dice(4, 5).isDouble());
    }

    @Test
    void isDoubleTrueForAllSixPossibleDoubles() {
        for (int i = 1; i <= 6; i++) {
            assertTrue(new Dice(i, i).isDouble());
        }
    }

    @Test
    void availableStepsOrdinaryRollReturnsBothValuesInOrder() {
        Dice dice = new Dice(6, 4);
        assertEquals(List.of(6, 4), dice.availableSteps());
    }

    @Test
    void availableStepsOrdinaryRollPreservesGivenOrderEvenReversed() {
        Dice dice = new Dice(4, 6);
        assertEquals(List.of(4, 6), dice.availableSteps());
    }

    @Test
    void availableStepsDoubleReturnsFourIdenticalValues() {
        Dice dice = new Dice(3, 3);
        assertEquals(List.of(3, 3, 3, 3), dice.availableSteps());
    }

    @Test
    void availableStepsOrdinaryRollHasSizeTwo() {
        assertEquals(2, new Dice(2, 5).availableSteps().size());
    }

    @Test
    void availableStepsDoubleHasSizeFour() {
        assertEquals(4, new Dice(2, 2).availableSteps().size());
    }

    @Test
    void rollAlwaysProducesValuesInValidRange() {
        Random random = new Random(42);
        for (int i = 0; i < 1000; i++) {
            Dice dice = Dice.roll(random);
            assertTrue(dice.die1() >= 1 && dice.die1() <= 6);
            assertTrue(dice.die2() >= 1 && dice.die2() <= 6);
        }
    }

    @Test
    void rollWithSameSeedIsDeterministic() {
        Dice first = Dice.roll(new Random(42));
        Dice second = Dice.roll(new Random(42));
        assertEquals(first, second);
    }

    @Test
    void rollProducesDoublesEventually() {
        Random random = new Random(42);
        boolean sawDouble = false;
        while (!sawDouble) {
            if (Dice.roll(random).isDouble()) {
                sawDouble = true;
                break;
            }
        }
        assertTrue(sawDouble);
    }

    @Test
    void sameValuesAreEqual() {
        assertEquals(new Dice(3, 5), new Dice(3, 5));
    }

    @Test
    void swappedValuesAreNotEqual() {
        assertNotEquals(new Dice(3, 5), new Dice(5, 3));
    }

    @Test
    void availableStepsOrdinaryRollIsImmutable() {
        List<Integer> steps = new Dice(3, 5).availableSteps();
        assertThrows(UnsupportedOperationException.class, () -> steps.add(1));
    }

    @Test
    void availableStepsDoubleIsImmutable() {
        List<Integer> steps = new Dice(3, 3).availableSteps();
        assertThrows(UnsupportedOperationException.class, () -> steps.add(3));
    }

    @Test
    void rollWithNullRandomThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> Dice.roll(null));
    }
}
