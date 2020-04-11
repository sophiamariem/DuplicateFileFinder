import java.nio.file.Path;

public class FilePaths {
    private Path duplicatePath;
    private Path originalPath;

    public FilePaths(Path duplicatePath, Path originalPath) {
        this.duplicatePath = duplicatePath;
        this.originalPath = originalPath;
    }

    public Path getDuplicatePath() {
        return duplicatePath;
    }

    public Path getOriginalPath() {
        return originalPath;
    }

    @Override
    public String toString() {
        return String.format("(Duplicate @: '%s', Original @: '%s')", duplicatePath, originalPath);
    }
}
