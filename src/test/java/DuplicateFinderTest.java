import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.nio.file.Path;
import java.util.List;

import org.junit.Test;

public class DuplicateFinderTest {

    private DuplicateFinder duplicateFinder = new  DuplicateFinder();

    @Test
    public void testDuplicates() {
        List<FilePaths> result = duplicateFinder.findDuplicateFiles(Path.of("src/test/resources"));
        assertThat(result.size(), is(3));

        assertThat(result.get(0).getOriginalPath().toString(), is("src/test/resources/original-2.txt"));
        assertThat(result.get(0).getDuplicatePath().toString(), is("src/test/resources/otherPath/copy-2.txt"));

        assertThat(result.get(1).getOriginalPath().toString(), is("src/test/resources/empty.txt"));
        assertThat(result.get(1).getDuplicatePath().toString(), is("src/test/resources/anotherEmpty.txt"));

        assertThat(result.get(2).getOriginalPath().toString(), is("src/test/resources/original-1.txt"));
        assertThat(result.get(2).getDuplicatePath().toString(), is("src/test/resources/copy-1.txt"));
    }

}
