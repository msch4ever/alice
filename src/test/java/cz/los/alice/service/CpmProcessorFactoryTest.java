package cz.los.alice.service;

import cz.los.alice.cpm.CpmProcessor;
import cz.los.alice.model.Crew;
import cz.los.alice.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cz.los.alice.service.CpmProcessorFactory.END;
import static cz.los.alice.service.CpmProcessorFactory.START;
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

    private Set<Task> createSimpleTasksSet() {
        Task firstTask = Task.builder()
                .taskCode("first")
                .dependencies(Collections.emptyList())
                .duration(1)
                .crew(SIMPLE_CREW)
                .build();
        Task intermediateTask = Task.builder()
                .taskCode("intermediate")
                .dependencies(List.of("first"))
                .duration(1)
                .crew(SIMPLE_CREW)
                .build();
        Task lastTask = Task.builder()
                .taskCode("last")
                .dependencies(List.of("intermediate"))
                .duration(1)
                .crew(SIMPLE_CREW)
                .build();

        return Set.of(firstTask, intermediateTask, lastTask);
    }

    private Set<Task> createTasksSetWithMultipleRootsAndTerminalTasks() {
        Task firstRoot = Task.builder()
                .taskCode("firstRoot")
                .dependencies(Collections.emptyList())
                .duration(1)
                .crew(SIMPLE_CREW)
                .build();
        Task secondRoot = Task.builder()
                .taskCode("secondRoot")
                .dependencies(Collections.emptyList())
                .duration(1)
                .crew(SIMPLE_CREW)
                .build();
        Task thirdRoot = Task.builder()
                .taskCode("thirdRoot")
                .dependencies(Collections.emptyList())
                .duration(1)
                .crew(SIMPLE_CREW)
                .build();
        Task intermediateTask = Task.builder()
                .taskCode("intermediate")
                .dependencies(List.of("firstRoot", "secondRoot", "thirdRoot"))
                .duration(1)
                .crew(SIMPLE_CREW)
                .build();
        Task firstTerminal = Task.builder()
                .taskCode("firstTerminal")
                .dependencies(List.of("intermediate"))
                .duration(1)
                .crew(SIMPLE_CREW)
                .build();
        Task secondTerminal = Task.builder()
                .taskCode("secondTerminal")
                .dependencies(List.of("intermediate"))
                .duration(1)
                .crew(SIMPLE_CREW)
                .build();
        Task thirdTerminal = Task.builder()
                .taskCode("thirdTerminal")
                .dependencies(List.of("intermediate"))
                .duration(1)
                .crew(SIMPLE_CREW)
                .build();
        Task fourthTerminal = Task.builder()
                .taskCode("fourthTerminal")
                .dependencies(List.of("intermediate"))
                .duration(1)
                .crew(SIMPLE_CREW)
                .build();


        return Set.of(
                firstRoot,
                secondRoot,
                thirdRoot,
                intermediateTask,
                firstTerminal,
                secondTerminal,
                thirdTerminal,
                fourthTerminal);
    }

    private Set<Task> createTasksSetForComplexScenario() {
        Task firstRoot = Task.builder()
                .taskCode("firstRoot")
                .dependencies(Collections.emptyList())
                .duration(1)
                .crew(SIMPLE_CREW)
                .build();
        Task secondRoot = Task.builder()
                .taskCode("secondRoot")
                .dependencies(Collections.emptyList())
                .duration(1)
                .crew(SIMPLE_CREW)
                .build();
        Task firstIntermediateTask = Task.builder()
                .taskCode("firstIntermediateTask")
                .dependencies(List.of("secondRoot"))
                .duration(1)
                .crew(SIMPLE_CREW)
                .build();
        Task secondIntermediateTask = Task.builder()
                .taskCode("secondIntermediateTask")
                .dependencies(List.of("firstIntermediateTask"))
                .duration(1)
                .crew(SIMPLE_CREW)
                .build();
        Task thirdIntermediateTask = Task.builder()
                .taskCode("thirdIntermediateTask")
                .dependencies(List.of("firstRoot"))
                .duration(1)
                .crew(SIMPLE_CREW)
                .build();
        Task fourthIntermediateTask = Task.builder()
                .taskCode("fourthIntermediateTask")
                .dependencies(List.of("firstIntermediateTask", "thirdIntermediateTask"))
                .duration(1)
                .crew(SIMPLE_CREW)
                .build();
        Task firstTerminal = Task.builder()
                .taskCode("firstTerminal")
                .dependencies(List.of("fourthIntermediateTask"))
                .duration(1)
                .crew(SIMPLE_CREW)
                .build();
        Task secondTerminal = Task.builder()
                .taskCode("secondTerminal")
                .dependencies(List.of("secondRoot"))
                .duration(1)
                .crew(SIMPLE_CREW)
                .build();
        Task thirdTerminal = Task.builder()
                .taskCode("thirdTerminal")
                .dependencies(List.of("secondIntermediateTask"))
                .duration(1)
                .crew(SIMPLE_CREW)
                .build();
        Task fourthTerminal = Task.builder()
                .taskCode("fourthTerminal")
                .dependencies(List.of("thirdIntermediateTask", "firstIntermediateTask", "firstRoot"))
                .duration(1)
                .crew(SIMPLE_CREW)
                .build();


        return Set.of(
                firstRoot,
                secondRoot,
                firstIntermediateTask,
                secondIntermediateTask,
                thirdIntermediateTask,
                fourthIntermediateTask,
                firstTerminal,
                secondTerminal,
                thirdTerminal,
                fourthTerminal);
    }

}
