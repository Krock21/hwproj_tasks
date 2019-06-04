package me.hwproj;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/** Simple FTP-server that allows showing list of files in a directory and loading files */
public class Server {
    /** Thread that accepts new clients */
    private Thread serverAcceptNewClientsThread;
    /** Thread that reads data from client */
    private Thread serverReadFromClientsThread;
    private final Lock selectorLock;
    /** Selector for interacting with users */
    private Selector selector;
    /** Path to directory with server's files */
    @NotNull
    private String pathToDir;

    /**
     * Port of the server for user to connect.
     */
    private static final int SERVER_PORT = 4242;

    /**
     * Must accept exactly one argument --- path to the directory with server's files.
     * If so, starts FTP server on this directory.
     */
    public static void main(String[] argc) throws Exception {
        if (argc.length != 1) {
            throw new IllegalArgumentException("Should take one argument: path to root");
        }

        String pathToDir = argc[0];
        Server server = new Server(pathToDir);
        server.start();
        server.join();
    }

    public Server(@NotNull String pathToDir) {
        this.pathToDir = Paths.get(pathToDir).toAbsolutePath().toString();
        selectorLock = new ReentrantLock();
    }

    /**
     * Starts the Server in 2 new threads. Use stop() to stop it.
     * @throws IOException if can't create Selector
     */
    public void start() throws IOException {
        selector = Selector.open();
        serverAcceptNewClientsThread = new Thread(() -> {
            try {
                ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
                serverSocketChannel.socket().bind(new InetSocketAddress(SERVER_PORT));
                serverSocketChannel.configureBlocking(false);
                while (!Thread.interrupted()) {
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    if (socketChannel != null) {
                        // new client with socketChannel
                        socketChannel.configureBlocking(false);
                        socketChannel.socket().setTcpNoDelay(true);
                        selectorLock.lock();
                        socketChannel.register(selector, SelectionKey.OP_READ,
                                new ClientData(socketChannel, this));
                        selectorLock.unlock();
                    }
                }

                serverSocketChannel.close();
            } catch (IOException e) {
                System.err.println("IOException in serverAcceptNewClientsThread "  + e.getMessage());
                System.exit(-1);
            }
        });
        serverReadFromClientsThread = new Thread(() -> {
            try {
                while (!Thread.interrupted()) {
                    int readyChannelsCount = selector.select(1000);
                    if (readyChannelsCount != 0) {
                        selectorLock.lock();
                        Set<SelectionKey> selectedKeys = selector.selectedKeys();
                        selectorLock.unlock();
                        Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
                        while (keyIterator.hasNext()) {
                            SelectionKey key = keyIterator.next();
                            if (key.isReadable()) { // a channel is ready for reading
                                var client = (ClientData) key.attachment();
                                client.read();
                            }
                            keyIterator.remove();
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("IOException in serverReadFromClientsThread");
                System.exit(-1);
            }
        });
        serverAcceptNewClientsThread.start();
        serverReadFromClientsThread.start();

        String ip;
        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            ip = socket.getLocalAddress().getHostAddress();
        } catch (Exception e) {
            ip = "UNKNOWN";
        }

        System.out.println("Ok. Server started. Server IP is " + ip + ", server port is " + SERVER_PORT + ".");
    }

    /** Stops the server and all active threads */
    public void stop() {
        if (serverAcceptNewClientsThread != null) {
            serverAcceptNewClientsThread.interrupt();
        }

        if (serverReadFromClientsThread != null) {
            serverReadFromClientsThread.interrupt();
        }

        serverAcceptNewClientsThread = null;
        serverReadFromClientsThread = null;

        System.out.println("Server stopped.");
    }

    /** Accepts new client to FTP server */
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
    @Nullable List<FileDescription> list(@NotNull String pathName) {
        var fileList = new ArrayList<FileDescription>();

        Path path = Paths.get(pathToDir, pathName);
        File file = path.toFile();

        if (!file.exists() || !file.isDirectory()) {
            return null;
        }

        for (var subFile : Objects.requireNonNull(file.listFiles())) {
            boolean isDirectory = subFile.isDirectory();
            String fileName = subFile.getName();
            fileList.add(new FileDescription(fileName, isDirectory));
        }

        return fileList;
    }

    /**
     * Returns (fileSize, InputStream of file) for a file for given name.
     * <p>
     * Returns null in case of any mistake (such as IOexception or incorrect pathName)
     */
    @Nullable SizeAndContent get(@NotNull String pathName) {
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

    /** Pair of (size, inputStream) corresponding to created file */
    static class SizeAndContent {
        private long size;
        @NotNull private InputStream inputStream;

        private SizeAndContent(long size, @NotNull InputStream inputStream) {
            this.size = size;
            this.inputStream = inputStream;
        }

        long getSize() {
            return size;
        }

        @NotNull InputStream getInputStream() {
            return inputStream;
        }
    }
}
