package me.hwproj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    private Board board;
    private static int BOARD_SIZE = 5; // should be greater than 2

    @BeforeEach
    void init() {
        board = new Board(BOARD_SIZE);
    }

    @Test
    void makeMoveTest() {
        Node n0 = board.getValue(0, 0);
        Node n1 = board.getValue(0, 1);
        if (n0.getValue() == n1.getValue()) {
            assertEquals(false, board.makeMove(0, 0, 0, 2));
        } else {
            assertEquals(false, board.makeMove(0, 0, 0, 1));
        }
        Node equalsToN0;
        int eqI = 0;
        int eqJ = 0;
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (i != 0 && j != 0 && board.getValue(i, j).getValue() == n0.getValue()) {
                    equalsToN0 = board.getValue(i, j);
                    eqI = i;
                    eqJ = j;
                }
            }
        }
        assertEquals(true, board.makeMove(0, 0, eqI, eqJ));
    }

    @Test
    void CheckCenterOnOddBoard() {
        Board currentBoard = new Board(7);
        assertEquals(-1, currentBoard.getValue(7 / 2, 7 / 2).getValue());
    }

    @Test
    void CheckBoardCorrectnessOnOdd() {
        int size = 5;
        Board board = new Board(size);
        Map<Integer, Integer> values = new TreeMap<>();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Integer value = board.getValue(i, j).getValue();
                if (value != -1)
                    if (values.containsKey(value)) {
                        values.put(value, values.get(value) + 1);
                    } else {
                        values.put(value, 1);
                    }
            }
        }
        for (var v : values.entrySet()) {
            assertFalse(v.getKey() < 0 || v.getKey() > size * size / 2 || v.getValue() != 2);
        }
    }


    @Test
    void CheckBoardCorrectnessOnEven() {
        int size = 6;
        Board board = new Board(size);
        Map<Integer, Integer> values = new TreeMap<>();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Integer value = board.getValue(i, j).getValue();
                if (value != -1)
                    if (values.containsKey(value)) {
                        values.put(value, values.get(value) + 1);
                    } else {
                        values.put(value, 1);
                    }
            }
        }
        for (var v : values.entrySet()) {
            assertFalse(v.getKey() < 0 || v.getKey() > size * size / 2 || v.getValue() != 2);
        }
    }

    @Test
    void getValue() {
    }
}