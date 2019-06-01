package me.hwproj;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;

public class ClientData {
    private static final int BUFFER_SIZE = 1024;
    private static final int LONG_SIZE = 8;

    private boolean readingHead = true;
    private long toReadSize = LONG_SIZE;
    @NotNull private ByteBuffer buffer = ByteBuffer.allocate(LONG_SIZE);
    private ByteArrayOutputStream outputStream = null;
    @NotNull private SocketChannel socketChannel;
    @NotNull private Server server;

    public ClientData(@NotNull SocketChannel channel, @NotNull Server server) {
        socketChannel = channel;
        this.server = server;
    }

    public void read() throws IOException {
        if (readingHead) {
            toReadSize -= socketChannel.read(buffer);
            if (toReadSize == 0) { // read all head
                try (var dataInputStream = new DataInputStream(new ByteArrayInputStream(buffer.array()))) {
                    toReadSize = dataInputStream.readLong();
                    readingHead = false;
                    buffer = ByteBuffer.allocate(BUFFER_SIZE);
                    outputStream = new ByteArrayOutputStream();
                }
            }
        } else {
            if (toReadSize < BUFFER_SIZE) {
                buffer = ByteBuffer.allocate((int) toReadSize);
            }
            int bytesRead = socketChannel.read(buffer);
            if (bytesRead == 0) {
                return;
            }
            toReadSize -= bytesRead;
            outputStream.write(buffer.array(), 0, bytesRead);
            buffer.clear();
            if (toReadSize == 0) { // read all query
                outputStream.flush();
                byte[] query = outputStream.toByteArray();
                buffer = ByteBuffer.allocate(LONG_SIZE);
                readingHead = true;
                outputStream = null;
                toReadSize = LONG_SIZE;
                executeQuery(query);
            }
        }
    }

    private void executeQuery(@NotNull byte[] query) throws IOException {
        try (var dataInputStream = new DataInputStream(new ByteArrayInputStream(query))) {
            int queryId = dataInputStream.readInt();
            String path = dataInputStream.readUTF();
            System.out.println("Received query " + queryId + " " + path);
            if (queryId == 1) {
                executeList(path);
            } else if (queryId == 2) {
                executeGet(path);
            }
        }
    }

    private void executeList(@NotNull String path) throws IOException {
        List<FileDescriprion> files = server.list(path);
        if (files == null) {
            sendResponse(generateReject());
            return;
        }
        try (var byteArrayOutputStream = new ByteArrayOutputStream();
             var dataOutputStream = new DataOutputStream(byteArrayOutputStream)) {
            dataOutputStream.writeInt(files.size());
            for (var file : files) {
                dataOutputStream.writeUTF(file.getPath());
                dataOutputStream.writeBoolean(file.getIsDirectory());
            }
            dataOutputStream.flush();
            byteArrayOutputStream.flush();

            ByteBuffer[] response = MessageGenerator.addHead(byteArrayOutputStream);
            sendResponse(response);
        }
    }

    private void executeGet(@NotNull String path) throws IOException {
        Server.SizeAndContent fileToSend = server.get(path);
        long size = -1;
        if (fileToSend != null) {
            size = fileToSend.getSize();
        }
        try (var byteArrayOutputStream = new ByteArrayOutputStream();
             var dataOutputStream = new DataOutputStream(byteArrayOutputStream)) {
            dataOutputStream.writeLong(size);
            dataOutputStream.flush();
            byteArrayOutputStream.flush();

            sendResponse(new ByteBuffer[]{ByteBuffer.wrap(byteArrayOutputStream.toByteArray())});
        }
        if (fileToSend == null) {
            return;
        }
        var buffer = new byte[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = fileToSend.getInputStream().read(buffer)) > 0) {
            var byteBuffer = ByteBuffer.wrap(buffer, 0, bytesRead);
            sendResponse(new ByteBuffer[]{byteBuffer});
        }
    }

    @NotNull
    private ByteBuffer[] generateReject() throws IOException {
        return MessageGenerator.generateMessage(-1, null);
    }

    private void sendResponse(@NotNull ByteBuffer[] buffers) throws IOException {
        while (hasRemaining(buffers)) {
            socketChannel.write(buffers);
        }
    }

    private boolean hasRemaining(@NotNull ByteBuffer[] buffers) {
        for (var buffer : buffers) {
            if (buffer.hasRemaining()) {
                return true;
            }
        }
        return false;
    }
}
