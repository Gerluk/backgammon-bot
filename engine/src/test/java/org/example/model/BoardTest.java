package org.example.model;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    @Test
    void initialSetupHasCorrectStandardPosition() {
        Board board = Board.initialSetup();

        assertEquals(2, board.checkersOf(Player.WHITE, 23));
        assertEquals(5, board.checkersOf(Player.WHITE, 12));
        assertEquals(3, board.checkersOf(Player.WHITE, 7));
        assertEquals(5, board.checkersOf(Player.WHITE, 5));

        assertEquals(2, board.checkersOf(Player.BLACK, 0));
        assertEquals(5, board.checkersOf(Player.BLACK, 11));
        assertEquals(3, board.checkersOf(Player.BLACK, 16));
        assertEquals(5, board.checkersOf(Player.BLACK, 18));
    }

    @Test
    void initialSetupTotalCheckersPerPlayerIsFifteen() {
        Board board = Board.initialSetup();
        int whiteTotal = 0;
        int blackTotal = 0;
        for (int i = 0; i < Board.NUM_POINTS; i++) {
            whiteTotal += board.checkersOf(Player.WHITE, i);
            blackTotal += board.checkersOf(Player.BLACK, i);
        }
        assertEquals(15, whiteTotal);
        assertEquals(15, blackTotal);
    }

    @Test
    void initialSetupNoCheckersOnBarOrBorneOff() {
        Board board = Board.initialSetup();
        assertEquals(0, board.barCount(Player.WHITE));
        assertEquals(0, board.barCount(Player.BLACK));
        assertEquals(0, board.borneOffCount(Player.WHITE));
        assertEquals(0, board.borneOffCount(Player.BLACK));
    }

    @Test
    void initialSetupAllOtherPointsAreEmpty() {
        Board board = Board.initialSetup();
        Set<Integer> occupied = Set.of(23, 12, 7, 5, 0, 11, 16, 18);

        for (int i = 0; i < Board.NUM_POINTS; i++) {
            if (!occupied.contains(i)) {
                assertTrue(board.isEmpty(i));
            }
        }
    }

    @Test
    void emptyHasNoCheckersAnywhere() {
        Board board = Board.empty();
        for (int i = 0; i < Board.NUM_POINTS; i++) {
            assertTrue(board.isEmpty(i));
        }
        assertEquals(0, board.barCount(Player.WHITE));
        assertEquals(0, board.barCount(Player.BLACK));
        assertEquals(0, board.borneOffCount(Player.WHITE));
        assertEquals(0, board.borneOffCount(Player.BLACK));
    }

    @Test
    void customPositionReflectsGivenValues() {
        int[] points = new int[Board.NUM_POINTS];
        points[0] = 3;
        points[23] = -4;
        Board board = Board.customPosition(points, 2, 1, 5, 6);

        assertEquals(3, board.checkersOf(Player.WHITE, 0));
        assertEquals(4, board.checkersOf(Player.BLACK, 23));
        assertEquals(2, board.barCount(Player.WHITE));
        assertEquals(1, board.barCount(Player.BLACK));
        assertEquals(5, board.borneOffCount(Player.WHITE));
        assertEquals(6, board.borneOffCount(Player.BLACK));
    }

    @Test
    void customPositionThrowsWhenArrayTooShort() {
        int[] tooShort = new int[Board.NUM_POINTS - 1];
        assertThrows(IllegalArgumentException.class,
                () -> Board.customPosition(tooShort, 0, 0, 0, 0));
    }

    @Test
    void customPositionThrowsWhenArrayTooLong() {
        int[] tooLong = new int[Board.NUM_POINTS + 1];
        assertThrows(IllegalArgumentException.class,
                () -> Board.customPosition(tooLong, 0, 0, 0, 0));
    }

    @Test
    void customPositionDoesNotAliasInputArray() {
        int[] points = new int[Board.NUM_POINTS];
        points[0] = 5;
        Board board = Board.customPosition(points, 0, 0, 0, 0);
        points[0] = 99;
        assertEquals(5, board.checkersOf(Player.WHITE, 0));
    }

    @Test
    void copyProducesIndependentDeepCopy() {
        Board original = Board.initialSetup();
        Board copy = original.copy();

        copy.movePiece(Player.WHITE, 23, 20);

        assertEquals(2, original.checkersOf(Player.WHITE, 23));
        assertEquals(0, original.checkersOf(Player.WHITE, 20));
        assertEquals(1, copy.checkersOf(Player.WHITE, 23));
        assertEquals(1, copy.checkersOf(Player.WHITE, 20));
    }

    @Test
    void copyPreservesBarAndBorneOffCounts() {
        int[] points = new int[Board.NUM_POINTS];
        Board original = Board.customPosition(points, 3, 2, 4, 1);
        Board copy = original.copy();

        assertEquals(3, copy.barCount(Player.WHITE));
        assertEquals(2, copy.barCount(Player.BLACK));
        assertEquals(4, copy.borneOffCount(Player.WHITE));
        assertEquals(1, copy.borneOffCount(Player.BLACK));
    }

    @Test
    void mutatingCopyBarDoesNotAffectOriginal() {
        int[] points = new int[Board.NUM_POINTS];
        Board original = Board.customPosition(points, 1, 0, 0, 0);
        Board copy = original.copy();

        copy.enterFromBar(Player.WHITE, 3);

        assertEquals(1, original.barCount(Player.WHITE));
        assertEquals(0, copy.barCount(Player.WHITE));
    }

    @Test
    void pointValuePositiveForWhiteNegativeForBlack() {
        int[] points = new int[Board.NUM_POINTS];
        points[3] = 4;
        points[7] = -2;
        Board board = Board.customPosition(points, 0, 0, 0, 0);

        assertEquals(4, board.pointValue(3));
        assertEquals(-2, board.pointValue(7));
        assertEquals(0, board.pointValue(10));
    }

    @Test
    void checkersOfReturnsZeroForOpponentOnOccupiedPoint() {
        int[] points = new int[Board.NUM_POINTS];
        points[5] = 3;
        Board board = Board.customPosition(points, 0, 0, 0, 0);

        assertEquals(3, board.checkersOf(Player.WHITE, 5));
        assertEquals(0, board.checkersOf(Player.BLACK, 5));
    }

    @Test
    void checkersOfNeverNegative() {
        int[] points = new int[Board.NUM_POINTS];
        points[5] = -4;
        Board board = Board.customPosition(points, 0, 0, 0, 0);

        assertEquals(4, board.checkersOf(Player.BLACK, 5));
        assertTrue(board.checkersOf(Player.WHITE, 5) >= 0);
    }

    @Test
    void isEmptyFalseWhenOccupiedByEitherPlayer() {
        int[] points = new int[Board.NUM_POINTS];
        points[3] = 1;
        points[4] = -1;
        Board board = Board.customPosition(points, 0, 0, 0, 0);

        assertFalse(board.isEmpty(3));
        assertFalse(board.isEmpty(4));
    }

    @Test
    void pointValueThrowsForNegativeIndex() {
        Board board = Board.empty();
        assertThrows(IndexOutOfBoundsException.class, () -> board.pointValue(-1));
    }

    @Test
    void pointValueThrowsForIndexEqualToNumPoints() {
        Board board = Board.empty();
        assertThrows(IndexOutOfBoundsException.class, () -> board.pointValue(Board.NUM_POINTS));
    }

    @Test
    void pointValueAcceptsBoundaryIndices() {
        Board board = Board.empty();
        assertDoesNotThrow(() -> board.pointValue(0));
        assertDoesNotThrow(() -> board.pointValue(Board.NUM_POINTS - 1));
    }

    @Test
    void checkersOfThrowsForOutOfBoundsIndex() {
        Board board = Board.empty();
        assertThrows(IndexOutOfBoundsException.class, () -> board.checkersOf(Player.WHITE, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> board.checkersOf(Player.WHITE, 24));
    }

    @Test
    void isEmptyThrowsForOutOfBoundsIndex() {
        Board board = Board.empty();
        assertThrows(IndexOutOfBoundsException.class, () -> board.isEmpty(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> board.isEmpty(24));
    }

    @Test
    void isBlockedForThrowsForOutOfBoundsIndex() {
        Board board = Board.empty();
        assertThrows(IndexOutOfBoundsException.class, () -> board.isBlockedFor(Player.WHITE, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> board.isBlockedFor(Player.WHITE, 24));
    }

    @Test
    void isBlotThrowsForOutOfBoundsIndex() {
        Board board = Board.empty();
        assertThrows(IndexOutOfBoundsException.class, () -> board.isBlot(Player.WHITE, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> board.isBlot(Player.WHITE, 24));
    }

    @Test
    void isInHomeThrowsForOutOfBoundsIndex() {
        Board board = Board.empty();
        assertThrows(IndexOutOfBoundsException.class, () -> board.isInHome(Player.WHITE, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> board.isInHome(Player.WHITE, 24));
    }

    @Test
    void movePieceThrowsForOutOfBoundsFromOrTo() {
        Board board = Board.customPosition(new int[Board.NUM_POINTS], 0, 0, 0, 0);
        assertThrows(IndexOutOfBoundsException.class, () -> board.movePiece(Player.WHITE, -1, 5));
        assertThrows(IndexOutOfBoundsException.class, () -> board.movePiece(Player.WHITE, 20, 24));
    }

    @Test
    void enterFromBarThrowsForOutOfBoundsEntryPoint() {
        Board board = Board.customPosition(new int[Board.NUM_POINTS], 2, 0, 0, 0);
        assertThrows(IndexOutOfBoundsException.class, () -> board.enterFromBar(Player.WHITE, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> board.enterFromBar(Player.WHITE, 24));
    }

    @Test
    void bearOffThrowsForOutOfBoundsFrom() {
        Board board = Board.empty();
        assertThrows(IndexOutOfBoundsException.class, () -> board.bearOff(Player.WHITE, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> board.bearOff(Player.WHITE, 24));
    }

    @Test
    void isBlockedForWhiteBlockedAtExactlyMinusTwo() {
        int[] points = new int[Board.NUM_POINTS];
        points[5] = -2;
        Board board = Board.customPosition(points, 0, 0, 0, 0);
        assertTrue(board.isBlockedFor(Player.WHITE, 5));
    }

    @Test
    void isBlockedForWhiteBlockedForMoreThanTwo() {
        int[] points = new int[Board.NUM_POINTS];
        points[5] = -5;
        Board board = Board.customPosition(points, 0, 0, 0, 0);
        assertTrue(board.isBlockedFor(Player.WHITE, 5));
    }

    @Test
    void isBlockedForWhiteNotBlockedAtExactlyMinusOne() {
        int[] points = new int[Board.NUM_POINTS];
        points[5] = -1;
        Board board = Board.customPosition(points, 0, 0, 0, 0);
        assertFalse(board.isBlockedFor(Player.WHITE, 5));
    }

    @Test
    void isBlockedForWhiteNotBlockedOnEmptyPoint() {
        Board board = Board.empty();
        assertFalse(board.isBlockedFor(Player.WHITE, 5));
    }

    @Test
    void isBlockedForWhiteNotBlockedOnOwnCheckers() {
        int[] points = new int[Board.NUM_POINTS];
        points[5] = 10;
        Board board = Board.customPosition(points, 0, 0, 0, 0);
        assertFalse(board.isBlockedFor(Player.WHITE, 5));
    }

    @Test
    void isBlockedForBlackBlockedAtExactlyTwo() {
        int[] points = new int[Board.NUM_POINTS];
        points[5] = 2;
        Board board = Board.customPosition(points, 0, 0, 0, 0);
        assertTrue(board.isBlockedFor(Player.BLACK, 5));
    }

    @Test
    void isBlockedForBlackNotBlockedAtExactlyOne() {
        int[] points = new int[Board.NUM_POINTS];
        points[5] = 1;
        Board board = Board.customPosition(points, 0, 0, 0, 0);
        assertFalse(board.isBlockedFor(Player.BLACK, 5));
    }

    @Test
    void isBlotWhiteTrueWhenExactlyOneWhiteChecker() {
        int[] points = new int[Board.NUM_POINTS];
        points[5] = 1;
        Board board = Board.customPosition(points, 0, 0, 0, 0);
        assertTrue(board.isBlot(Player.WHITE, 5));
    }

    @Test
    void isBlotWhiteFalseWhenTwoWhiteCheckers() {
        int[] points = new int[Board.NUM_POINTS];
        points[5] = 2;
        Board board = Board.customPosition(points, 0, 0, 0, 0);
        assertFalse(board.isBlot(Player.WHITE, 5));
    }

    @Test
    void isBlotWhiteFalseWhenEmpty() {
        Board board = Board.empty();
        assertFalse(board.isBlot(Player.WHITE, 5));
    }

    @Test
    void isBlotWhiteFalseWhenOccupiedByBlack() {
        int[] points = new int[Board.NUM_POINTS];
        points[5] = -1;
        Board board = Board.customPosition(points, 0, 0, 0, 0);
        assertFalse(board.isBlot(Player.WHITE, 5));
    }

    @Test
    void isBlotBlackTrueWhenExactlyOneBlackChecker() {
        int[] points = new int[Board.NUM_POINTS];
        points[5] = -1;
        Board board = Board.customPosition(points, 0, 0, 0, 0);
        assertTrue(board.isBlot(Player.BLACK, 5));
    }

    @Test
    void isBlot_black_falseWhenTwoBlackCheckers() {
        int[] points = new int[Board.NUM_POINTS];
        points[5] = -2;
        Board board = Board.customPosition(points, 0, 0, 0, 0);
        assertFalse(board.isBlot(Player.BLACK, 5));
    }

    @Test
    void isBlotBlackFalseWhenOccupiedByWhite() {
        int[] points = new int[Board.NUM_POINTS];
        points[5] = 1;
        Board board = Board.customPosition(points, 0, 0, 0, 0);
        assertFalse(board.isBlot(Player.BLACK, 5));
    }

    @Test
    void movePieceToEmptyPointMovesCorrectly() {
        int[] points = new int[Board.NUM_POINTS];
        points[10] = 1;
        Board board = Board.customPosition(points, 0, 0, 0, 0);

        board.movePiece(Player.WHITE, 10, 5);

        assertEquals(0, board.checkersOf(Player.WHITE, 10));
        assertEquals(1, board.checkersOf(Player.WHITE, 5));
    }

    @Test
    void movePieceToOwnOccupiedPointStacksCheckers() {
        int[] points = new int[Board.NUM_POINTS];
        points[10] = 1;
        points[5] = 2;
        Board board = Board.customPosition(points, 0, 0, 0, 0);

        board.movePiece(Player.WHITE, 10, 5);

        assertEquals(0, board.checkersOf(Player.WHITE, 10));
        assertEquals(3, board.checkersOf(Player.WHITE, 5));
    }

    @Test
    void movePieceOntoOpponentBlotHitsIt() {
        int[] points = new int[Board.NUM_POINTS];
        points[15] = 1;
        points[10] = -1;
        Board board = Board.customPosition(points, 0, 0, 0, 0);

        board.movePiece(Player.WHITE, 15, 10);

        assertEquals(0, board.checkersOf(Player.BLACK, 10));
        assertEquals(1, board.checkersOf(Player.WHITE, 10));
        assertEquals(1, board.barCount(Player.BLACK));
    }

    @Test
    void movePieceBlackHittingWhiteBlotIncrementsWhiteBar() {
        int[] points = new int[Board.NUM_POINTS];
        points[10] = -1;
        points[15] = 1;
        Board board = Board.customPosition(points, 0, 0, 0, 0);

        board.movePiece(Player.BLACK, 10, 15);

        assertEquals(0, board.checkersOf(Player.WHITE, 15));
        assertEquals(1, board.checkersOf(Player.BLACK, 15));
        assertEquals(1, board.barCount(Player.WHITE));
    }

    @Test
    void pointWithTwoCheckersIsBlockedAndIsNotBlot() {
        int[] points = new int[Board.NUM_POINTS];
        points[10] = -2;
        Board board = Board.customPosition(points, 0, 0, 0, 0);

        assertTrue(board.isBlockedFor(Player.WHITE, 10));
        assertFalse(board.isBlot(Player.BLACK, 10));
    }

    @Test
    void movePieceThrowsExceptionWhenNoOwnCheckerAtSource() {
        Board board = Board.empty();
        assertThrows(IllegalStateException.class, () -> board.movePiece(Player.WHITE, 10, 5));
    }

    @Test
    void movePieceThrowsExceptionWhenSourceHasOnlyOpponentChecker() {
        int[] points = new int[Board.NUM_POINTS];
        points[10] = -1;
        Board board = Board.customPosition(points, 0, 0, 0, 0);
        assertThrows(IllegalStateException.class, () -> board.movePiece(Player.WHITE, 10, 5));
    }

    @Test
    void movePieceThrowsExceptionWhenDestinationBlockedByOpponent() {
        int[] points = new int[Board.NUM_POINTS];
        points[10] = 1;
        points[5] = -2;
        Board board = Board.customPosition(points, 0, 0, 0, 0);
        assertThrows(IllegalStateException.class, () -> board.movePiece(Player.WHITE, 10, 5));
    }

    @Test
    void movePieceDecrementsSourceButKeepsOtherCheckersOnSamePoint() {
        int[] points = new int[Board.NUM_POINTS];
        points[10] = 4;
        Board board = Board.customPosition(points, 0, 0, 0, 0);

        board.movePiece(Player.WHITE, 10, 5);

        assertEquals(3, board.checkersOf(Player.WHITE, 10));
    }

    @Test
    void movePieceSingleCheckerMovePointBecomesEmptyAfter() {
        int[] points = new int[Board.NUM_POINTS];
        points[10] = 1;
        Board board = Board.customPosition(points, 0, 0, 0, 0);

        board.movePiece(Player.WHITE, 10, 5);

        assertTrue(board.isEmpty(10));
    }

    @Test
    void enterFromBarDecreasesBarCountAndPlacesChecker() {
        Board board = Board.customPosition(new int[Board.NUM_POINTS], 2, 0, 0, 0);

        board.enterFromBar(Player.WHITE, 3);
        board.enterFromBar(Player.WHITE, 3);

        assertEquals(0, board.barCount(Player.WHITE));
        assertEquals(2, board.checkersOf(Player.WHITE, 3));
    }

    @Test
    void enterFromBarHitsOpponentBlot() {
        int[] points = new int[Board.NUM_POINTS];
        points[3] = -1;
        Board board = Board.customPosition(points, 1, 0, 0, 0);

        board.enterFromBar(Player.WHITE, 3);

        assertEquals(1, board.checkersOf(Player.WHITE, 3));
        assertEquals(0, board.checkersOf(Player.BLACK, 3));
        assertEquals(1, board.barCount(Player.BLACK));
    }

    @Test
    void enterFromBarBlackEntersDecreasesBlackBarOnly() {
        Board board = Board.customPosition(new int[Board.NUM_POINTS], 5, 1, 0, 0);

        board.enterFromBar(Player.BLACK, 20);

        assertEquals(0, board.barCount(Player.BLACK));
        assertEquals(5, board.barCount(Player.WHITE), "Bar białego nie powinien się zmienić");
    }

    @Test
    void enterFromBarThrowsExceptionWhenNoCheckersOnBar() {
        Board board = Board.empty();
        assertThrows(IllegalStateException.class, () -> board.enterFromBar(Player.WHITE, 3));
    }

    @Test
    void enterFromBarThrowsExceptionWhenEntryPointBlocked() {
        int[] points = new int[Board.NUM_POINTS];
        points[3] = -2;
        Board board = Board.customPosition(points, 1, 0, 0, 0);
        assertThrows(IllegalStateException.class, () -> board.enterFromBar(Player.WHITE, 3));
    }

    @Test
    void enterFromBarAfterHittingBlotBarCountForEnteringPlayerStillDecreases() {
        int[] points = new int[Board.NUM_POINTS];
        points[3] = -1;
        Board board = Board.customPosition(points, 3, 0, 0, 0);

        board.enterFromBar(Player.WHITE, 3);

        assertEquals(2, board.barCount(Player.WHITE));
    }

    @Test
    void bearOffIncreasesBorneOffCountAndRemovesChecker() {
        int[] points = new int[Board.NUM_POINTS];
        points[3] = 1;
        Board board = Board.customPosition(points, 0, 0, 0, 0);

        board.bearOff(Player.WHITE, 3);

        assertEquals(0, board.checkersOf(Player.WHITE, 3));
        assertEquals(1, board.borneOffCount(Player.WHITE));
    }

    @Test
    void bearOffBlackPlayerIncrementsBlackBorneOffOnly() {
        int[] points = new int[Board.NUM_POINTS];
        points[20] = -1;
        Board board = Board.customPosition(points, 0, 0, 5, 0);

        board.bearOff(Player.BLACK, 20);

        assertEquals(1, board.borneOffCount(Player.BLACK));
        assertEquals(5, board.borneOffCount(Player.WHITE));
    }

    @Test
    void bearOffThrowsExceptionWhenNoOwnCheckerAtSource() {
        Board board = Board.empty();
        assertThrows(IllegalStateException.class, () -> board.bearOff(Player.WHITE, 3));
    }

    @Test
    void bearOffThrowsExceptionWhenOnlyOpponentCheckerAtSource() {
        int[] points = new int[Board.NUM_POINTS];
        points[3] = -1;
        Board board = Board.customPosition(points, 0, 0, 0, 0);
        assertThrows(IllegalStateException.class, () -> board.bearOff(Player.WHITE, 3));
    }

    @Test
    void bearOffMultipleCheckersOnSamePointRemovesOnlyOne() {
        int[] points = new int[Board.NUM_POINTS];
        points[3] = 3;
        Board board = Board.customPosition(points, 0, 0, 0, 0);

        board.bearOff(Player.WHITE, 3);

        assertEquals(2, board.checkersOf(Player.WHITE, 3));
        assertEquals(1, board.borneOffCount(Player.WHITE));
    }

    @Test
    void allCheckersInHomeFalseWhenCheckerOutsideHome() {
        Board board = Board.initialSetup();
        assertFalse(board.allCheckersInHome(Player.WHITE));
        assertFalse(board.allCheckersInHome(Player.BLACK));
    }

    @Test
    void allCheckersInHomeFalseWhenCheckerOnBar() {
        int[] points = new int[Board.NUM_POINTS];
        points[3] = 14;
        Board board = Board.customPosition(points, 1, 0, 0, 0);
        assertFalse(board.allCheckersInHome(Player.WHITE));
    }

    @Test
    void allCheckersInHomeTrueWhenAllInHomeBoard() {
        int[] points = new int[Board.NUM_POINTS];
        points[0] = 5;
        points[3] = 5;
        points[5] = 5;
        Board board = Board.customPosition(points, 0, 0, 0, 0);
        assertTrue(board.allCheckersInHome(Player.WHITE));
    }

    @Test
    void allCheckersInHomeFalseWhenSingleCheckerJustOutsideBoundary() {
        int[] points = new int[Board.NUM_POINTS];
        points[6] = 1;
        Board board = Board.customPosition(points, 0, 0, 0, 0);
        assertFalse(board.allCheckersInHome(Player.WHITE));
    }

    @Test
    void allCheckersInHomeBlackTrueWhenAllInHomeBoard() {
        int[] points = new int[Board.NUM_POINTS];
        points[18] = -5;
        points[23] = -10;
        Board board = Board.customPosition(points, 0, 0, 0, 0);
        assertTrue(board.allCheckersInHome(Player.BLACK));
    }

    @Test
    void allCheckersInHomeBlackFalseWhenSingleCheckerJustOutsideBoundary() {
        int[] points = new int[Board.NUM_POINTS];
        points[17] = -1;
        Board board = Board.customPosition(points, 0, 0, 0, 0);
        assertFalse(board.allCheckersInHome(Player.BLACK));
    }

    @Test
    void allCheckersInHomeIgnoresOpponentCheckersEverywhere() {
        int[] points = new int[Board.NUM_POINTS];
        points[0] = 15;
        points[10] = -1;
        points[20] = -1;
        Board board = Board.customPosition(points, 0, 0, 0, 0);
        assertTrue(board.allCheckersInHome(Player.WHITE));
    }

    @Test
    void isInHomeWhiteBoundaryAtFive() {
        Board board = Board.empty();
        assertTrue(board.isInHome(Player.WHITE, 5));
        assertFalse(board.isInHome(Player.WHITE, 6));
    }

    @Test
    void isInHomeWhiteBoundaryAtZero() {
        Board board = Board.empty();
        assertTrue(board.isInHome(Player.WHITE, 0));
    }

    @Test
    void isInHomeBlackBoundaryAtEighteen() {
        Board board = Board.empty();
        assertTrue(board.isInHome(Player.BLACK, 18));
        assertFalse(board.isInHome(Player.BLACK, 17));
    }

    @Test
    void isInHomeBlackBoundaryAtTwentyThree() {
        Board board = Board.empty();
        assertTrue(board.isInHome(Player.BLACK, 23));
    }

    @Test
    void isInHomePointsInMiddleAreNeverHome() {
        Board board = Board.empty();
        for (int i = 6; i <= 17; i++) {
            assertFalse(board.isInHome(Player.WHITE, i));
            assertFalse(board.isInHome(Player.BLACK, i));
        }
    }

    @Test
    void furthestCheckerInHomeWhiteReturnsHighestIndex() {
        int[] points = new int[Board.NUM_POINTS];
        points[2] = 1;
        points[5] = 1;
        Board board = Board.customPosition(points, 0, 0, 0, 0);
        assertEquals(5, board.furthestCheckerInHome(Player.WHITE));
    }

    @Test
    void furthestCheckerInHomeWhiteIgnoresCheckersOutsideHome() {
        int[] points = new int[Board.NUM_POINTS];
        points[2] = 1;
        points[10] = 5;
        Board board = Board.customPosition(points, 0, 0, 0, 0);
        assertEquals(2, board.furthestCheckerInHome(Player.WHITE));
    }

    @Test
    void furthestCheckerInHomeBlackReturnsLowestIndex() {
        int[] points = new int[Board.NUM_POINTS];
        points[20] = -1;
        points[18] = -1;
        Board board = Board.customPosition(points, 0, 0, 0, 0);
        assertEquals(18, board.furthestCheckerInHome(Player.BLACK));
    }

    @Test
    void furthestCheckerInHomeReturnsMinusOneWhenHomeEmpty() {
        Board board = Board.empty();
        assertEquals(-1, board.furthestCheckerInHome(Player.WHITE));
        assertEquals(-1, board.furthestCheckerInHome(Player.BLACK));
    }

    @Test
    void furthestCheckerInHomeSingleCheckerAtExtremeEdge() {
        int[] points = new int[Board.NUM_POINTS];
        points[0] = 1;
        Board board = Board.customPosition(points, 0, 0, 0, 0);
        assertEquals(0, board.furthestCheckerInHome(Player.WHITE));
    }

    @Test
    void hasWonTrueWhenAllFifteenBorneOff() {
        Board board = Board.customPosition(new int[Board.NUM_POINTS], 0, 0, 15, 0);
        assertTrue(board.hasWon(Player.WHITE));
    }

    @Test
    void hasWonFalseWhenFourteenBorneOff() {
        Board board = Board.customPosition(new int[Board.NUM_POINTS], 0, 0, 14, 0);
        assertFalse(board.hasWon(Player.WHITE));
    }

    @Test
    void hasWonFalseAtStartOfGame() {
        Board board = Board.initialSetup();
        assertFalse(board.hasWon(Player.WHITE));
        assertFalse(board.hasWon(Player.BLACK));
    }

    @Test
    void hasWonDoesNotConfuseBothPlayers() {
        Board board = Board.customPosition(new int[Board.NUM_POINTS], 0, 0, 15, 3);
        assertTrue(board.hasWon(Player.WHITE));
        assertFalse(board.hasWon(Player.BLACK));
    }

    @Test
    void isGammonTrueWhenLoserBorneOffNothing() {
        Board board = Board.customPosition(new int[Board.NUM_POINTS], 0, 0, 15, 0);
        assertTrue(board.isGammon(Player.WHITE));
    }

    @Test
    void isGammonFalseWhenLoserBoreOffExactlyOne() {
        Board board = Board.customPosition(new int[Board.NUM_POINTS], 0, 0, 15, 1);
        assertFalse(board.isGammon(Player.WHITE));
    }

    @Test
    void isGammonFalseWhenLoserBoreOffMany() {
        Board board = Board.customPosition(new int[Board.NUM_POINTS], 0, 0, 15, 10);
        assertFalse(board.isGammon(Player.WHITE));
    }

    @Test
    void isGammonWorksSymmetricallyForBlackWinner() {
        Board board = Board.customPosition(new int[Board.NUM_POINTS], 0, 0, 0, 15);
        assertTrue(board.isGammon(Player.BLACK));
    }

    @Test
    void isBackgammonTrueWhenLoserHasCheckerOnBar() {
        Board board = Board.customPosition(new int[Board.NUM_POINTS], 0, 1, 15, 0);
        assertTrue(board.isBackgammon(Player.WHITE));
    }

    @Test
    void isBackgammonTrueWhenLoserCheckerInWinnersHome() {
        int[] points = new int[Board.NUM_POINTS];
        points[3] = -1;
        Board board = Board.customPosition(points, 0, 0, 15, 0);
        assertTrue(board.isBackgammon(Player.WHITE));
    }

    @Test
    void isBackgammonFalseWhenLoserCheckersAreOutsideWinnersHome() {
        int[] points = new int[Board.NUM_POINTS];
        points[10] = -1;
        Board board = Board.customPosition(points, 0, 0, 15, 0);
        assertFalse(board.isBackgammon(Player.WHITE));
    }

    @Test
    void isBackgammonBoundaryCheckerExactlyAtEdgeOfWinnersHome() {
        int[] points = new int[Board.NUM_POINTS];
        points[5] = -1;
        Board board = Board.customPosition(points, 0, 0, 15, 0);
        assertTrue(board.isBackgammon(Player.WHITE));
    }

    @Test
    void isBackgammonBoundaryCheckerJustOutsideWinnersHome() {
        int[] points = new int[Board.NUM_POINTS];
        points[6] = -1;
        Board board = Board.customPosition(points, 0, 0, 15, 0);
        assertFalse(board.isBackgammon(Player.WHITE));
    }

    @Test
    void isBackgammonWorksSymmetricallyForBlackWinner() {
        Board board = Board.customPosition(new int[Board.NUM_POINTS], 1, 0, 0, 15);
        assertTrue(board.isBackgammon(Player.BLACK));
    }

    @Test
    void toStringDoesNotThrowAndContainsKeyInformation() {
        Board board = Board.initialSetup();
        String result = board.toString();

        assertNotNull(result);
        assertTrue(result.contains("Bar"));
        assertTrue(result.contains("Borne off"));
    }
}