package cz.los.alice.service;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import cz.los.alice.model.Task;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TaskParserTest {

    public static final String INPUT_SIMPLE_TEST_JSON = "input/SimpleTest.json";
    public static final String STUBS_NEEDED = "input/StubsNeeded.json";
    public static final String SINGLE_TASK = "input/SingleTask.json";
    public static final String EMPTY = "input/Empty.json";
    public static final String WIERD = "input/Wierd.json";
    public static final String SINGLE_TASK_WITH_DEPENDENCY = "input/SingleTaskWithDependency.json";
    public static final String NO_ROOTS = "input/NoRoots.json";

    @Test
    @DisplayName("Should parse JSON file happy test")
    public void parseInputFileHappyTest() {
        TaskParser taskParser = new TaskParser(new ClassPathResource(INPUT_SIMPLE_TEST_JSON));
        Set<Task> result = taskParser.parseInputFile();

        assertNotNull(result);
        assertEquals(4, result.size());
    }

    @Test
    @DisplayName("Should populate Task with stub values if crew or duration not provided in file")
    public void parseAndPopulateWithStubs() {
        TaskParser taskParser = new TaskParser(new ClassPathResource(STUBS_NEEDED));
        Set<Task> result = taskParser.parseInputFile();

        assertNotNull(result);
        assertEquals(4, result.size());
        Task noCrew = result.stream()
                .filter(it -> "NO_CREW".equals(it.getTaskCode()))
                .findFirst()
                .orElseThrow(AssertionError::new);
        Task noDuration = result.stream()
                .filter(it -> "NO_DURATION".equals(it.getTaskCode()))
                .findFirst()
                .orElseThrow(AssertionError::new);
        Task noCrewAndDuration = result.stream()
                .filter(it -> "NO_CREW_AND_DURATION".equals(it.getTaskCode()))
                .findFirst()
                .orElseThrow(AssertionError::new);
        assertNotNull(noCrew.getCrew());
        assertNotNull(noDuration.getDuration());
        assertNotNull(noCrewAndDuration.getCrew());
        assertNotNull(noCrewAndDuration.getDuration());
    }

    @Test
    @DisplayName("Should parse JSON file with the single entry")
    public void parseInputFileWithSingleEntry() {
        TaskParser taskParser = new TaskParser(new ClassPathResource(SINGLE_TASK));
        Set<Task> result = taskParser.parseInputFile();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should throw exception if provided file is empty")
    public void failIfFileIsEmpty() {
        TaskParser taskParser = new TaskParser(new ClassPathResource(EMPTY));
        assertThrows(Exception.class, taskParser::parseInputFile);
    }

    @Test
    @DisplayName("Should throw exception if provided file has undefined data inside")
    public void failIfFileHasWierdData() {
        TaskParser taskParser = new TaskParser(new ClassPathResource(WIERD));
        assertThrows(UnrecognizedPropertyException.class, taskParser::parseInputFile);
    }

    @Test
    @DisplayName("Should throw exception if provided file has single task with dependency")
    public void failIfFileHasOneTaskOnlyAndWithDependency() {
        TaskParser taskParser = new TaskParser(new ClassPathResource(SINGLE_TASK_WITH_DEPENDENCY));
        assertThrows(IllegalArgumentException.class, taskParser::parseInputFile);
    }

    @Test
    @DisplayName("Should throw exception if provided file has no root task")
    public void failIfFileHasNoRootTask() {
        TaskParser taskParser = new TaskParser(new ClassPathResource(NO_ROOTS));
        assertThrows(IllegalArgumentException.class, taskParser::parseInputFile);
    }
}
