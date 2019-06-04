package me.hwproj.GUI;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
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
import java.util.*;

/**
 * GUI for our FTP server.
 */
public class ClientUIMain extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("My FTP client");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("../../../../resources/ClientUI.fxml"));
        Parent root = loader.load();
        primaryStage.setScene(new Scene(root));
        ClientUIController controller = loader.getController();
        controller.initialiseStage(primaryStage);
        primaryStage.show();
    }



    /* TODo
    @Override
    public void stop() {
        try {
            client.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
     */

    public static void main(String[] args) {
        Application.launch(args);
    }
}
