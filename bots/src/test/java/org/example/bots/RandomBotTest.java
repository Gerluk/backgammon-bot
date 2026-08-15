package org.example.bots;

import org.example.model.Board;
import org.example.model.GameState;
import org.example.model.Move;
import org.example.model.Player;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class RandomBotTest {

    private final GameState dummyState = new GameState(Board.initialSetup(), Player.WHITE, List.of(6, 4));

    @Test
    void chooseTurnReturnsOneOfTheAvailableOptions() {
        RandomBot bot = new RandomBot(new Random());

        List<Move> optionA = List.of(new Move(23, 17));
        List<Move> optionB = List.of(new Move(12, 6));
        List<List<Move>> availableTurns = List.of(optionA, optionB);

        List<Move> chosen = bot.chooseTurn(dummyState, availableTurns);
        assertTrue(availableTurns.contains(chosen));
    }

    @Test
    void chooseTurnSingleOptionAlwaysReturnsIt() {
        RandomBot bot = new RandomBot(new Random());
        List<Move> onlyOption = List.of(new Move(10, 5));
        List<List<Move>> availableTurns = List.of(onlyOption);

        for (int i = 0; i < 10; i++) {
            assertEquals(onlyOption, bot.chooseTurn(dummyState, availableTurns));
        }
    }

    @Test
    void chooseTurnSingleEmptySequenceReturnsEmptyListWithoutThrowing() {
        RandomBot bot = new RandomBot(new Random());
        List<List<Move>> availableTurns = List.of(List.of());

        List<Move> chosen = assertDoesNotThrow(() -> bot.chooseTurn(dummyState, availableTurns));
        assertTrue(chosen.isEmpty());
    }

    @Test
    void chooseTurnWithFixedSeedIsDeterministic() {
        RandomBot bot1 = new RandomBot(new Random(42));
        RandomBot bot2 = new RandomBot(new Random(42));

        List<List<Move>> availableTurns = List.of(
                List.of(new Move(23, 17)), List.of(new Move(12, 6)), List.of(new Move(7, 1)));

        List<Move> chosen1 = bot1.chooseTurn(dummyState, availableTurns);
        List<Move> chosen2 = bot2.chooseTurn(dummyState, availableTurns);

        assertEquals(chosen1, chosen2);
    }

    @Test
    void chooseTurnDifferentSeedsCanProduceDifferentChoices() {
        List<List<Move>> availableTurns = List.of(
                List.of(new Move(23, 17)), List.of(new Move(12, 6)), List.of(new Move(7, 1)));

        Set<List<Move>> distinctChoices = new HashSet<>();
        for (int seed = 0; seed < 100; seed++) {
            RandomBot bot = new RandomBot(new Random(seed));
            distinctChoices.add(bot.chooseTurn(dummyState, availableTurns));
        }

        assertTrue(distinctChoices.size() > 1);
    }

    @Test
    void chooseTurnEventuallySelectsEveryOption() {
        RandomBot bot = new RandomBot(new Random(42));
        List<List<Move>> availableTurns = List.of(
                List.of(new Move(23, 17)), List.of(new Move(12, 6)), List.of(new Move(7, 1)));

        Set<List<Move>> seen = new HashSet<>();
        while (seen.size() < 3) {
            seen.add(bot.chooseTurn(dummyState, availableTurns));
        }

        assertEquals(3, seen.size());
    }

    @Test
    void chooseTurnThrowsExceptionWhenNoOptionsProvided() {
        RandomBot bot = new RandomBot(new Random(42));
        assertThrows(IllegalStateException.class, () -> bot.chooseTurn(dummyState, List.of()));
    }

    @Test
    void constructorWithoutArgumentsUsesInternalRandom() {
        RandomBot bot = new RandomBot();
        List<Move> onlyOption = List.of(new Move(10, 5));
        List<List<Move>> availableTurns = List.of(onlyOption);

        assertDoesNotThrow(() -> bot.chooseTurn(dummyState, availableTurns));
    }

    @Test
    void getNameReturnsRandomBot() {
        assertEquals("RandomBot", new RandomBot().getName());
    }

    @Test
    void implementsBotInterface() {
        Bot bot = new RandomBot();
        assertNotNull(bot);
    }

    @Test
    void chooseTurnWorksWithNullState() {
        RandomBot bot = new RandomBot(new Random(42));
        List<Move> onlyOption = List.of(new Move(10, 5));
        List<List<Move>> availableTurns = List.of(onlyOption);

        assertDoesNotThrow(() -> bot.chooseTurn(null, availableTurns));
    }

    @Test
    void chooseTurnDoesNotMutateInputList() {
        RandomBot bot = new RandomBot(new Random());

        List<Move> optionA = List.of(new Move(23, 17));
        List<Move> optionB = List.of(new Move(12, 6));
        List<List<Move>> availableTurns = List.of(optionA, optionB);

        bot.chooseTurn(dummyState, availableTurns);

        assertEquals(2, availableTurns.size());
        assertEquals(optionA, availableTurns.getFirst());
        assertEquals(optionB, availableTurns.get(1));
    }
}
