package me.hwproj.GUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * GUI for our FTP server.
 */
public class ClientUIMain extends Application {
    /**
     * App's controller.
     */
    private ClientUIController controller;

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("My FTP client");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("../../../../resources/ClientUI.fxml"));
        Parent root = loader.load();
        primaryStage.setScene(new Scene(root));
        controller = loader.getController();
        controller.initialiseStage(primaryStage);
        primaryStage.show();
    }

    @Override
    public void stop() {
        controller.stop();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
