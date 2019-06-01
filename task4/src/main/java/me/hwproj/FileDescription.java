package me.hwproj;

/** Stores information about file: path to it and if it is a directory */
public class FileDescription {
    private String path;
    private boolean isDirectory;

    public FileDescription(String file, boolean isDirectory) {
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
