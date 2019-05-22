package me.hwproj;

import java.io.File;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;

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
        Path path = FileSystems.getDefault().getPath(pathName);
        if (!path.startsWith(pathToDir)) {
            return errorResult;
        }

        file.toPath().startsWith()
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
