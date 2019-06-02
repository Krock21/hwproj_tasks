import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
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
    private VBox content;

    /**
     * TODO
     */
    private double currentWidth;
    private double currentHeight;

    /**
     * TODO
     */
    private static final int INITIAL_WIDTH = 300;
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

        primaryStage.setTitle("My FTP client");

        content = new VBox();
        Scene scene = new Scene(content);
        //primaryStage.setFullScreen(true);
        //primaryStage.setResizable(false);

        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        screenWidth = primaryScreenBounds.getWidth();
        screenHeight = primaryScreenBounds.getHeight();
        fileHeight = screenHeight / FILES_PER_SCREEN;

        for (int i = 0; i < FILES_PER_SCREEN; i++) {
            labels[i] = new Label();
            labels[i].setText("Приличный текст");
            labels[i].setMinHeight(fileHeight);
            labels[i].setFont(Font.font(fileHeight));
        }

        //TODO
        primaryStage.setMinWidth(INITIAL_WIDTH);
        primaryStage.setMinHeight(MINIMAL_FILES_ON_SCREEN * fileHeight);

        content.widthProperty().addListener((observableValue, oldValue, newValue) -> setCurrentWidthToStage(newValue));
        content.heightProperty().addListener((observableValue, oldValue, newValue) -> setCurrentHeightToStage(newValue));

        /*primaryStage.setX(primaryScreenBounds.getMinX());
        primaryStage.setY(primaryScreenBounds.getMinY());
        primaryStage.setWidth(primaryScreenBounds.getWidth() - 50); //So it would be easy to drag window...
        primaryStage.setHeight(primaryScreenBounds.getHeight() - 50);
         */

        primaryStage.setScene(scene);

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

        content.setBackground(new Background(new BackgroundFill(Color.BLUE, CornerRadii.EMPTY, Insets.EMPTY)));

        primaryStage.show();

        currentWidth = content.getWidth();
        currentHeight = content.getHeight();

        System.out.println(currentHeight);
        resize();
        redraw();
    }

    /**
     * TODO
     */
    private void setCurrentWidthToStage(Number newWidth) {
        //primaryStage.setWidth((double) newWidth);
        currentWidth = (double) newWidth;
        resize();
    }

    /**
     * TODO
     */
    private void setCurrentHeightToStage(Number newHeight) {
        //primaryStage.setHeight((double) newHeight);
        currentHeight = (double) newHeight;
        resize();
    }

    /**
     * TODO
     */
    private void resize() {
        currentFilesOnScreen = (int) ((currentHeight / screenHeight) * FILES_PER_SCREEN);

        if (currentFile >= currentFilesOnScreen) {
            currentFile = currentFilesOnScreen - 1;
        }
        if (currentFile < 0) {
            currentFile = 0;
        }

        content.setFillWidth(true);
        var fileList = content.getChildren();
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
