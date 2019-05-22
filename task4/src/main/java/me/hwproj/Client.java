package me.hwproj;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Client {

    private SocketChannel socketChannel;

    public static void main(String[] args) throws IOException {
        var client = new Client();
        var in = new Scanner(System.in);
        System.out.println("Write ip address");
        String serverIp = in.nextLine();
        System.out.println("Write port");
        int serverPort = in.nextInt();
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
                    System.out.println("Start executing get");
                    var result = client.executeGet(path);
                    System.out.println("Get executed. Here is the result:");
                    System.out.println(new String(result, StandardCharsets.UTF_8));
                }
            } catch (IllegalArgumentException e) {
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

    public List<File> executeList(String path) throws IOException {
        sendRequest(writeRequest(1, path));
        return generateAnswerForList(readAnswerFromServer());
    }

    private List<File> generateAnswerForList(byte[] bytes) {
        var string = new String(bytes, StandardCharsets.UTF_8);
        String[] splittedResponse = string.split(" ");
        int size = Integer.valueOf(splittedResponse[0]);
        if (size == -1) {
            throw new IllegalArgumentException("List returned -1");
        }
        assert splittedResponse.length == size * 2 + 1;
        var result = new ArrayList<File>();
        for (int i = 1; i < splittedResponse.length; i += 2) {
            result.add(new File(splittedResponse[i], splittedResponse[i + 1].equals("1")));
        }
        return result;
    }

    public byte[] executeGet(String path) throws IOException {
        sendRequest(writeRequest(2, path));
        return generateAnswerForGet(readAnswerFromServer());
    }

    private byte[] generateAnswerForGet(byte[] bytes) throws IOException {
        try (DataInputStream is = new DataInputStream(new ByteArrayInputStream(bytes))) {
            long size = is.readLong();
            if (size == -1) {
                throw new IllegalArgumentException("Get returned -1");
            }
            return is.readAllBytes();
        }
    }

    private void sendRequest(ByteBuffer buffer) throws IOException {
        while (buffer.hasRemaining()) {
            socketChannel.write(buffer);
        }
        buffer.clear();
    }

    private byte[] readAnswerFromServer() throws IOException {
        try (var stream = new ByteArrayOutputStream()) {
            var buffer = ByteBuffer.allocate(1024);
            int bytesRead;
            while ((bytesRead = socketChannel.read(buffer)) > 0) {
                buffer.flip();
                stream.write(getSubarray(buffer.array(), bytesRead));
                buffer.clear();
            }
            return stream.toByteArray();
        }
    }

    private byte[] getSubarray(byte[] buffer, int bytesRead) {
        var result = new byte[bytesRead];
        System.arraycopy(buffer, 0, result, 0, bytesRead);
        return result;
    }

    private ByteBuffer writeRequest(int requestCode, String request) {
        String data = requestCode + " " + request;
        ByteBuffer buffer = ByteBuffer.allocate(data.getBytes().length);
        buffer.clear();
        buffer.put(data.getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        return buffer;
    }
}
