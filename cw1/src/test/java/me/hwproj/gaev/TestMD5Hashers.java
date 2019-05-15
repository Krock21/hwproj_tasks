package me.hwproj.gaev;

import me.hwproj.gaev.fasthasher.MD5FastHasher;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class TestMD5Hashers {

    @Test
    void testOnFile() throws IOException, NoSuchAlgorithmException, ExecutionException, InterruptedException {
        File file = File.createTempFile("TestMD5Hashers1", null);
        byte[] hash1 = MD5SimpleHasher.getHash(file);
        byte[] hash2 = MD5FastHasher.getHash(file);
        byte[] hash3 = MD5SimpleHasher.getHash(file);
        byte[] hash4 = MD5FastHasher.getHash(file);
        assertArrayEquals(hash1, hash2);
        assertArrayEquals(hash2, hash3);
        assertArrayEquals(hash3, hash4);
    }

    @Test
    void testOnDirectory() throws IOException, NoSuchAlgorithmException, ExecutionException, InterruptedException {
        Path path = Files.createTempDirectory("TestMD5Hashers2");
        File file1 = File.createTempFile("TestMD5Hashers21", null, path.toFile());
        File file2 = File.createTempFile("TestMD5Hashers22", null, path.toFile());
        new FileWriter(file1).write("Hello");
        new FileWriter(file2).write("Kitty");

        byte[] hash1 = MD5SimpleHasher.getHash(path.toFile());
        byte[] hash2 = MD5FastHasher.getHash(path.toFile());
        byte[] hash3 = MD5SimpleHasher.getHash(path.toFile());
        byte[] hash4 = MD5FastHasher.getHash(path.toFile());
        assertArrayEquals(hash1, hash2);
        assertArrayEquals(hash2, hash3);
        assertArrayEquals(hash3, hash4);

    }
}