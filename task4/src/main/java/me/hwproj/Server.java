package me.hwproj;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
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
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Server {
    private static final int BUFFER_SIZE = 1000000;
    private Thread serverAcceptNewClientsThread;
    private Thread serverReadFromClientsThread;
    private Lock selectorLock;
    private Selector selector;

    public static void main(String[] argc) throws IOException {
        if (argc.length == 0 || argc.length > 2) {
            throw new IllegalArgumentException("Daun vvedi argumenti normalno");
        }

        String pathToDir = argc[0];
        Server server = new Server(pathToDir);
        server.start();
    }

    private String pathToDir;

    public Server(String pathToDir) throws IOException {
        this.pathToDir = Paths.get(pathToDir).toAbsolutePath().toString();
        selector = Selector.open();
    }

    public void start() throws ServerError {
        if (serverAcceptNewClientsThread == null && serverReadFromClientsThread == null) {
            serverAcceptNewClientsThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
                        serverSocketChannel.socket().bind(new InetSocketAddress(4242));
                        serverSocketChannel.configureBlocking(false);
                        while (true) {
                            SocketChannel socketChannel = serverSocketChannel.accept();
                            if (socketChannel != null) {//do something with socketChannel...}}
                                // new client with socketChannel
                                socketChannel.configureBlocking(false);
                                selectorLock.lock();
                                SelectionKey key = socketChannel.register(selector, SelectionKey.OP_READ);
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
                        while (true) {
                            selectorLock.lock();
                            Selector currentSelector = selector;
                            selectorLock.unlock();
                            int readyChannelsCount = currentSelector.select(1000);
                            if (readyChannelsCount != 0) {
                                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
                                while (keyIterator.hasNext()) {
                                    SelectionKey key = keyIterator.next();
                                    if (key.isReadable()) {// a channel is ready for reading
                                        // TODO
                                        SocketChannel channel = (SocketChannel) key.channel(); // ??????????????????????????????????/
                                        byte[] buffer = new byte[BUFFER_SIZE];
                                        int bytesRead = channel.read(ByteBuffer.wrap(buffer));
                                    }
                                    keyIterator.remove();
                                }
                            }
                        }
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

    public void stop() {

    }


    private List<StringAndBoolean> list(String pathName) {
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

    private static class StringAndBoolean {
        private String fileName;
        private boolean isDirectory;

        private StringAndBoolean(String fileName, boolean isDirectory) {
            this.fileName = fileName;
            this.isDirectory = isDirectory;
        }
    }

    private SizeAndContent get(String pathName) {
        Path path = Paths.get(pathToDir, pathName);
        File file = path.toFile();

        if (!file.exists() || !file.isFile() || !file.canRead()) {
            return errorResult;
        }

        try {
            return new SizeAndContent(file.length(), new FileInputStream(file));
        } catch (FileNotFoundException e) {
            return errorResult;
        }
    }

    private SizeAndContent errorResult = new SizeAndContent(-1, null);

    private static class SizeAndContent {
        private long size;
        private InputStream inputStream;

        private SizeAndContent(long size, InputStream inputStream) {
            this.size = size;
            this.inputStream = inputStream;
        }
    }
}
