package me.hwproj;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Игра "Найди пару".
 * При запуске отображается поле с кнопками размера N x N (N передаётся как параметр при запуске), кнопки без надписей.
 * Каждой кнопке ставится в соответствие случайное число от 0 до N2 / 2.
 * Игрок нажимает на две произвольные (разные) кнопки, на них показываются соответствующие им числа.
 * Если числа совпали, кнопки делаются неактивными.
 * Если числа не совпали, кнопки через некоторое время возвращаются в изначальное положение.
 * Игра заканчивается, когда игрок открыл все пары чисел (программа должна генерировать числа таким образом, чтобы это было возможно).
 */
public class CW2 extends Application {
    private List<List<Button>> boardUI = new ArrayList<>();
    private Board gameBoard;
    private int boardSize = 3;

    private static class MutablePair {
        private Integer i;
        private Integer j;

        private MutablePair(Integer i, Integer j) {
            this.i = i; // Immutable
            this.j = j;
        }
    }

    private static class MutableBoolean {
        private Boolean value;

        private MutableBoolean(Boolean value) {
            this.value = value;
        }
    }

    @Override
    public void start(Stage stage) {
        MutablePair previousPosition = new MutablePair(-1, -1);
        if (getParameters().getRaw().size() == 1)
            boardSize = Integer.parseInt(getParameters().getRaw().get(0));
        gameBoard = new Board(boardSize);
        MutableBoolean isStopped = new MutableBoolean(false);
        for (int i = 0; i < boardSize; i++) {
            List<Button> row = new ArrayList<>();
            for (int j = 0; j < boardSize; j++) {
                var button = new Button();
                button.setMaxWidth(Double.MAX_VALUE);
                button.setMaxHeight(Double.MAX_VALUE);
                Integer columnIndex = j;
                Integer rowIndex = i;
                if (i == boardSize / 2 && j == boardSize / 2 && boardSize % 2 != 0) {
                    button.setText("-1");
                    button.setDisable(true);
                } else {
                    button.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            if (isStopped.value) {
                                return;
                            }
                            if (previousPosition.i.equals(-1)) {
                                previousPosition.i = rowIndex;
                                previousPosition.j = columnIndex;
                                boardUI.get(previousPosition.i).get(previousPosition.j).setDisable(true);
                            } else {
                                isStopped.value = true;
                                boardUI.get(previousPosition.i).get(previousPosition.j)
                                        .setText(
                                                Integer.toString(
                                                        gameBoard.getValue(
                                                                previousPosition.i, previousPosition.j)
                                                                .getValue()));
                                boardUI.get(rowIndex).get(columnIndex)
                                        .setText(
                                                Integer.toString(
                                                        gameBoard.getValue(
                                                                rowIndex, columnIndex)
                                                                .getValue()));
                                boardUI.get(rowIndex).get(columnIndex).setDisable(true);
                                if (gameBoard.makeMove(previousPosition.i, previousPosition.j,
                                        rowIndex, columnIndex)) {
                                    previousPosition.i = -1;
                                    previousPosition.j = -1;
                                    isStopped.value = false;
                                } else {
                                    Thread thread = new Thread(() -> {
                                        Runnable updater = new Runnable() {
                                            @Override
                                            public void run() {
                                                boardUI.get(previousPosition.i).get(previousPosition.j)
                                                        .setText("");
                                                boardUI.get(rowIndex).get(columnIndex)
                                                        .setText("");
                                                boardUI.get(previousPosition.i).get(previousPosition.j).setDisable(false);
                                                boardUI.get(rowIndex).get(columnIndex).setDisable(false);
                                                previousPosition.i = -1;
                                                previousPosition.j = -1;
                                                isStopped.value = false;
                                            }
                                        };

                                        try {
                                            Thread.sleep(1000);
                                        } catch (InterruptedException e) {
                                        }
                                        Platform.runLater(updater);
                                    });
                                    thread.start();
                                }
                                if (gameBoard.getStatus() != BoardStatus.INGAME) {
                                    var textLabel = "You win!";
                                    var endLabel = new Label(textLabel);
                                    endLabel.setAlignment(Pos.CENTER);
                                    var endScene = new Scene(endLabel, 300, 300);
                                    stage.setScene(endScene);
                                    stage.show();
                                }
                            }
                        }
                    });
                }
                row.add(button);
            }
            boardUI.add(row);
        }

        var root = new GridPane();

        for (int column = 0; column < boardSize; column++) {
            var currentColumn = new ColumnConstraints();
            currentColumn.setPercentWidth(100 / boardSize);
            root.getColumnConstraints().add(currentColumn);
        }

        for (int row = 0; row < boardSize; row++) {
            var currentRow = new RowConstraints();
            currentRow.setPercentHeight(100 / boardSize);
            root.getRowConstraints().add(currentRow);
        }

        root.setGridLinesVisible(true);

        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                root.add(boardUI.get(i).get(j), i, j);
            }
        }

        var scene = new Scene(root, 500, 500);
        stage.setScene(scene);

        stage.setTitle("TigTig");

        stage.show();
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 2) {
            throw new Exception("Should be 0 or 1 args");
        }
        launch(args);
    }

}