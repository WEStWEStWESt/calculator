import com.calculator.files.LinearFileReader;
import com.calculator.files.exceptions.EmptyPageException;
import com.calculator.files.exceptions.FileNotFoundException;
import com.calculator.files.exceptions.FileUnreadableException;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.calculator.files.LinearFileReader.DEFAULT_DEPTH;
import static org.junit.Assert.*;

public class LinearFileReaderTest {
    public static final String VALID_FILEPATH = "src/test/resources/testFileForReadingInParts.txt";
    public static final String UNREADABLE_FILEPATH = "src/test/resources/testUnreadableFile.txt";
    public static final String NONE_EXISTED_FILEPATH = "src/test/resources/oops.txt";
    public static final String EMPTY_DIRECTORY_FILEPATH = "src/test/resources/root/empty";
    public static final String EMPTY_FILE_FILEPATH = "src/test/resources/root/empty.txt";
    public static final String ONE_FILE_FILEPATH = "src/test/resources/root/0.txt";
    public static final String ONE_FILE_IN_FOLDER_FILEPATH = "src/test/resources/root/level1/level2/level3/level4/level5/5.txt";
    public static final String ALL_FILES_FILEPATH = "src/test/resources/root";

    /* public static final String EMPTY_DIRECTORY_FILEPATH = "/src";
     private String contextPath;

     @Before
     public void setUp() throws Exception {
         String userPath = System.getProperty("user.dir");
         this.contextPath = userPath.substring(0, userPath.lastIndexOf("\\"));
     }
     @Test(expected = EmptyPageException.class)
     public void whenCreateNewLinearFileReader_shouldThrowEmptyPageException() throws IOException, EmptyPageException {
         new LinearFileReader(contextPath + EMPTY_DIRECTORY_FILEPATH);
     }*/
    //TODO check getPage() functionality which throws an IOException

    @Test
    public void whenReadAllFiles_givenNegativeDepth_shouldReturnFilesWithDefaultDepth() throws IOException, EmptyPageException {
        LinearFileReader reader = LinearFileReader.getBuilder().withPath(ALL_FILES_FILEPATH).withDepth(-10).build();
        assertAllLines(reader);
        assertEquals(DEFAULT_DEPTH, getNumericField("depth", reader));
    }

    @Test
    public void whenReadAllFiles_shouldReturnAllLinesOfTwoDepthFolders() throws IOException, EmptyPageException {
        assertAllLines(ALL_FILES_FILEPATH, 2);
    }

    @Test
    public void whenReadAllFilesStartingWithLevelOneFolder_shouldReturnAllTheFilesLines() throws IOException, EmptyPageException {
        assertAllLines(ALL_FILES_FILEPATH, 4);
    }

    @Test
    public void whenReadOneFolderWithOneFile_shouldReturnAllTheFileLines() throws IOException, EmptyPageException {
        assertAllLines(ONE_FILE_IN_FOLDER_FILEPATH, 1);
    }

    @Test
    public void whenReadOneFile_shouldReturnAllTheFileLines() throws IOException, EmptyPageException {
        assertAllLines(ONE_FILE_FILEPATH, 1);
    }

    @Test
    public void whenReadEmptyDirectory_hasNextReturnsFalse() throws IOException, EmptyPageException {
        assertFalse(new LinearFileReader(EMPTY_FILE_FILEPATH).hasNext());
    }

    @Test(expected = EmptyPageException.class)
    public void whenCreateNewLinearFileReader_shouldThrowEmptyPageException() throws IOException, EmptyPageException {
        new LinearFileReader(EMPTY_DIRECTORY_FILEPATH);
    }

    @Test
    public void whenCreateNewLinearFileReader_shouldCreateNewInstance() throws IOException, EmptyPageException {
        new LinearFileReader(VALID_FILEPATH);
    }

    @Test(expected = FileNotFoundException.class)
    public void whenCreateNewLinearFileReader_shouldThrowNullArgumentException() throws IOException, EmptyPageException {
        new LinearFileReader(null);
    }

    @Test(expected = FileNotFoundException.class)
    public void whenCreateNewLinearFileReader_shouldThrowFileNotFoundException() throws IOException, EmptyPageException {
        new LinearFileReader(NONE_EXISTED_FILEPATH);
    }

    @Ignore
    @Test(expected = FileUnreadableException.class)
    public void whenCreateNewLinearFileReader_shouldThrowFileUnreadableException() throws IOException, EmptyPageException {
        new LinearFileReader(UNREADABLE_FILEPATH);
    }

    private int getNumericField(String fieldName, LinearFileReader reader) {
        Field depthField = ReflectionUtils.findField(LinearFileReader.class, "depth");
        ReflectionUtils.makeAccessible(depthField);
        return (int) ReflectionUtils.getField(depthField, reader);
    }

    private void assertAllLines(String path, int depth) throws IOException, EmptyPageException {
        LinearFileReader reader = LinearFileReader.getBuilder()
                .withDepth(depth)
                .withPath(path)
                .build();
        assertAllLines(reader);
    }

   /* private void assertAllLines(String path, int depth, int linesPageSize, int filesPageSize) throws IOException, EmptyPageException {
        LinearFileReader reader = LinearFileReader.getBuilder()
                .withLinesPageSize(linesPageSize)
                .withFilesPageSize(filesPageSize)
                .withDepth(depth)
                .withPath(path)
                .build();
        assertAllLines(reader);
    }*/

    private void assertAllLines(LinearFileReader reader) {
        assertTrue(reader.hasNext());
        var lines = new HashSet<String>();
        /* while (reader.hasNext()) {lines.add(reader.next());} */
        reader.forEachRemaining(lines::add);
        int depth = getNumericField("depth", reader);
        int expectedLines = depth * 4;
        assertEquals(expectedLines, lines.size());
        Map<Integer, Set<String>> groups = lines.stream()
                .collect(Collectors.groupingBy(this::getClassifier, Collectors.toSet()));
        assertEquals(depth, groups.size());
        groups.keySet().forEach(key -> assertSubsetLines(key, groups));
    }

    private void assertSubsetLines(int fileNumber, Map<Integer, Set<String>> groups) {
        Set<String> lines = groups.get(fileNumber);
        assertEquals(4, lines.size());
        assertTrue(lines.contains("file %s - line1".formatted(fileNumber)));
        assertTrue(lines.contains("file %s - line2".formatted(fileNumber)));
        assertTrue(lines.contains("file %s - line3".formatted(fileNumber)));
        assertTrue(lines.contains("file %s - line4".formatted(fileNumber)));
    }

    private Integer getClassifier(String line) {
        return Integer.parseInt(line.substring("file ".length(), "file ".length() + 1));
    }
    // Improved tests infrastructure. Test of all files for LinearFileReader added.
}