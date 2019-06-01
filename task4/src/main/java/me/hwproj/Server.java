package me.hwproj;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
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


/**
 * Simple FTP-server.
 */
public class Server {
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
            throw new IllegalArgumentException("Should take one argument: path to root");
        }

        String pathToDir = argc[0];
        Server server = new Server(pathToDir);
        server.start();
        server.join();
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
        selector = Selector.open();
        serverAcceptNewClientsThread = new Thread(() -> {
            try {
                ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
                serverSocketChannel.socket().bind(new InetSocketAddress(4242));
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
            } catch (IOException e) {
                System.err.println("IOException in serverAcceptNewClientsThread");
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
    private void join() throws Exception {
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
    public List<FileDescriprion> list(@NotNull String pathName) {
        var fileList = new ArrayList<FileDescriprion>();

        Path path = Paths.get(pathToDir, pathName);
        File file = path.toFile();

        if (!file.exists() || !file.isDirectory()) {
            return null;
        }

        for (var subFile : Objects.requireNonNull(file.listFiles())) {
            boolean isDirectory = subFile.isDirectory();
            String fileName = subFile.getName();
            fileList.add(new FileDescriprion(fileName, isDirectory));
        }

        return fileList;
    }

    /**
     * Returns (fileSize, InputStream of file) for a file for given name.
     * <p>
     * Returns null in case of any mistake (such as IOexception or incorrect pathName).
     */
    @Nullable
    public SizeAndContent get(@NotNull String pathName) {
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
    public static class SizeAndContent {
        private long size;
        @NotNull private InputStream inputStream;

        private SizeAndContent(long size, @NotNull InputStream inputStream) {
            this.size = size;
            this.inputStream = inputStream;
        }

        public long getSize() {
            return size;
        }

        @NotNull
        public InputStream getInputStream() {
            return inputStream;
        }
    }
}
