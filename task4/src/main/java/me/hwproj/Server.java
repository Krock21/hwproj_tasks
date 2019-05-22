package me.hwproj;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Server {

    public static void main(String[] argc) {
        if (argc.length == 0 || argc.length > 2) {
            throw new IllegalArgumentException("Daun vvedi argumenti normalno");
        }

        String pathToDir = argc[0];
        Server server = new Server(pathToDir);
        server.start();
    }

    private String pathToDir;

    public Server(String pathToDir) {
        this.pathToDir = Paths.get(pathToDir).toAbsolutePath().toString();
    }

    public void start() {

    }

    public void stop() {

    }


    private List<StringAndBoolean> list(String pathName) {
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
