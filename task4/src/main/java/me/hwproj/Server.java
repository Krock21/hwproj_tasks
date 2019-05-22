package me.hwproj;

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

public class Server {
    private static final int BUFFER_SIZE = 1000000;
    private Thread serverAcceptNewClientsThread;
    private Thread serverReadFromClientsThread;
    private final Lock selectorLock;
    private Selector selector;

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

    private String pathToDir;

    public Server(String pathToDir) throws IOException {
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

    private void receiveQuery(String query, Socket socket) throws IOException {
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
                long size = sizeAndContent.size;
                InputStream inputStream = sizeAndContent.inputStream;

                if (size == -1 || inputStream == null) {
                    outputStream.writeLong(-1);
                    return;
                }

                outputStream.writeLong(size);

                byte[] buffer = new byte[1024];
                while (true) {
                    int length = inputStream.read(buffer);
                    if (length == -1 || length == 0) {
                        break;
                    }

                    outputStream.write(buffer, 0, length);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void stop() {
        serverAcceptNewClientsThread.interrupt();
        serverReadFromClientsThread.interrupt();
        serverAcceptNewClientsThread = null;
        serverReadFromClientsThread = null;
    }

    public void join() throws Exception {
        if (serverAcceptNewClientsThread == null || serverReadFromClientsThread == null) {
            throw new Exception("Server is not started");
        }
        serverAcceptNewClientsThread.join();
        serverReadFromClientsThread.join();
    }

    public List<StringAndBoolean> list(String pathName) {
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

    public SizeAndContent get(String pathName) {
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
