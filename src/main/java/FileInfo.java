import java.nio.file.Path;

public class FileInfo {
    private long modifiedTime;
    private Path path;

    public FileInfo(long modifiedTime, Path path) {
        this.modifiedTime = modifiedTime;
        this.path = path;
    }

    public long getModifiedTime() {
        return modifiedTime;
    }

    public Path getPath() {
        return path;
    }
}
