package me.hwproj.gaev;

import me.hwproj.gaev.fasthasher.MD5FastHasher;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.Timer;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, ExecutionException, InterruptedException {
        String string = new Scanner(System.in).next();
        long start1 = System.currentTimeMillis();
        MD5SimpleHasher.getHash(Paths.get(string).toFile());
        long end1 = System.currentTimeMillis();

        long start2 = System.currentTimeMillis();
        MD5FastHasher.getHash(Paths.get(string).toFile());
        long end2 = System.currentTimeMillis();

        System.out.println((end1 - start1) + ", " + (end2 - start2));
    }
}
