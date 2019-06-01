package me.hwproj;

public class FileDescriprion {
    private String path;
    private boolean isDirectory;

    public FileDescriprion(String file, boolean isDirectory) {
        this.path = file;
        this.isDirectory = isDirectory;
    }

    public String getPath() {
        return path;
    }

    public boolean getIsDirectory() {
        return isDirectory;
    }
}
