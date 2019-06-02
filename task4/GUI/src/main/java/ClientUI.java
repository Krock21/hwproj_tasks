import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * TODO
 */
public class ClientUI extends Application {
        /**
         * Main stage of the window.
         */
        private Stage primaryStage;

        @Override
        public void start(Stage primaryStage) throws Exception {
            this.primaryStage = primaryStage;

            primaryStage.setTitle("My FTP client");

            Group root = new Group();
            Scene scene = new Scene(root);
            //primaryStage.setFullScreen(true);
            //primaryStage.setResizable(false);

            primaryStage.setMinWidth(300);
            primaryStage.setMinHeight(300);

            primaryStage.widthProperty().addListener((observableValue, oldValue, newValue) -> setCurrentWidthToStage(newValue));
            primaryStage.heightProperty().addListener((observableValue, oldValue, newValue) -> setCurrentHeightToStage(newValue));

        /*Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setX(primaryScreenBounds.getMinX());
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
            primaryStage.setWidth((double) newWidth);
        }

        /**
         * TODO
         */
        private void setCurrentHeightToStage(Number newHeight) {
            primaryStage.setHeight((double) newHeight);
        }

        @Override
        public void stop() {
        }

        public static void main(String[] args) {
            Application.launch(args);
        }
}
