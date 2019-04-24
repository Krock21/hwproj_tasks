package me.hwproj;

import javafx.application.Application;
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

import java.util.ArrayList;
import java.util.List;

public class Task3 extends Application {
    private List<List<Button>> boardUI = new ArrayList<>();
    private Board gameBoard = new Board();

    @Override
    public void start(Stage stage) {
        for (int i = 0; i < 3; i++) {
            List<Button> row = new ArrayList<>();
            for (int j = 0; j < 3; j++) {
                var button = new Button();
                button.setMaxWidth(Double.MAX_VALUE);
                button.setMaxHeight(Double.MAX_VALUE);
                var columnIndex = j;
                var rowIndex = i;
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        button.setText(gameBoard.makeMove(columnIndex, rowIndex));
                        if (gameBoard.getStatus() != BoardStatus.INGAME) {
                            var textLabel = "";
                            if (gameBoard.getStatus() == BoardStatus.X_WIN) {
                                textLabel = "X wins!";
                            } else if (gameBoard.getStatus() == BoardStatus.O_WIN) {
                                textLabel = "O wins!";
                            } else {
                                textLabel = "DRAW!";
                            }
                            var endLabel = new Label(textLabel);
                            endLabel.setAlignment(Pos.CENTER);
                            var endScene = new Scene(endLabel, 300, 300);
                            stage.setScene(endScene);
                            stage.show();
                        }
                    }
                });
                row.add(button);
            }
            boardUI.add(row);
        }

        var root = new GridPane();

        var column1 = new ColumnConstraints();
        column1.setPercentWidth(33);
        column1.setFillWidth(true);
        root.getColumnConstraints().add(column1);

        var column2 = new ColumnConstraints();
        column2.setPercentWidth(34);
        root.getColumnConstraints().add(column2);

        var column3 = new ColumnConstraints();
        column3.setPercentWidth(33);
        root.getColumnConstraints().add(column3);

        var row1 = new RowConstraints();
        row1.setPercentHeight(33);
        root.getRowConstraints().add(row1);

        var row2 = new RowConstraints();
        row2.setPercentHeight(34);
        root.getRowConstraints().add(row2);

        var row3 = new RowConstraints();
        row3.setPercentHeight(33);
        root.getRowConstraints().add(row3);

        root.setGridLinesVisible(true);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                root.add(boardUI.get(i).get(j), i, j);
            }
        }

        var scene = new Scene(root, 500, 500);
        stage.setScene(scene);

        stage.setTitle("TigTig");

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}