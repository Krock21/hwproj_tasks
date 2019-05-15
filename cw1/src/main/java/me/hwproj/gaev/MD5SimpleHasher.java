
package me.hwproj.gaev;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * provides function to get hash of File or Directory in 1 thread
 */
public class MD5SimpleHasher {
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
    public static byte[] getHash(File file) throws NoSuchAlgorithmException, IOException {
        MessageDigest md5Hasher = MessageDigest.getInstance("MD5");
        if (file.isFile()) {
            byte[] buffer = new byte[BUFFER_SIZE];
            try (InputStream inputStream = Files.newInputStream(Paths.get(file.getAbsolutePath()))) {
                while (inputStream.read(buffer) > 0) {
                    md5Hasher.update(buffer);
                }
            }
        } else {
            md5Hasher.update(file.getName().getBytes());
            Path rootPath = Paths.get(file.getAbsolutePath());
            Files.walk(rootPath, 1).forEach(
                    path -> {
                        if (!path.equals(rootPath)) {
                            try {
                                md5Hasher.update(getHash(path.toFile()));
                            } catch (NoSuchAlgorithmException e) {
                                System.err.println("NoSuchAlgorithmsException");
                                System.exit(-1);
                            } catch (IOException e) {
                                System.err.println("IOException");
                                System.exit(-1);
                            }
                        }
                    }
            );
        }
        return md5Hasher.digest();
    }
}
