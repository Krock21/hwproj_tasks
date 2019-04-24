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
