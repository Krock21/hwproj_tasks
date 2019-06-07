package me.hwproj;

import me.hwproj.GUI.ClientUITest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClientTest {
    private Thread serverThread;

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

    @BeforeAll
    static void initialize() {
        new File("temp").mkdir();
    }

    @AfterAll
    static void clear() {
        ClientUITest.deleteDirectory(new File("temp"));
    }

    @AfterEach
    void tearDown() {
        if (serverThread != null && !serverThread.isInterrupted()) {
            serverThread.interrupt();
        }
    }

    @Test
    void main() throws IOException, InterruptedException {
        String pathToRoot = "temp/severalClientsTest";
        new File(pathToRoot).mkdirs();

        int n = 100;
        String[] fileNames = new String[n];
        for (int i = 0; i < n; i++) {
            fileNames[i] = String.valueOf(1000 + i);
            new File(pathToRoot + "/" + fileNames[i]).createNewFile();
        }

        startServer(pathToRoot);
        Thread.sleep(4000); //Wait for server to set up

        int m = 20;
        var threads = new Thread[m];
        for (int i = 0; i < m; i++) {
            threads[i] = new Thread(() -> {
               Client client = new Client();
                try {
                    client.connect("127.0.0.1", 4242);
                } catch (IOException e) {
                    e.printStackTrace();
                    fail();
                }

                while (!Thread.interrupted()) {
                    List<FileDescription> fileDescriptionList = new ArrayList<>();
                    try {
                        fileDescriptionList = client.executeList(".");
                    } catch (IOException e) {
                        e.printStackTrace();
                        fail();
                    }

                    assertEquals(n, fileDescriptionList.size());

                    fileDescriptionList.sort((fd1, fd2) -> {
                        if (fd1.getIsDirectory() == fd2.getIsDirectory()) {
                            return fd1.getPath().equals("../") ? -1 : fd1.getPath().compareTo(fd2.getPath());
                        } else {
                            return fd2.getIsDirectory() ? 1 : -1;
                        }
                    });

                    boolean result = true;
                    for (int j = 0; j < n; j++) {
                        if (!fileNames[j].equals(fileDescriptionList.get(j).getPath())) {
                            result = false;
                        }
                    }

                    assertTrue(result);
                }
            });

            threads[i].start();
        }

        Thread.sleep(8000);

        for (int i = 0; i < m; i++) {
            threads[i].interrupt();
        }
    }
}