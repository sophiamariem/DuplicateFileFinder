import java.nio.file.Path;
import java.util.List;

public class FinderMain {

    public static void main(String[] args) {
        String path = args[0];
        DuplicateFinder duplicateFinder = new DuplicateFinder();
        List<FilePaths> paths = duplicateFinder.findDuplicateFiles(Path.of(path));
        for (FilePaths result : paths) {
            System.out.println(result.toString());
        }
        if (paths.size() == 0) {
            System.out.println("No duplicates found.");
        }
    }
}
