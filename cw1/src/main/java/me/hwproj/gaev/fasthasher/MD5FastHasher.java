package me.hwproj.gaev.fasthasher;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

class MD5FastForkJoinHasher extends RecursiveTask<byte[]> {
    private File file;

    public MD5FastForkJoinHasher(File file) {
        this.file = file;
    }

    public byte[] compute() {
        try {
            MessageDigest md5Hasher = MessageDigest.getInstance("MD5");
            if (file.isFile()) {
                byte[] buffer = new byte[MD5FastHasher.BUFFER_SIZE];
                try (InputStream inputStream = Files.newInputStream(Paths.get(file.getAbsolutePath()))) {
                    while (inputStream.read(buffer) > 0) {
                        md5Hasher.update(buffer);
                    }
                }
            } else {
                md5Hasher.update(file.getName().getBytes());
                Path rootPath = Paths.get(file.getAbsolutePath());
                List<MD5FastForkJoinHasher> tasks = new ArrayList<>();
                Files.walk(rootPath).forEach(
                        path -> {
                            if (!path.equals(rootPath)) {
                                var task = new MD5FastForkJoinHasher(path.toFile());
                                task.fork();
                                tasks.add(task);
                            }
                        }
                );
                for (var task : tasks) {
                    md5Hasher.update(task.join());
                }
            }
            return md5Hasher.digest();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("NoSuchAlgorithmsException");
            System.exit(-1);
        } catch (IOException e) {
            System.err.println("IOException");
            System.exit(-1);
        }
        return null;
    }
}

/**
 * provides function to get hash of File or Directory in maximum available threads
 */
public class MD5FastHasher {
    /**
     * Buffer size, which is used to read big files
     */
    public static final int BUFFER_SIZE = 1000000;

    /**
     * compute MD5 hash of File or Directory
     *
     * @param file File object which points to File or Directory
     * @return md5 hash of File or Directory
     * @throws IOException if there is problem with IO
     */

    public static byte[] getHash(File file) throws NoSuchAlgorithmException, IOException, ExecutionException, InterruptedException {
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        MD5FastForkJoinHasher hasher = new MD5FastForkJoinHasher(file);
        forkJoinPool.execute(hasher);
        return hasher.get();
    }
}
