package me.hwproj;

import javafx.scene.control.Button;

import java.util.ArrayList;
import java.util.List;

public class Board {
    private List<List<Integer>> board = new ArrayList<>();
    private boolean currentTurn;

    public Board() {
        for (int i = 0; i < 3; i++) {
            List<Integer> row = new ArrayList<>();
            row.add(0);
            row.add(0);
            row.add(0);
            board.add(row);
        }
    }

    public Integer getStatus() {
        for (int value = 1; value < 3; value++) {
            for (int i = 0; i < 3; i++) {
                int flag1 = value;
                int flag2 = value;
                for (int j = 0; j < 3; j++) {
                    if (board.get(i).get(j) != value) {
                        flag1 = 0;
                    }

                    if (board.get(j).get(i) != value) {
                        flag2 = 0;
                    }
                }
                if (flag1 != 0 || flag2 != 0) {
                    return value;
                }
            }
            if (board.get(0).get(0) == value &&
                    board.get(1).get(1) == value &&
                    board.get(2).get(2) == value) {
                return value;
            }
            if (board.get(2).get(0) == value &&
                    board.get(1).get(1) == value &&
                    board.get(0).get(2) == value) {
                return value;
            }
        }
        return 0;
    }

    public String makeMove(int i, int j) {
        if (board.get(i).get(j) == 0) {
            if (currentTurn) {
                var newRow = board.get(i);
                newRow.set(j, 2);
                board.set(i, newRow);
                currentTurn = !currentTurn;
                return "O";
            } else {
                var newRow = board.get(i);
                newRow.set(j, 1);
                board.set(i, newRow);
                currentTurn = !currentTurn;
                return "X";
            }
        } else {
            if (board.get(i).get(j) == 1) {
                return "X";
            } else {
                return "O";
            }
        }
    }
}
