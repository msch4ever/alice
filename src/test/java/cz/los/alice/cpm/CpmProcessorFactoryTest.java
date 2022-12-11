package cz.los.alice.cpm;

import cz.los.alice.model.Crew;
import cz.los.alice.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static cz.los.alice.TestUtils.createSimpleTasksSet;
import static cz.los.alice.TestUtils.createTasksSetForComplexScenario;
import static cz.los.alice.TestUtils.createTasksSetWithMultipleRootsAndTerminalTasks;
import static cz.los.alice.cpm.CpmProcessorFactory.END;
import static cz.los.alice.cpm.CpmProcessorFactory.START;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CpmProcessorFactoryTest {

    public static final Crew SIMPLE_CREW = Crew.builder().assignment(1).build();
    private CpmProcessorFactory factory;

    @BeforeEach
    public void setup() {
        this.factory = new CpmProcessorFactory();
    }

    @ParameterizedTest(name = "Should throw IllegalArgumentException if tasks is null or empty")
    @NullAndEmptySource
    public void nullOrEmptyTasksTest(Set<Task> tasks) {
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> factory.createCpmProcessor(tasks));
        assertEquals("Provided tasks Set should not be null and have at least one task", thrown.getMessage());
    }

    @Test
    @DisplayName("Should create CpmProcessor happy test")
    public void createCpmProcessorHappyTest() {
        Set<Task> tasks = createSimpleTasksSet();

        CpmProcessor result = factory.createCpmProcessor(tasks);

        assertCollectionSizes(tasks, result);
        assertHasStartAndEndTasks(result);
        assertStartAndEndTasksDependencies(result);

        assertEquals(1, result.getRootTasks().size());
        assertEquals(1, result.getTerminalTasks().size());

        Map<String, List<Task>> predecessorsByTask = result.getPredecessorsByTask();

        assertEquals(1, predecessorsByTask.get("first").size());
        assertEquals(1, predecessorsByTask.get("intermediate").size());
        assertEquals(1, predecessorsByTask.get("last").size());

        Map<String, List<Task>> successorsByTask = result.getSuccessorsByTask();

        assertEquals(1, successorsByTask.get(START).size());
        assertEquals(1, successorsByTask.get("first").size());
        assertEquals(1, successorsByTask.get("intermediate").size());
        assertEquals(1, successorsByTask.get("last").size());
    }

    @Test
    @DisplayName("Should create CpmProcessor with multiple root and terminal tasks")
    public void createCpmProcessorMultipleRootAndTerminalTasks() {
        Set<Task> tasks = createTasksSetWithMultipleRootsAndTerminalTasks();

        CpmProcessor result = factory.createCpmProcessor(tasks);

        assertCollectionSizes(tasks, result);
        assertHasStartAndEndTasks(result);
        assertStartAndEndTasksDependencies(result);

        assertEquals(3, result.getRootTasks().size());
        assertEquals(4, result.getTerminalTasks().size());

        Map<String, List<Task>> predecessorsByTask = result.getPredecessorsByTask();

        assertEquals(1, predecessorsByTask.get("firstRoot").size());
        assertEquals(1, predecessorsByTask.get("secondRoot").size());
        assertEquals(1, predecessorsByTask.get("thirdRoot").size());
        assertEquals(3, predecessorsByTask.get("intermediate").size());
        assertEquals(1, predecessorsByTask.get("firstTerminal").size());
        assertEquals(1, predecessorsByTask.get("secondTerminal").size());
        assertEquals(1, predecessorsByTask.get("thirdTerminal").size());
        assertEquals(1, predecessorsByTask.get("fourthTerminal").size());
        assertEquals(4, predecessorsByTask.get(END).size());

        Map<String, List<Task>> successorsByTask = result.getSuccessorsByTask();

        assertEquals(3, successorsByTask.get(START).size());
        assertEquals(1, successorsByTask.get("firstRoot").size());
        assertEquals(1, successorsByTask.get("secondRoot").size());
        assertEquals(1, successorsByTask.get("thirdRoot").size());
        assertEquals(4, successorsByTask.get("intermediate").size());
        assertEquals(1, successorsByTask.get("firstTerminal").size());
        assertEquals(1, successorsByTask.get("secondTerminal").size());
        assertEquals(1, successorsByTask.get("thirdTerminal").size());
        assertEquals(1, successorsByTask.get("fourthTerminal").size());
    }

    @Test
    @DisplayName("Should create CpmProcessor for a complex scenario")
    public void createCpmProcessorComplexScenario() {
        Set<Task> tasks = createTasksSetForComplexScenario();

        CpmProcessor result = factory.createCpmProcessor(tasks);

        assertCollectionSizes(tasks, result);
        assertHasStartAndEndTasks(result);
        assertStartAndEndTasksDependencies(result);

        assertEquals(2, result.getRootTasks().size());
        assertEquals(4, result.getTerminalTasks().size());

        Map<String, List<Task>> predecessorsByTask = result.getPredecessorsByTask();

        assertEquals(1, predecessorsByTask.get("firstRoot").size());
        assertEquals(1, predecessorsByTask.get("secondRoot").size());
        assertEquals(1, predecessorsByTask.get("firstIntermediateTask").size());
        assertEquals(1, predecessorsByTask.get("secondIntermediateTask").size());
        assertEquals(1, predecessorsByTask.get("thirdIntermediateTask").size());
        assertEquals(2, predecessorsByTask.get("fourthIntermediateTask").size());
        assertEquals(1, predecessorsByTask.get("firstTerminal").size());
        assertEquals(1, predecessorsByTask.get("secondTerminal").size());
        assertEquals(1, predecessorsByTask.get("thirdTerminal").size());
        assertEquals(3, predecessorsByTask.get("fourthTerminal").size());
        assertEquals(4, predecessorsByTask.get(END).size());

        Map<String, List<Task>> successorsByTask = result.getSuccessorsByTask();

        assertEquals(2, successorsByTask.get(START).size());
        assertEquals(2, successorsByTask.get("firstRoot").size());
        assertEquals(2, successorsByTask.get("secondRoot").size());
        assertEquals(3, successorsByTask.get("firstIntermediateTask").size());
        assertEquals(1, successorsByTask.get("secondIntermediateTask").size());
        assertEquals(2, successorsByTask.get("thirdIntermediateTask").size());
        assertEquals(1, successorsByTask.get("fourthIntermediateTask").size());
        assertEquals(1, successorsByTask.get("firstTerminal").size());
        assertEquals(1, successorsByTask.get("secondTerminal").size());
        assertEquals(1, successorsByTask.get("thirdTerminal").size());
        assertEquals(1, successorsByTask.get("fourthTerminal").size());
    }

    private static void assertCollectionSizes(Set<Task> tasks, CpmProcessor result) {
        assertNotNull(result);
        assertEquals(tasks.size() + 2, result.getAllTasks().size());
        assertEquals(tasks.size() + 2, result.getPredecessorsByTask().size());
        assertEquals(tasks.size() + 2, result.getSuccessorsByTask().size());
    }

    private static void assertHasStartAndEndTasks(CpmProcessor result) {
        Map<String, List<Task>> predecessorsByTask = result.getPredecessorsByTask();
        Map<String, List<Task>> successorsByTask = result.getSuccessorsByTask();

        assertTrue(predecessorsByTask.containsKey(START));
        assertTrue(predecessorsByTask.containsKey(END));

        assertTrue(successorsByTask.containsKey(START));
        assertTrue(successorsByTask.containsKey(END));
    }

    private void assertStartAndEndTasksDependencies(CpmProcessor result) {
        Map<String, List<Task>> predecessorsByTask = result.getPredecessorsByTask();
        Map<String, List<Task>> successorsByTask = result.getSuccessorsByTask();

        assertEquals(0, predecessorsByTask.get(START).size());
        assertEquals(0, successorsByTask.get(END).size());
    }

}
