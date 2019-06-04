package me.hwproj;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;

/** Communication between server and client */
public class ClientData {
    private static final int BUFFER_SIZE = 1024;
    private static final int LONG_SIZE = 8;

    private boolean readingHead = true;
    /** Bytes needed to read to change stage (start reading body or answer a query) */
    private long toReadSize = LONG_SIZE;
    @NotNull private ByteBuffer buffer = ByteBuffer.allocate(LONG_SIZE);
    /** Output stream where body of message is stored */
    private ByteArrayOutputStream outputStream = null;
    @NotNull private SocketChannel socketChannel;
    @NotNull private Server server;

    public ClientData(@NotNull SocketChannel channel, @NotNull Server server) {
        socketChannel = channel;
        this.server = server;
    }

    /**
     * Reads data from socket.
     * Reads only until the end of stage (i.e. if can read 100 bytes, but haven't read head,
     *      would read only 8 bytes)
     */
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

    /** Parses query id, executes it and sends response */
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

    /**
     * Executes "List" query
     * Response format: [long: messageSize][int: size]([String: filename][boolean: isDirectory])*
     *      messageSize -- number of bytes in message's body
     *      size -- number of files in directory
     * size = -1 if file is not presented
     */
    private void executeList(@NotNull String path) throws IOException {
        List<FileDescription> files = server.list(path);

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

    /**
     * Executes "Get" query
     * Response format: [long: size][bytes: file]
     *      size -- number of bytes in a file
     * size = -1 if file is not presented
     */
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

    /** Generates message with size = -1 for "List" query */
    @NotNull
    private ByteBuffer[] generateReject() throws IOException {
        return MessageGenerator.generateMessage(-1, null);
    }

    /** Sends response to client */
    private void sendResponse(@NotNull ByteBuffer[] buffers) throws IOException {
        while (hasRemaining(buffers)) {
            socketChannel.write(buffers);
        }
    }

    /** Checks if some of buffers has remaining bytes */
    private boolean hasRemaining(@NotNull ByteBuffer[] buffers) {
        for (var buffer : buffers) {
            if (buffer.hasRemaining()) {
                return true;
            }
        }
        return false;
    }
}
