package me.hwproj;

import javafx.scene.control.Button;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Board {
    private List<List<Node>> board = new ArrayList<>();
    private int boardSize;
    private int countDead;

    public Board(int boardSize) {
        this.boardSize = boardSize;
        var values = new ArrayList<Integer>();
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                values.add((i * boardSize + j) / 2);
            }
        }
        if (boardSize % 2 != 0) {
            values.remove(boardSize * boardSize - 1);
        }
        Collections.shuffle(values);
        var currentValue = values.iterator();
        for (int i = 0; i < boardSize; i++) {
            List<Node> row = new ArrayList<>();
            for (int j = 0; j < boardSize; j++) {
                if (boardSize % 2 == 0 || i != boardSize / 2 || j != boardSize / 2) {
                    row.add(new Node(currentValue.next()));
                } else {
                    Node midNode = new Node(-1);
                    midNode.setDead(true);
                    row.add(midNode);
                    countDead++;
                }
            }
            board.add(row);
        }
    }

    public BoardStatus getStatus() {
        if (countDead == boardSize * boardSize) {
            return BoardStatus.WIN;
        } else {
            return BoardStatus.INGAME;
        }
    }

    public boolean makeMove(int i1, int j1, int i2, int j2) {
        if (board.get(i1).get(j1).getValue() == board.get(i2).get(j2).getValue() &&
                !board.get(i1).get(j1).isDead() && !board.get(i2).get(j2).isDead()) {
            board.get(i1).get(j1).setDead(true);
            board.get(i2).get(j2).setDead(true);
            countDead += 2;
            return true;
        }
        return false;
    }

    public Node getValue(int i, int j) {
        return board.get(i).get(j);
    }
}
