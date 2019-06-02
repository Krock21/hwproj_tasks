import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Pair;
import me.hwproj.Client;
import me.hwproj.FileDescription;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * GUI for our FTP server.
 */
public class ClientUI extends Application {
    /**
     * Maximum screen's width.
     */
    private double screenWidth;


    /**
     * Maximum screen's height.
     */
    private double screenHeight;

    /**
     * Maximum number of files per screen (if screen height is maximum).
     */
    private static final int FILES_PER_SCREEN = 20;

    /**
     * Height of one file on the screen (fixed to screenHeight).
     */
    private double fileHeight;

    /**
     * Number of files existing on screen (fixed to current fileMenu's height).
     */
    private int currentFilesOnScreen;

    /**
     * Main stage of the window.
     */
    private Stage primaryStage;

    /**
     * List of files.
     */
    private VBox fileMenu;

    /**
     * Current size of fileMenu.
     */
    private double currentFileMenuWidth;
    private double currentFileMenuHeight;

    /**
     * Initial window's screen.
     */
    private static final int INITIAL_WIDTH = 600;

    /**
     * Minimum number of files that will be present on the screen. Therefore, minimum screen height is not less than
     * fileHeight * MINIMAL_FILES_ON_SCREEN;
     */
    private static final int MINIMAL_FILES_ON_SCREEN = 5;

    /**
     * All label's that will show files
     */
    private Label[] labels = new Label[FILES_PER_SCREEN];


    /**
     * List of files that we got from the server on the last call.
     */
    private List<FileDescription> currentFiles;

    /**
     * Is this list sorted (first by isDirectory(), secondly lexicographically).
     */
    private boolean sorted = false;

    /**
     * Number of the current chosen label on the screen.
     */
    private int currentLabel = 0;

    /**
     * Number of the current chosen file.
     */
    private int currentFile = 0;

    /**
     * Path to the current work directory (on the server).
     */
    private Path path = FileSystems.getDefault().getPath(".");

    /**
     * Client we working with.
     */
    private Client client = new Client();

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
        connectToServer.setOnAction(this::connect);
        MenuItem disconnectFromServer = new MenuItem("Disconnect from server");
        disconnectFromServer.setOnAction(this::disconnect);
        menu.getItems().addAll(connectToServer, disconnectFromServer);
        menuBar.getMenus().add(menu);

        root.setCenter(fileMenu);
        root.setTop(menuBar);
        Scene scene = new Scene(root);

        for (int i = 0; i < FILES_PER_SCREEN; i++) {
            labels[i] = new Label();
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
                    if (currentLabel + 1 < currentFilesOnScreen && currentFile + 1 < currentFiles.size()) {
                        currentLabel++;
                    }

                    if (currentFile + 1 < currentFiles.size()) {
                        currentFile++;
                    }

                    redraw();
                    break;
                case UP: case W:
                    if (currentLabel > 0) {
                        currentLabel--;
                    }

                    if (currentFile > 0) {
                        currentFile--;
                    }

                    redraw();
                    break;
                case ENTER:
                    FileDescription file = currentFiles.get(currentFile);

                    if (file.getPath().equals("../")) {
                        path = path.getParent();
                        updateManager();
                    } else if (file.getIsDirectory()) {
                        path = FileSystems.getDefault().getPath(path.toString(), file.getPath() + "/");
                        updateManager();
                    } else {
                        FileChooser fileChooser = new FileChooser();
                        File fileToSave = fileChooser.showSaveDialog(primaryStage);
                        fileChooser.setInitialFileName(file.getPath());

                        if (fileToSave != null) {
                            try {
                                client.executeGetWithFile(FileSystems.getDefault().getPath(path.toString(), file.getPath()).toString(), fileToSave);
                            } catch (IOException e) {
                                showError("IO error: " + e.getMessage());
                            }
                        }
                    }

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
     * Updates current state after changing the working directory.
     */
    private void updateManager() {
        currentFile = 0;
        currentLabel = 0;

        try {
            currentFiles = client.executeList(path.toString());
        } catch (IOException e) {
            showError("Error during execution: " + e.getMessage());
        }

        if (path.getParent() != null) {
            currentFiles.add(new FileDescription("../", true));
        }
        sorted = false;
        redraw();
    }

    /**
     * For now unused. Handles changing of the screen's width.
     */
    private void setCurrentWidthToStage(Number newWidth) {
        //primaryStage.setWidth((double) newWidth);
        currentFileMenuWidth = (double) newWidth;
        resize();
        redraw();
    }

    /**
     * Handles changing of the screen's height.
     */
    private void setCurrentHeightToStage(Number newHeight) {
        //primaryStage.setHeight((double) newHeight);
        currentFileMenuHeight = (double) newHeight;
        resize();
        redraw();
    }

    /**
     * Changes number of label's presented on the screen after height's changes.
     */
    private void resize() {
        currentFilesOnScreen = (int) (currentFileMenuHeight / fileHeight);

        if (currentLabel >= currentFilesOnScreen) {
            currentLabel = currentFilesOnScreen - 1;
        }
        if (currentLabel < 0) {
            currentLabel = 0;
        }

        fileMenu.setFillWidth(true);
        var fileList = fileMenu.getChildren();
        fileList.clear();
        fileList.addAll(Arrays.asList(labels).subList(0, currentFilesOnScreen));
        redraw();
    }

    /**
     * Set's correct text and color to the label's on the screen.
     */
    private void redraw() {
        if (currentFiles == null) {
            currentFiles = new ArrayList<>();
        }
        if (!sorted) {
            currentFiles.sort((fd1, fd2) -> {
                if (fd1.getIsDirectory() == fd2.getIsDirectory()) {
                    return fd1.getPath().equals("../") ? -1 : fd1.getPath().compareTo(fd2.getPath());
                } else {
                    return fd2.getIsDirectory() ? 1 : -1;
                }
            });
            sorted = true;
        }

        for (var label : labels) {
            label.setText("");
        }

        for (int i = 0; i < currentFilesOnScreen; i++) {
            if (i == currentLabel) {
                labels[i].setBackground(new Background(new BackgroundFill(Color.CYAN, CornerRadii.EMPTY, Insets.EMPTY)));
            } else {
                labels[i].setBackground(new Background(new BackgroundFill(Color.BLUE, CornerRadii.EMPTY, Insets.EMPTY)));
            }
        }

        for (int i = 0; currentFile + i < currentFiles.size() && currentLabel + i < currentFilesOnScreen; i++) {
            assignLabel(labels[currentLabel + i], currentFiles.get(currentFile + i));

        }
        for (int i = 1; currentFile - i >= 0 && currentLabel - i >= 0; i++) {
            assignLabel(labels[currentLabel - i], currentFiles.get(currentFile - i));
        }
    }

    /**
     * Set correct text and text color to the label representing given file.
     */
    private void assignLabel(Label label, FileDescription fileDescription) {
        if (fileDescription.getIsDirectory()) {
            label.setTextFill(Color.WHITE);
        } else {
            label.setTextFill(Color.GRAY);
        }

        label.setText(fileDescription.getPath());
    }

    @Override
    public void stop() {
        try {
            client.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Connects client to the server he choosing.
     */
    private void connect(ActionEvent actionEvent) {
        // Create the custom dialog.
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Connect to server");
        dialog.setHeaderText("Type info to connect to server");

        ButtonType connectType = new ButtonType("Connect", ButtonBar.ButtonData.APPLY);
        dialog.getDialogPane().getButtonTypes().addAll(connectType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField ip = new TextField();
        ip.setPromptText("IP");
        TextField port = new TextField();
        port.setPromptText("Port");

        grid.add(new Label("Server IP:"), 0, 0);
        grid.add(ip, 1, 0);
        grid.add(new Label("Server port:"), 0, 1);
        grid.add(port, 1, 1);

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(ip::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == connectType) {
                return new Pair<>(ip.getText(), port.getText());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            try {
                client.connect(result.getKey(), Integer.valueOf(result.getValue()));

                path = FileSystems.getDefault().getPath(".");
                updateManager();
            } catch (NumberFormatException e) {
                showError("Port must be a number.");
            } catch (ConnectException e) {
                showError("Connection denied. Please check server IP and port.");
            } catch (IOException e) {
                showError("IO error: " + e.getMessage());
            }
        });
    }

    /**
     * Disconnecting user from server he is connected to. Does nothing if he is not connected to any.
     */
    private void disconnect(ActionEvent actionEvent) {
        try {
            client.disconnect();
            currentFiles = null;
            currentFile = 0;
            currentLabel = 0;
            for (var label : labels) {
                label.setText("");
            }
        } catch (IOException ignored) {
        }
    }

    /**
     * Show's alert with error with given message.
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Failed connect.");
        alert.setHeaderText("Error connecting to server.");
        alert.setContentText(message);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
