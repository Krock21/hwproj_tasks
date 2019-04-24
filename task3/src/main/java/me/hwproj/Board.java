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
        for (int i = 0; i < 3; i++) {
            int flag1 = -1;
            int flag2 = -1;
            for (int j = 0; j < 3; j++) {
                if (board.get(i).get(j) != flag1) {
                    if (flag1 == -1) {
                        flag1 = board.get(i).get(j);
                    } else {
                        flag1 = -2;
                    }
                }

                if (board.get(j).get(i) != flag2) {
                    if (flag2 == -1) {
                        flag2 = board.get(i).get(j);
                    } else {
                        flag2 = -2;
                    }
                }
            }
            if (flag1 == 1 || flag2 == 1) {
                return 1;
            }
            if (flag1 == 2 || flag2 == 2) {
                return 2;
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
