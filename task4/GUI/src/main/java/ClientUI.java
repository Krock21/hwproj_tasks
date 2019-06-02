import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
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
    private static final int INITIAL_FILE_ON_SCREEN = 5;

    /**
     * TODO
     */
    private Label[] labels = new Label[FILES_PER_SCREEN];

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
            labels[i].setText("Хуесосина");
            labels[i].setMinHeight(fileHeight);
            labels[i].setFont(Font.font(fileHeight));
        }

        //TODO
        primaryStage.setMinWidth(INITIAL_WIDTH);
        primaryStage.setMinHeight(INITIAL_FILE_ON_SCREEN * fileHeight);

        currentWidth = primaryStage.getWidth();
        currentHeight = primaryStage.getHeight();

        resize();

        content.widthProperty().addListener((observableValue, oldValue, newValue) -> setCurrentWidthToStage(newValue));
        content.heightProperty().addListener((observableValue, oldValue, newValue) -> setCurrentHeightToStage(newValue));

        /*primaryStage.setX(primaryScreenBounds.getMinX());
        primaryStage.setY(primaryScreenBounds.getMinY());
        primaryStage.setWidth(primaryScreenBounds.getWidth() - 50); //So it would be easy to drag window...
        primaryStage.setHeight(primaryScreenBounds.getHeight() - 50);
         */

        primaryStage.setScene(scene);
        primaryStage.show();
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

        content.setFillWidth(true);
        var fileList = content.getChildren();
        fileList.clear();
        fileList.addAll(Arrays.asList(labels).subList(0, currentFilesOnScreen));
    }

    @Override
    public void stop() {
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
