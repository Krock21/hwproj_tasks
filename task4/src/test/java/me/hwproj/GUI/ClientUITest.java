package me.hwproj.GUI;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import me.hwproj.Server;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static javafx.scene.input.KeyCode.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.util.NodeQueryUtils.hasText;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class ClientUITest extends ApplicationTest {
    /**
     * ID's of all labels on the choose file screen.
     */
    private static String[] labelNames;

    /**
     * Main stage app is run on.
     */
    private Stage primaryStage;

    /**
     * Stages's controller object.
     */
    private ClientUIController controller;

    /**
     * Thread on which server will run on.
     */
    private Thread serverThread;

    /**
     * Deletes all files in directory (recursievly) and directory itself.
     */
    public static void deleteDirectory(@NotNull File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }

        directoryToBeDeleted.delete();
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;

        var loader = new FXMLLoader(ClientUIMain.class.getResource("ClientUI.fxml"));
        Parent root = loader.load();
        stage.setScene(new Scene(root));
        controller = loader.getController();
        controller.initialiseStage(stage);

        stage.setFullScreen(true);
        stage.show();
        stage.toFront();
    }

    @BeforeAll
    static void initialize() {
        labelNames = new String[ClientUIController.FILES_PER_SCREEN];
        for (int i = 0; i < labelNames.length; i++) {
            labelNames[i] = "#label" + (i + 1);
        }

        new File("temp").mkdir();
    }

    @AfterAll
    static void clear() {
        deleteDirectory(new File("temp"));
    }

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() throws Exception {
        FxToolkit.hideStage();
        release(new KeyCode[]{});
        release(new MouseButton[]{});
        if (serverThread != null && !serverThread.isInterrupted()) {
            serverThread.interrupt();
        }
    }

    /**
     * Connects user to local server.
     */
    private void pressConnect() {
        clickOn("#menu");
        clickOn("#connectToServer");

        targetWindow("Connect to server").clickOn("#ip").write("127.0.0.1");
        targetWindow("Connect to server").clickOn("#port").write("4242");

        clickButton("Connect to server", "Connect");
    }

    /**
     * Starts thread with local server.
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

    /**
     * Returns true if label with given ID is currently on focus (meaning have CYAN color).
     */
    private boolean labelOnFocus(String id) {
        Label label = lookup(id).query();
        if (!label.getBackground().getFills().get(0).getFill().equals(Color.CYAN)) {
            return false;
        }

        for (String labelName : labelNames) {
            if (labelName.equals(id)) {
                continue;
            }

            if (!((Label) lookup(labelName).query()).getBackground().getFills().get(0).getFill().equals(Color.BLUE)) {
                return false;
            }
        }

        return true;
    }

    @Test
    void allLabelsPresentedOnScreen() {
        String pathToRoot = "temp/";
        new File(pathToRoot).mkdirs();

        startServer(pathToRoot);
        pressConnect();

        Label label20 = lookup("#label20").query();
        assertNotNull(label20);
    }

    @Test
    void testCorrectInteractionOnSmallServer() throws Exception {
        String pathToRoot = "temp/testSimple";

        new File(pathToRoot).mkdirs();
        new File(pathToRoot + "/1.txt").createNewFile();
        new File(pathToRoot + "/2.txt").createNewFile();
        new File(pathToRoot + "/1_folder").mkdir();
        new File(pathToRoot + "/1_folder/3.txt").createNewFile();
        new File(pathToRoot + "/2_folder").mkdir();

        startServer(pathToRoot);
        pressConnect();

        assertTrue(labelOnFocus("#label1"));

        verifyThat("#label1", hasText("1_folder"));
        verifyThat("#label2", hasText("2_folder"));
        verifyThat("#label3", hasText("1.txt"));
        verifyThat("#label4", hasText("2.txt"));

        push(ENTER);

        verifyThat("#label1", hasText("../"));
        verifyThat("#label2", hasText("3.txt"));

        assertTrue(labelOnFocus("#label2"));

        push(UP);

        assertTrue(labelOnFocus("#label1"));

        push(ENTER);

        assertTrue(labelOnFocus("#label1"));

        verifyThat("#label1", hasText("1_folder"));
        verifyThat("#label2", hasText("2_folder"));
        verifyThat("#label3", hasText("1.txt"));
        verifyThat("#label4", hasText("2.txt"));

        for (int i = 0; i < 10; i++) {
            push(UP);
        }

        verifyThat("#label1", hasText("1_folder"));
        verifyThat("#label2", hasText("2_folder"));
        verifyThat("#label3", hasText("1.txt"));
        verifyThat("#label4", hasText("2.txt"));

        for (int i = 0; i < 10; i++) {
            push(DOWN);
        }
        verifyThat("#label1", hasText("1_folder"));
        verifyThat("#label2", hasText("2_folder"));
        verifyThat("#label3", hasText("1.txt"));
        verifyThat("#label4", hasText("2.txt"));
        assertTrue(labelOnFocus("#label4"));
    }

    @Test
    void testCorrectInteractionOnServerWithTooManyFilesInFolder() throws Exception {
        String pathToRoot = "temp/testBig";

        new File(pathToRoot).mkdirs();

        int n = 50;

        String[] filenames = new String[n];
        for (int i = 0; i < n; i++) {
            filenames[i] = String.valueOf(1000 + i); //They will be correctly sorted.
            new File(pathToRoot + "/" + filenames[i]).createNewFile();
        }

        startServer(pathToRoot);
        pressConnect();

        assertTrue(labelOnFocus("#label1"));

        for (int i = 0; i < ClientUIController.FILES_PER_SCREEN; i++) {
            verifyThat(labelNames[i], hasText(filenames[i]));
        }

        for (int i = 1; i < ClientUIController.FILES_PER_SCREEN; i++) {
            push(DOWN);
            assertTrue(labelOnFocus(labelNames[i]));

            for (int j = 0; j < ClientUIController.FILES_PER_SCREEN; j++) {
                verifyThat(labelNames[j], hasText(filenames[j]));
            }
        }

        for (int i = ClientUIController.FILES_PER_SCREEN; i < n; i++) {
            push(DOWN);
            assertTrue(labelOnFocus(labelNames[ClientUIController.FILES_PER_SCREEN - 1]));

            for (int j = 0; j < ClientUIController.FILES_PER_SCREEN; j++) {
                verifyThat(labelNames[j], hasText(filenames[i - ClientUIController.FILES_PER_SCREEN + j + 1]));
            }
        }

        for (int q = 0; q < 5; q++) {
            push(DOWN);
            for (int i = 0; i < ClientUIController.FILES_PER_SCREEN; i++) {
                verifyThat(labelNames[i], hasText(filenames[n - ClientUIController.FILES_PER_SCREEN + i]));
            }
        }

        for (int i = ClientUIController.FILES_PER_SCREEN - 2; i >= 0; i--) {
            push(UP);
            assertTrue(labelOnFocus(labelNames[i]));
        }

        for (int i = n - ClientUIController.FILES_PER_SCREEN; i > 0; i--) {
            push(UP);
            assertTrue(labelOnFocus(labelNames[0]));

            for (int j = 0; j < ClientUIController.FILES_PER_SCREEN; j++) {
                verifyThat(labelNames[j], hasText(filenames[i + j - 1]));
            }
        }

        for (int q = 0; q < 5; q++) {
            push(UP);
            for (int i = 0; i < ClientUIController.FILES_PER_SCREEN; i++) {
                verifyThat(labelNames[i], hasText(filenames[i]));
            }
        }
    }

    /**
     * Returns true if label marked as directory (text color is WHITE)
     */
    private boolean isDir(String labelid) {
        return ((Label) lookup(labelid).query()).getTextFill().equals(Color.WHITE);
    }

    /**
     * Returns true if label marked as file (text color is GRAY)
     */
    private boolean isFile(String labelid) {
        return ((Label) lookup(labelid).query()).getTextFill().equals(Color.GRAY);
    }

    @Test
    void testCorrectOrderAndColoringOfDirectoryAndFiles() throws Exception {
        String pathToRoot = "temp/testDirectories";
        new File(pathToRoot).mkdirs();

        new File(pathToRoot + "/folder/1").mkdirs();
        new File(pathToRoot + "/folder/2").createNewFile();
        new File(pathToRoot + "/folder/3").createNewFile();
        new File(pathToRoot + "/folder/4").mkdirs();
        new File(pathToRoot + "/folder/5").mkdirs();
        new File(pathToRoot + "/folder/6").createNewFile();

        startServer(pathToRoot);
        pressConnect();

        assertTrue(isDir("#label1"));
        verifyThat("#label1", hasText("folder"));
        push(ENTER);
        verifyThat("#label1", hasText("../"));

        assertTrue(isDir("#label2"));
        verifyThat("#label2", hasText("1"));

        assertTrue(isDir("#label3"));
        verifyThat("#label3", hasText("4"));

        assertTrue(isDir("#label4"));
        verifyThat("#label4", hasText("5"));

        assertTrue(isFile("#label5"));
        verifyThat("#label5", hasText("2"));

        assertTrue(isFile("#label6"));
        verifyThat("#label6", hasText("3"));

        assertTrue(isFile("#label7"));
        verifyThat("#label7", hasText("6"));
    }

    @Test
    void emptyFolderCorrectlyWorks() {
        String pathToRoot = "temp/testPreviousFolder";
        new File(pathToRoot).mkdirs();

        new File(pathToRoot + "/folder").mkdirs();

        startServer(pathToRoot);
        pressConnect();

        push(ENTER);

        verifyThat("#label1", hasText("../"));
        assertTrue(labelOnFocus("#label1"));
        verifyThat("#label2", hasText(""));

        push(ENTER);
        verifyThat("#label1", hasText("folder"));
        verifyThat("#label2", hasText(""));
    }

    @Test
    void getBackReturnsToPreviousFolderCorrectly() {
        String pathToRoot = "temp/testPreviousFolder";
        new File(pathToRoot).mkdirs();

        new File(pathToRoot + "/1_folder").mkdirs();
        new File(pathToRoot + "/2_folder").mkdirs();
        new File(pathToRoot + "/3_folder").mkdirs();

        new File(pathToRoot + "/2_folder/1_folder").mkdirs();
        new File(pathToRoot + "/2_folder/2_folder").mkdirs();
        new File(pathToRoot + "/2_folder/3_folder").mkdirs();
        new File(pathToRoot + "/2_folder/4_folder").mkdirs();

        startServer(pathToRoot);
        pressConnect();


        push(DOWN);
        push(ENTER);
        push(DOWN);
        push(DOWN);
        push(DOWN);
        push(ENTER);

        push(ENTER);
        assertTrue(labelOnFocus("#label5"));
        verifyThat("#label5", hasText("4_folder"));
        push(UP);
        push(UP);
        push(UP);
        push(UP);
        push(ENTER);
        assertTrue(labelOnFocus("#label2"));
        verifyThat("#label2", hasText("2_folder"));
    }

    @Test
    void disconnectAndReconnectCorrectlyWorks()  {
        String pathToRoot = "temp/testReconnect";
        new File(pathToRoot).mkdirs();

        new File(pathToRoot + "/folder").mkdirs();

        startServer(pathToRoot);
        pressConnect();

        verifyThat("#label1", hasText("folder"));

        clickOn("#menu");
        clickOn("#disconnectFromServer");

        pressConnect();
        verifyThat("#label1", hasText("folder"));

        pressConnect();
        verifyThat("#label1", hasText("folder"));
    }

    @Test
    void downloadFileCorrectlyWorks() throws IOException {
        String pathToRoot = "temp/testDownload";
        new File(pathToRoot).mkdirs();

        int n = 4;
        var files = new File[n];
        var savedFiles = new File[n];

        for (int i = 0; i < n; i++) {
            files[i] = new File(pathToRoot + "/" + i);

            try (var outputStream = new FileOutputStream(files[i])) {
                outputStream.write(i);
                outputStream.flush();
            }

            savedFiles[i] = new File(pathToRoot + "/" + (n + i));
        }

        startServer(pathToRoot);
        pressConnect();

        FileChooser mockedChooser = Mockito.mock(FileChooser.class);
        when(mockedChooser.showSaveDialog(primaryStage)).thenReturn(savedFiles[0], savedFiles[1], savedFiles[2], savedFiles[3]);

        controller.setFileChooser(mockedChooser);

        push(ENTER);

        for (int i = 1; i < n; i++) {
            push(DOWN);
            push(ENTER);
        }

        int[] result = new int[n];
        for (int i = 0; i < n; i++) {
            try (var inputStream = new FileInputStream(savedFiles[i])){
                result[i] = inputStream.read();
            }
        }

        for (int i = 0; i < n; i++) {
            assertEquals(i, result[i]);
        }
    }

    @Test
    void wrongIpGivesErrorAlert() {
        clickOn("#menu");
        clickOn("#connectToServer");

        targetWindow("Connect to server").clickOn("#ip").write("NOT IP");
        targetWindow("Connect to server").clickOn("#port").write("4242");

        clickButton("Connect to server", "Connect");

        assertDoesNotThrow(() -> clickButton("Failed to connect", "OK"));
    }

    @Test
    void cancelButtonWorks() {
        String pathToRoot = "temp/testCancel";
        new File(pathToRoot).mkdirs();
        new File(pathToRoot + "/folder").mkdirs();

        startServer(pathToRoot);

        clickOn("#menu");
        clickOn("#connectToServer");

        clickButton("Connect to server", "Cancel");

        pressConnect();
        verifyThat("#label1", hasText("folder"));
    }

    /**
     * It's impossible to query button by IDs on Dialogs and Alerts, this method does it by button's text.
     */
    private void clickButton(String windowTitle, String buttonText) {
        for (var node : targetWindow(windowTitle).lookup(".button").queryAll()) {
            if (node instanceof Button) {
                var button = (Button) node;
                if (button.getText().equals(buttonText)) {
                    clickOn(button);
                    break;
                }
            }
        }
    }
}