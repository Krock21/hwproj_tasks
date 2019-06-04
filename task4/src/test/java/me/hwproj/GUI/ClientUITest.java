package me.hwproj.GUI;

import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import me.hwproj.Server;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.assertions.api.Assertions.assertThat;
import static org.testfx.util.NodeQueryUtils.hasText;

public class ClientUITest extends ApplicationTest {
    private Stage primaryStage;

    /**
     * TODO
     */
    private static boolean deleteDirectory(@NotNull File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }

        return directoryToBeDeleted.delete();
    }

    @Override
    public void start (Stage stage) throws Exception {
        this.primaryStage = stage;
        Parent root = FXMLLoader.load(ClientUIMain.class.getResource("../../../../resources/ClientUI.fxml"));
        stage.setScene(new Scene(root));
        stage.show();
        stage.toFront();
    }

    @BeforeAll
    static void start() throws IOException {
        new File("temp").mkdir();
    }

    @AfterAll
    static void clear() {
        deleteDirectory(new File("temp"));
    }

    @BeforeEach
    void setUp () {
    }

    @AfterEach
    void tearDown () throws Exception {
        FxToolkit.hideStage();
        release(new KeyCode[]{});
        release(new MouseButton[]{});
    }

    /**
     * TODo
     */
    private void pressConnect() {
        targetWindow("Connect to server").clickOn("#ip").write("127.0.0.1");
        targetWindow("Connect to server").clickOn("#port").write("4242");

        for (var node : targetWindow("Connect to server").lookup(".button").queryAll()) {
            if (node instanceof Button) {
                Button button = (Button) node;
                if (button.getText().equals("Connect")) {
                    clickOn(button);
                }
            }
        }
    }

    @Test
    void testSimple () throws Exception {
        String pathToRoot = "temp/testSimple";

        new File(pathToRoot).mkdirs();
        new File(pathToRoot + "/1.txt").createNewFile();

        var serverThread = new Thread (() -> {
            var server = new Server(pathToRoot);
            try {
                server.start();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                server.join();
            } catch (InterruptedException e) {
                server.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        serverThread.start();

        clickOn("#menu");
        clickOn("#connectToServer");

        pressConnect();

        verifyThat("#label1", hasText("1.txt"));
    }
}