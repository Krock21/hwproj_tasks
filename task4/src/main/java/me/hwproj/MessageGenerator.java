package me.hwproj;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/** Helping functions to generate messages to send by client or server */
public class MessageGenerator {
    /**
     * Generates message in following format:
     * [long: size][int: intCode][String: string]
     *      size -- number of bytes in message's body
     */
    @NotNull
    public static ByteBuffer[] generateMessage(int intCode, @Nullable String string) throws IOException {
        try (var byteArrayOutputStream = new ByteArrayOutputStream();
             var dataOutputStream = new DataOutputStream(byteArrayOutputStream)) {
            dataOutputStream.writeInt(intCode);
            if (string != null) {
                dataOutputStream.writeUTF(string);
            }
            dataOutputStream.flush();
            byteArrayOutputStream.flush();

            return addHead(byteArrayOutputStream);
        }
    }

    /** Adds head ([long: size] -- number of bytes in body) for message's stored in {@code bodyOutputStream} */
    @NotNull
    public static ByteBuffer[] addHead(@NotNull ByteArrayOutputStream bodyOutputStream) throws IOException {
        var bodyByteArray = bodyOutputStream.toByteArray();
        long requestLength = bodyByteArray.length;

        try (var headByteArrayOutputStream = new ByteArrayOutputStream();
             var headDataOutputStream = new DataOutputStream(headByteArrayOutputStream)) {
            headDataOutputStream.writeLong(requestLength);
            headDataOutputStream.flush();
            headByteArrayOutputStream.flush();

            var headByteArray = headByteArrayOutputStream.toByteArray();

            var bodyBuffer = ByteBuffer.wrap(bodyByteArray);
            var headBuffer = ByteBuffer.wrap(headByteArray);

            return new ByteBuffer[] {headBuffer, bodyBuffer};
        }
    }
}
