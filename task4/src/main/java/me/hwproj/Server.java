package me.hwproj;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.ServerError;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;


/**
 * Simple FTP-server.
 */
public class Server {
    /**
     * Buffer for reading in socket.
     */
    private static final int BUFFER_SIZE = 1024;

    /**
     * Thread that accepts new clients.
     */
    private Thread serverAcceptNewClientsThread;

    /**
     * Thread that reads data from client.
     */
    private Thread serverReadFromClientsThread;

    /**
     * Selector lock.
     */
    private final Lock selectorLock;

    /**
     * Selector for interacting with users.
     */
    private Selector selector;

    /**
     * Must accept exactly one argument --- path to the directory with server's files. If so, starts FTP server
     * on this directory.
     */
    public static void main(String[] argc) throws Exception {
        if (argc.length != 1) {
            throw new IllegalArgumentException("Daun vvedi argumenti normalno");
        }

        String pathToDir = argc[0];
        Server server = new Server(pathToDir);
        server.start();
        server.join();
        System.out.println("kekend");
    }

    /**
     * Path to directory with server's files.
     */
    @NotNull
    private String pathToDir;

    private Server(@NotNull String pathToDir) throws IOException {
        this.pathToDir = Paths.get(pathToDir).toAbsolutePath().toString();
        selectorLock = new ReentrantLock();
    }

    /**
     * starts the Server in 2 new threads. Use stop() to stop it.
     *
     * @throws IOException if can't create Selector
     */
    public void start() throws IOException {
        if (serverAcceptNewClientsThread == null && serverReadFromClientsThread == null) {
            selectorLock.lock();
            selector = Selector.open();
            selectorLock.unlock();
            serverAcceptNewClientsThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
                        serverSocketChannel.socket().bind(new InetSocketAddress(4242));
                        serverSocketChannel.configureBlocking(false);
                        while (!Thread.interrupted()) {
                            SocketChannel socketChannel = serverSocketChannel.accept();
                            if (socketChannel != null) {//do something with socketChannel...}}
                                // new client with socketChannel
                                socketChannel.configureBlocking(false);
                                socketChannel.socket().setTcpNoDelay(true);
                                selectorLock.lock();
                                socketChannel.register(selector, SelectionKey.OP_READ);
                                selectorLock.unlock();
                            }
                        }
                    } catch (IOException e) {
                        System.err.println("IOException in serverAcceptNewClientsThread");
                        System.exit(-1);
                    }
                }
            });
            serverReadFromClientsThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (!Thread.interrupted()) {
                            selectorLock.lock();
                            Selector currentSelector = selector;
                            selectorLock.unlock();
                            int readyChannelsCount = currentSelector.select(1000);
                            if (readyChannelsCount != 0) {
                                selectorLock.lock();
                                Set<SelectionKey> selectedKeys = currentSelector.selectedKeys();
                                selectorLock.unlock();
                                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
                                while (keyIterator.hasNext()) {
                                    SelectionKey key = keyIterator.next();
                                    if (key.isReadable()) {// a channel is ready for reading
                                        // TODO
                                        SocketChannel channel = (SocketChannel) key.channel(); // ??????????????????????????????????
                                        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                                        int bytesRead = channel.read(buffer);
                                        if (bytesRead % 2 != 0) {
                                            throw new IOException();
                                        }
                                        String query = buffer.toString().substring(0, bytesRead / 2);
                                    }
                                    keyIterator.remove();
                                }
                            }
                        }
                        serverAcceptNewClientsThread.start();
                        serverReadFromClientsThread.start();
                    } catch (IOException e) {
                        System.err.println("IOException in serverReadFromClientsThread");
                        System.exit(-1);
                    }
                }
            });
        } else {
            throw new ServerError("Server is already running", new Error());
        }
    }

    /**
     * Answers on user query and writes results to socket.
     * If query is <1: Int> <path: String>, returns <size: Int> (<name: String> <is_dir: Boolean>)*
     * If query is <2: Int> <path: String>, returns <size: Long> <content: Bytes>.
     *
     * Otherwise, or in case of any errors or wrong query parameters, writes "-1" to socket.
     */
    private void receiveQuery(@NotNull String query, @NotNull Socket socket) throws IOException {
        try (DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                Scanner scanner = new Scanner(query)) {

            if (!scanner.hasNextInt()) {
                outputStream.writeBytes("-1");
                return;
            }

            int command = scanner.nextInt();
            if (command != 1 && command != 2) {
                outputStream.writeBytes("-1");
                return;
            }

            if (command == 1) {
                String path = scanner.next();

                if (scanner.hasNext()) {
                    outputStream.writeBytes("-1");
                    return;
                }

                var fileList = list(path);
                if (fileList == null) {
                    outputStream.writeBytes("-1");
                    return;
                }

                outputStream.writeBytes(String.valueOf(fileList.size()));
                for (StringAndBoolean stringAndBoolean : fileList) {
                    outputStream.writeBytes(" " + stringAndBoolean.fileName + " ");
                    outputStream.writeBytes(stringAndBoolean.isDirectory ? "1" : "0");
                }
            } else {
                String path = scanner.next();

                if (scanner.hasNext()) {
                    outputStream.writeLong(-1);
                    return;
                }

                var sizeAndContent = get(path);

                if (sizeAndContent == null) {
                    outputStream.writeLong(-1);
                    return;
                }

                long size = sizeAndContent.size;
                InputStream inputStream = sizeAndContent.inputStream;



                outputStream.writeLong(size);

                byte[] buffer = new byte[1024];
                while (true) {
                    int length = inputStream.read(buffer);
                    if (length == -1 || length == 0) {
                        break;
                    }

                    outputStream.write(buffer, 0, length);
                }

                inputStream.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Stops the server and all active threads.
     */
    public void stop() {
        serverAcceptNewClientsThread.interrupt();
        serverReadFromClientsThread.interrupt();
        serverAcceptNewClientsThread = null;
        serverReadFromClientsThread = null;
    }

    /**
     * Accepts new client to FTP server.
     */
    public void join() throws Exception {
        if (serverAcceptNewClientsThread == null || serverReadFromClientsThread == null) {
            throw new Exception("Server is not started");
        }
        serverAcceptNewClientsThread.join();
        serverReadFromClientsThread.join();
    }

    /**
     * Lists all files in directory in format (fileName, isDirectory).
     * Returns null in case of any mistakes (such as IOexception or wrong pathName).
     */
    @Nullable
    private List<StringAndBoolean> list(@NotNull String pathName) {
        var fileList = new ArrayList<StringAndBoolean>();

        Path path = Paths.get(pathToDir, pathName);
        File file = path.toFile();

        if (!file.exists() || !file.isDirectory()) {
            return null;
        }

        try {
            for (var subFile : Files.walk(path).map(Path::toFile).collect(Collectors.toList())) {
                boolean isDirectory = subFile.isDirectory();
                String pathToSubFile = subFile.getAbsolutePath().substring(pathToDir.length());

                fileList.add(new StringAndBoolean(pathToSubFile, isDirectory));
            }

            return fileList;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Class for storing (pathName, isDirectory).
     */
    private static class StringAndBoolean {
        private String fileName;
        private boolean isDirectory;

        private StringAndBoolean(String fileName, boolean isDirectory) {
            this.fileName = fileName;
            this.isDirectory = isDirectory;
        }
    }

    /**
     * Returns (fileSize, InputStream of file) for a file for given name.
     *
     * Returns null in case of any mistake (such as IOexception or incorrect pathName).
     */
    @Nullable
    private SizeAndContent get(@NotNull String pathName) {
        Path path = Paths.get(pathToDir, pathName);
        File file = path.toFile();

        if (!file.exists() || !file.isFile() || !file.canRead()) {
            return null;
        }

        try {
            return new SizeAndContent(file.length(), new FileInputStream(file));
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    /**
     * Pair of (size, inputStream) corresponding to created file.
     */
    private static class SizeAndContent {
        private long size;
        private InputStream inputStream;

        private SizeAndContent(long size, InputStream inputStream) {
            this.size = size;
            this.inputStream = inputStream;
        }
    }
}
