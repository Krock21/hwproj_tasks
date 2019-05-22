package me.hwproj;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Server {

    public static void main(String[] argc) {

    }

    private String pathToDir;

    public Server(String pathToDir) {
        this.pathToDir = pathToDir;
    }

    public void start() {

    }

    public void stop() {

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
