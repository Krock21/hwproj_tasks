package me.hwproj;

public class File {
    private String path;
    private boolean isDirectory;

    public File(String file, boolean isDirectory) {
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
