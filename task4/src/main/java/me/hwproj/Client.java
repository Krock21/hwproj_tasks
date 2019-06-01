package me.hwproj;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class Client {

    private SocketChannel socketChannel;
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            throw new IllegalArgumentException("Should take two arguments: server ip and server port");
        }
        var client = new Client();
        var in = new Scanner(System.in);
        String serverIp = args[0];
        int serverPort = Integer.valueOf(args[1]);
        System.out.println("Server ip: " + serverIp + " port: " + serverPort);
        System.out.println("Start connecting");
        client.connect(serverIp, serverPort);
        System.out.println("Successfully connected!");
        while (true) {
            try {
                System.out.println("Write request code (1 for list, 2 for get, 3 for disconnect)");
                int requestCode = in.nextInt();
                if (requestCode == 3) {
                    client.disconnect();
                    break;
                }
                System.out.println("Write path");
                in.nextLine();
                String path = in.nextLine();
                if (requestCode == 1) {
                    System.out.println("Start executing list");
                    var result = client.executeList(path);
                    System.out.println("List executed! Here is the result:");
                    for (var file : result) {
                        System.out.println(file.getPath() + " " + file.getIsDirectory());
                    }
                } else if (requestCode == 2) {
                    /*System.out.println("Start executing get");
                    var result = client.executeGet(path);
                    System.out.println("Get executed. Here is the result:");
                    System.out.println(new String(result, StandardCharsets.UTF_8));*/
                    // TODO
                }
            } catch (IllegalArgumentException | InputMismatchException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public Client() {

    }

    public void connect(String serverIp, int serverPort) throws IOException {
        socketChannel = SocketChannel.open();
        socketChannel.socket().setTcpNoDelay(true);
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress(serverIp, serverPort));
        while (!socketChannel.finishConnect()) {
            // waiting
        }
    }

    public void disconnect() throws IOException {
        socketChannel.close();
    }

    public List<FileDescriprion> executeList(String path) throws IOException {
        sendRequest(writeRequest(1, path));
        try (var byteArrayOutputStream = new ByteArrayOutputStream()) {
            readResponse(byteArrayOutputStream);
            return generateAnswerForList(byteArrayOutputStream.toByteArray());
        }
    }

    private void readResponse(OutputStream outputStream) throws IOException {
        long bytesReadHead = 0;
        var buffer = ByteBuffer.allocate(8); // buffer to read head -- size of response
        while (bytesReadHead < 8) { // Haven't read head
            bytesReadHead += socketChannel.read(buffer);
        }
        try (var headInputStream = new DataInputStream(new ByteArrayInputStream(buffer.array()))) {
            long needToRead = headInputStream.readLong();
            buffer = ByteBuffer.allocate(BUFFER_SIZE);
            // Reading body
            while (needToRead > 0) {
                int bytesRead = socketChannel.read(buffer);
                needToRead -= bytesRead;
                outputStream.write(buffer.array(), 0, bytesRead);
                buffer.clear();
            }
            outputStream.flush();
        }
    }

    @NotNull
    private List<FileDescriprion> generateAnswerForList(@NotNull byte[] bytes) throws IOException {
        try (var dataInputStream = new DataInputStream(new ByteArrayInputStream(bytes))) {
            int size = dataInputStream.readInt();
            if (size == -1) {
                throw new IllegalArgumentException("List returned -1");
            }
            var result = new ArrayList<FileDescriprion>();
            for (int i = 0; i < size; ++i) {
                String fileName = dataInputStream.readUTF();
                boolean isDirectory = dataInputStream.readBoolean();
                result.add(new FileDescriprion(fileName, isDirectory));
            }
            return result;
        }
    }

    public void executeGet(String path, String pathToStore) throws IOException {
        sendRequest(writeRequest(2, path));
        //generateAnswerForGet(readAnswerFromServer());
        // TODO
    }

    private byte[] generateAnswerForGet(byte[] bytes) throws IOException {
        // TODO
        return null;
    }

    private void sendRequest(ByteBuffer[] buffers) throws IOException {
        while (buffers[0].hasRemaining() || buffers[1].hasRemaining()) {
            socketChannel.write(buffers);
        }
    }

    @NotNull
    private ByteBuffer[] writeRequest(int requestCode, @NotNull String request) throws IOException {
        return MessageGenerator.generateMessage(requestCode, request);
    }
}