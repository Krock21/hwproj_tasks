package me.hwproj.GUI;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
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
import java.util.Stack;

public class ClientUIController {
    /**
     * Main stage of the window.
     */
    private Stage primaryStage;

    /**
     * Maximum number of files per screen (if screen height is maximum).
     */
    public static final int FILES_PER_SCREEN = 20;

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
     * Height of one file on the screen (fixed to screenHeight).
     */
    private double fileHeight;

    /**
     * Number of files existing on screen (fixed to current fileMenu's height).
     */
    private int currentFilesOnScreen;


    /**
     * List of files.
     */
    @FXML
    private VBox fileMenu;

    /**
     * All labels with files on the screen (not all of them may be present).
     */
    @FXML
    private Label label1;

    @FXML
    private Label label2;

    @FXML
    private Label label3;

    @FXML
    private Label label4;

    @FXML
    private Label label5;

    @FXML
    private Label label6;

    @FXML
    private Label label7;

    @FXML
    private Label label8;

    @FXML
    private Label label9;

    @FXML
    private Label label10;

    @FXML
    private Label label11;

    @FXML
    private Label label12;

    @FXML
    private Label label13;

    @FXML
    private Label label14;

    @FXML
    private Label label15;

    @FXML
    private Label label16;

    @FXML
    private Label label17;

    @FXML
    private Label label18;

    @FXML
    private Label label19;

    @FXML
    private Label label20;

    /**
     * All label's that will show files (array with labels above. Cannot initialize it here because FXML does not allow it).
     */
    private Label[] labels;
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

    /**
     * Main app's pane.
     */
    @FXML
    private BorderPane pane;

    /**
     * When user goes into directory, his position in parent directory saves in this stack.
     */
    private Stack<LabelAndFile> prevPositions = new Stack<>();

    /**
     * Method to call on the app's closing.
     */
    void stop() {
        try {
            client.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Pair of label and file positions.
     */
    private static class LabelAndFile {
        private int label;
        private int file;

        private LabelAndFile(int label, int file) {
            this.label = label;
            this.file = file;
        }


        private int getLabel() {
            return label;
        }

        private int getFile() {
            return file;
        }
    }

    /**
     * Bar on the top of the app.
     */
    @FXML
    private MenuBar menuBar;

    /**
     *
     */
    public void initialiseStage(Stage stage) {
        primaryStage = stage;

        primaryStage.setMinWidth(INITIAL_WIDTH);
        primaryStage.setMinHeight(MINIMAL_FILES_ON_SCREEN * fileHeight);

        primaryStage.setWidth(INITIAL_WIDTH);
        primaryStage.setHeight(MINIMAL_FILES_ON_SCREEN * fileHeight);

        primaryStage.heightProperty().addListener((observableValue, oldValue, newValue) -> onStageHeightChange(newValue));

        primaryStage.getScene().setOnKeyPressed(event -> {
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
                    if (currentFiles == null || currentFiles.size() == 0) {
                        break;
                    }

                    FileDescription file = currentFiles.get(currentFile);

                    if (file.getPath().equals("../")) {
                        path = path.getParent();

                        var prevPosition = prevPositions.pop();
                        if (prevPosition != null) {
                            currentFile = prevPosition.getFile();
                            currentLabel = Math.min(prevPosition.getLabel(), currentFilesOnScreen - 1);
                        } else {
                            currentFile = 0;
                            currentLabel = 0;
                        }

                        updateManager();
                    } else if (file.getIsDirectory()) {
                        path = FileSystems.getDefault().getPath(path.toString(), file.getPath() + "/");
                        prevPositions.push(new LabelAndFile(currentLabel, currentFile));
                        currentFile = 1;
                        currentLabel = 1;
                        updateManager();
                    } else {
                        //var fileChooser = new FileChooser();
                        fileChooser.setInitialFileName(file.getPath());
                        File fileToSave = fileChooser.showSaveDialog(primaryStage);

                        if (fileToSave != null) {
                            System.out.println("okokok");

                            try {
                                client.executeGet(FileSystems.getDefault().getPath(path.toString(), file.getPath()).toString(), fileToSave);
                            } catch (IOException e) {
                                showError("IO error: " + e.getMessage());
                            }
                        } else {
                            System.out.println("wtf");
                        }
                    }

                    break;
            }
        });
    }

    /**
     * File chooser that will show save dialogs to the user.
     *
     * I could came up with some legend why it can be here with public setter...
     * but actually I just need it to mock.
     */
    private FileChooser fileChooser = new FileChooser();

    public void setFileChooser(FileChooser fileChooser) {
         this.fileChooser = fileChooser;
    }

    /**
     * Initializes object's on screen.
     */
    @FXML
    public void initialize() {
        labels = new Label[] {label1, label2, label3, label4, label5, label6, label7, label8, label9, label10,
                label11, label12, label13, label14, label15, label16, label17, label18, label19, label20};

        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        fileHeight = (primaryScreenBounds.getHeight() - menuBar.getMaxHeight() - 1) / FILES_PER_SCREEN;

        fileMenu.getChildren().clear();

        for (int i = 0; i < FILES_PER_SCREEN; i++) {
            labels[i].setMinHeight(fileHeight);
            labels[i].setMaxHeight(fileHeight);
            labels[i].setFont(Font.font(fileHeight));
        }

        fileMenu.setFillWidth(true);
        fileMenu.setBackground(new Background(new BackgroundFill(Color.BLUE, CornerRadii.EMPTY, Insets.EMPTY)));
        fileMenu.heightProperty().addListener((observableValue, oldValue, newValue) -> onFileMenuHeightChange(newValue));
        reassignFileMenu();
    }

    /**
     * Unbind and bind again fileMenu so it would change it's height to fill the parent.
     */
    private void reassignFileMenu() {
        //fileMenu = new VBox();
        pane.setCenter(null);
        pane.setCenter(fileMenu);
    }

    /**
     * Updates current state after changing the working directory.
     */
    private void updateManager() {
        try {
            currentFiles = client.executeList(path.toString());
        } catch (IOException e) {
            showError("Error during execution: " + e.getMessage());
        }

        if (path.getParent() != null) {
            currentFiles.add(new FileDescription("../", true));
        }
        if (currentFiles.size() == 1) {
            currentLabel = 0;
            currentFile = 0;
        }
        sorted = false;
        redraw();
    }

    /**
     * Handles changing of the screen's height.
     */
    private void onStageHeightChange(Number newHeight) {
        reassignFileMenu();
    }

    /**
     * Handles changing of the fileMenu height.
     */
    private void onFileMenuHeightChange(Number newHeight) {
        redraw();
    }

    /**
     * Changes number of label presented on the screen after height's changes.
     */
    private void resize() {
        currentFilesOnScreen = Math.min((int) (fileMenu.getHeight() / fileHeight), 20);

        if (currentLabel >= currentFilesOnScreen) {
            currentLabel = currentFilesOnScreen - 1;
        }
        if (currentLabel < 0) {
            currentLabel = 0;
        }
    }

    /**
     * Set's correct text and color to the label's on the screen.
     */
    private void redraw() {
        resize();

        fileMenu.getChildren().clear();

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
        fileMenu.getChildren().addAll(Arrays.asList(labels).subList(0, currentFilesOnScreen));

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

    /**
     * Connects client to the server he choosing.
     */
    @FXML
    private void connect(ActionEvent actionEvent) {
        disconnect(null);

        // Create the custom dialog.
        var dialog = new Dialog<Pair<String, String>>();
        dialog.setTitle("Connect to server");
        dialog.setHeaderText("Type info to connect to server");

        var connectType = new ButtonType("Connect", ButtonBar.ButtonData.APPLY);
        dialog.getDialogPane().getButtonTypes().addAll(connectType, ButtonType.CANCEL);

        var grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        var ip = new TextField();
        ip.setPromptText("IP");
        ip.setId("ip");
        var port = new TextField();
        port.setId("port");
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

                currentFile = 0;
                currentLabel = 0;
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
    @FXML
    private void disconnect(ActionEvent actionEvent) {
        try {
            client.disconnect();
            currentFiles = null;
            currentFile = 0;
            currentLabel = 0;
            prevPositions.clear();
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
        var alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Failed connect.");
        alert.setHeaderText("Error connecting to server.");
        alert.setContentText(message);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }
}
