import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.Arrays;

/**
 * TODO
 */
public class ClientUI extends Application {
    /**
     * TODO
     */
    private double screenWidth;


    /**
     * TODO
     */
    private double screenHeight;

    /**
     * TODO
     */
    private static final int FILES_PER_SCREEN = 20;

    /**
     * TODO
     */
    private double fileHeight;

    /**
     * TODO
     */
    private int currentFilesOnScreen;

    /**
     * Main stage of the window.
     */
    private Stage primaryStage;

    /**
     * TODO
     */
    private VBox fileMenu;

    /**
     * TODO
     */
    private double currentFileMenuWidth;
    private double currentFileMenuHeight;

    /**
     * TODO
     */
    private static final int INITIAL_WIDTH = 600;
    private static final int MINIMAL_FILES_ON_SCREEN = 5;

    /**
     * TODO
     */
    private Label[] labels = new Label[FILES_PER_SCREEN];

    /**
     * TODO
     */
    private int currentFile = 0;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        screenWidth = primaryScreenBounds.getWidth();
        screenHeight = primaryScreenBounds.getHeight();
        fileHeight = screenHeight / FILES_PER_SCREEN;

        primaryStage.setTitle("My FTP client");

        BorderPane root = new BorderPane();
        fileMenu = new VBox();
        fileMenu.setFillWidth(true);
        //fileMenu.setPrefHeight(screenHeight);

        MenuBar menuBar = new MenuBar();
        Menu menu = new Menu("Server");
        MenuItem connectToServer = new MenuItem("Connect to new server");
        MenuItem disconnectFromServer = new MenuItem("Disconnect from server");
        menu.getItems().addAll(connectToServer, disconnectFromServer);
        menuBar.getMenus().add(menu);

        root.setCenter(fileMenu);
        root.setTop(menuBar);
        Scene scene = new Scene(root);

        for (int i = 0; i < FILES_PER_SCREEN; i++) {
            labels[i] = new Label();
            labels[i].setText("queue of QUEUE");
            labels[i].setMinHeight(fileHeight);
            labels[i].setFont(Font.font(fileHeight));
        }

        primaryStage.setMinWidth(INITIAL_WIDTH);
        primaryStage.setMinHeight(MINIMAL_FILES_ON_SCREEN * fileHeight);

        primaryStage.setWidth(INITIAL_WIDTH);
        primaryStage.setHeight(MINIMAL_FILES_ON_SCREEN * fileHeight);

        /*primaryStage.setX(primaryScreenBounds.getMinX());
        primaryStage.setY(primaryScreenBounds.getMinY());
        primaryStage.setWidth(primaryScreenBounds.getWidth() - 50); //So it would be easy to drag window...
        primaryStage.setHeight(primaryScreenBounds.getHeight() - 50);
         */

        primaryStage.setScene(scene);

        //fileMenu.widthProperty().addListener((observableValue, oldValue, newValue) -> setCurrentWidthToStage(newValue));
        fileMenu.heightProperty().addListener((observableValue, oldValue, newValue) -> setCurrentHeightToStage(newValue));

        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case DOWN: case S:
                    if (currentFile + 1 < currentFilesOnScreen) {
                        currentFile++;
                    }

                    redraw();
                    break;
                case UP: case W:
                    if (currentFile > 0) {
                        currentFile--;
                    }

                    redraw();
                    break;
                case ENTER:
                    break;
            }
        });

        fileMenu.setBackground(new Background(new BackgroundFill(Color.BLUE, CornerRadii.EMPTY, Insets.EMPTY)));

        primaryStage.show();

        /*currentFileMenuWidth = fileMenu.getWidth();
        currentFileMenuHeight = fileMenu.getHeight();

        resize();
        redraw();*/
    }

    /**
     * TODO
     */
    private void setCurrentWidthToStage(Number newWidth) {
        //primaryStage.setWidth((double) newWidth);
        currentFileMenuWidth = (double) newWidth;
        resize();
        redraw();
    }

    /**
     * TODO
     */
    private void setCurrentHeightToStage(Number newHeight) {
        //primaryStage.setHeight((double) newHeight);
        currentFileMenuHeight = (double) newHeight;
        resize();
        redraw();
    }

    /**
     * TODO
     */
    private void resize() {
        currentFilesOnScreen = (int) (currentFileMenuHeight / fileHeight);

        if (currentFile >= currentFilesOnScreen) {
            currentFile = currentFilesOnScreen - 1;
        }
        if (currentFile < 0) {
            currentFile = 0;
        }

        fileMenu.setFillWidth(true);
        var fileList = fileMenu.getChildren();
        fileList.clear();
        fileList.addAll(Arrays.asList(labels).subList(0, currentFilesOnScreen));
    }

    /**
     * TODO
     */
    private void redraw() {
        for (int i = 0; i < currentFilesOnScreen; i++) {
            if (i == currentFile) {
                labels[i].setBackground(new Background(new BackgroundFill(Color.CYAN, CornerRadii.EMPTY, Insets.EMPTY)));
            } else {
                labels[i].setBackground(new Background(new BackgroundFill(Color.BLUE, CornerRadii.EMPTY, Insets.EMPTY)));
            }
        }
    }

    @Override
    public void stop() {
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
