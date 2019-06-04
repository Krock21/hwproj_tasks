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

import static javafx.scene.input.KeyCode.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.assertions.api.Assertions.assertThat;
import static org.testfx.util.NodeQueryUtils.hasText;

public class ClientUITest extends ApplicationTest {
    private Stage primaryStage;

    /**
     * TODO
     */
    private Thread serverThread;

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

        FXMLLoader loader = new FXMLLoader(getClass().getResource("../../../../resources/ClientUI.fxml"));
        Parent root = loader.load();
        primaryStage.setScene(new Scene(root));
        ClientUIController controller = loader.getController();
        controller.initialiseStage(primaryStage);

        primaryStage.setFullScreen(true);
        primaryStage.show();
        primaryStage.toFront();
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
        if (serverThread != null && !serverThread.isInterrupted()) {
            serverThread.interrupt();
            serverThread.join();
        }
    }

    /**
     * TODo
     */
    private void pressConnect() {
        clickOn("#menu");
        clickOn("#connectToServer");

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

    /**
     * TODo
     */
    private void startServer(String pathToRoot) {
        serverThread = new Thread (() -> {
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
                server.stop();
            }

            server.stop();
        });
        serverThread.start();
    }

    @Test
    void allLabelsPresentedOnScreen() {
        String pathToRoot = "temp/";
        startServer(pathToRoot);
        pressConnect();

        Label label20 = lookup("#label20").query();
        assertNotNull(label20);
    }

    @Test
    void testSimple() throws Exception {
        String pathToRoot = "temp/testSimple";

        new File(pathToRoot).mkdirs();
        new File(pathToRoot + "/1.txt").createNewFile();
        new File(pathToRoot + "/2.txt").createNewFile();
        new File(pathToRoot + "/folder").mkdir();
        new File(pathToRoot + "/folder/3.txt").createNewFile();

        startServer(pathToRoot);
        pressConnect();

        verifyThat("#label1", hasText("folder"));
        verifyThat("#label2", hasText("1.txt"));
        verifyThat("#label3", hasText("2.txt"));

        push(ENTER);

        verifyThat("#label1", hasText("../"));
        verifyThat("#label2", hasText("3.txt"));

        push(UP);
        push(ENTER);

        verifyThat("#label1", hasText("folder"));
        verifyThat("#label2", hasText("1.txt"));
        verifyThat("#label3", hasText("2.txt"));
    }
}