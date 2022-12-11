package cz.los.alice.cpm;

import cz.los.alice.model.EnrichedTask;
import cz.los.alice.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cz.los.alice.TestUtils.createSimpleTasksSet;
import static cz.los.alice.TestUtils.createSingleTasksSet;
import static cz.los.alice.TestUtils.createTasksSetForComplexScenario;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CpmProcessorTest {

    private CpmProcessor processor;
    // This is a horrible approach to use a dependency in test instead of mocking that.
    // But I already spent too much time on creating preconditions so will consider it a justified trade off.
    // I do not do that in real life:)
    // I never use comments in code.
    // I prefer self-describing implementation and javadocs
    private CpmProcessorFactory factory;

    @BeforeEach
    public void setup() {
        this.factory = new CpmProcessorFactory();
    }

    @Test
    @DisplayName("Should create a CpmGraph complex scenario")
    public void buildVpmGraphComplexTest() {
        processor = factory.createCpmProcessor(createTasksSetForComplexScenario());
        CpmGraph result = processor.buildCpmGraph();

        assertGraphHasStartAndEndNodes(result);
        assertAllNodesResolved(result);
        assertEquals(12, result.getNodesByTask().size());
    }

    @Test
    @DisplayName("Should create a CpmGraph single Task scenario")
    public void buildVpmGraphSingleTaskTest() {
        processor = factory.createCpmProcessor(createSingleTasksSet());
        CpmGraph result = processor.buildCpmGraph();

        assertGraphHasStartAndEndNodes(result);
        assertAllNodesResolved(result);
        assertEquals(3, result.getNodesByTask().size());
    }

    @Test
    @DisplayName("Should create critical path based on provided CpmGraph easy scenario")
    public void createCriticalPathEasyTest() {
        processor = factory.createCpmProcessor(createSimpleTasksSet());
        CpmGraph graph = processor.buildCpmGraph();

        assertGraphHasStartAndEndNodes(graph);
        assertAllNodesResolved(graph);
        assertEquals(5, graph.getNodesByTask().size());

        List<String> expectedCriticalPath = List.of("first", "intermediate", "last");
        List<String> result = processor.buildCriticalPath(graph);
        assertEquals(expectedCriticalPath.size(), result.size());
        assertEquals(expectedCriticalPath, result);
    }

    @Test
    @DisplayName("Should create critical path based on provided CpmGraph complex scenario")
    public void createCriticalPathComplexTest() {
        processor = factory.createCpmProcessor(createTasksSetForComplexScenario());
        CpmGraph graph = processor.buildCpmGraph();

        assertGraphHasStartAndEndNodes(graph);
        assertAllNodesResolved(graph);
        assertEquals(12, graph.getNodesByTask().size());

        List<String> expectedCriticalPath = List.of("firstRoot", "thirdIntermediateTask", "fourthIntermediateTask", "firstTerminal");
        List<String> result = processor.buildCriticalPath(graph);
        assertEquals(expectedCriticalPath.size(), result.size());
        assertEquals(expectedCriticalPath, result);
    }

    @Test
    @DisplayName("Should create workers on site stats on provided CpmGraph easy scenario")
    public void createWorkersOnSiteStatisticsEasyTest() {
        processor = factory.createCpmProcessor(createSimpleTasksSet());
        CpmGraph graph = processor.buildCpmGraph();

        assertGraphHasStartAndEndNodes(graph);
        assertAllNodesResolved(graph);
        assertEquals(5, graph.getNodesByTask().size());

        Map<Integer, Integer> expectedStats = Map.of(0, 1, 1, 1, 2, 1, 3, 0);
        Map<Integer, Integer> result = processor.createWorkersOnSiteStatistics(graph);
        assertEquals(expectedStats.size(), result.size());
        assertEquals(expectedStats, result);
    }

    @Test
    @DisplayName("Should create workers on site stats on provided CpmGraph complex scenario")
    public void createWorkersOnSiteStatisticsComplexTest() {
        processor = factory.createCpmProcessor(createTasksSetForComplexScenario());
        CpmGraph graph = processor.buildCpmGraph();

        assertGraphHasStartAndEndNodes(graph);
        assertAllNodesResolved(graph);
        assertEquals(12, graph.getNodesByTask().size());

        Map<Integer, Integer> expectedStats = Map.of(0, 2, 1, 3, 2, 4, 3, 4, 4, 0);
        Map<Integer, Integer> result = processor.createWorkersOnSiteStatistics(graph);
        assertEquals(expectedStats.size(), result.size());
        assertEquals(expectedStats, result);
    }

    @Test
    @DisplayName("Should create a set of task enriched with start and end intervals on provided CpmGraph easy scenario")
    public void createEnrichedTasksSimpleTest() {
        Set<Task> givenTasks = createSimpleTasksSet();
        processor = factory.createCpmProcessor(givenTasks);
        CpmGraph graph = processor.buildCpmGraph();

        assertGraphHasStartAndEndNodes(graph);
        assertAllNodesResolved(graph);
        assertEquals(5, graph.getNodesByTask().size());

        List<EnrichedTask> result = processor.createEnrichedTasks(graph);
        assertEquals(givenTasks.size(), result.size());
        assertEquals(givenTasks, result.stream().map(EnrichedTask::getTask).collect(Collectors.toSet()));
        assertTrue(result.stream().allMatch(task -> task.getStatInterval() != null));
        assertTrue(result.stream().allMatch(task -> task.getEndInterval() != null));
    }

    @Test
    @DisplayName("Should create a set of task enriched with start and end intervals on provided CpmGraph complex scenario")
    public void createEnrichedTasksComplexTest() {
        Set<Task> givenTasks = createTasksSetForComplexScenario();
        processor = factory.createCpmProcessor(givenTasks);
        CpmGraph graph = processor.buildCpmGraph();

        assertGraphHasStartAndEndNodes(graph);
        assertAllNodesResolved(graph);
        assertEquals(12, graph.getNodesByTask().size());

        List<EnrichedTask> result = processor.createEnrichedTasks(graph);
        assertEquals(givenTasks.size(), result.size());
        assertEquals(givenTasks, result.stream().map(EnrichedTask::getTask).collect(Collectors.toSet()));
        assertTrue(result.stream().allMatch(task -> task.getStatInterval() != null));
        assertTrue(result.stream().allMatch(task -> task.getEndInterval() != null));
    }

    private static void assertGraphHasStartAndEndNodes(CpmGraph result) {
        assertEquals("START", result.getStartNode().getTask().getTaskCode());
        assertEquals("END", result.getEndNode().getTask().getTaskCode());
    }

    private static void assertAllNodesResolved(CpmGraph result) {
        assertTrue(result.getNodesByTask().values().stream().allMatch(Node::isResolvedForward));
        assertTrue(result.getNodesByTask().values().stream().allMatch(Node::isResolvedBackward));
        assertTrue(result.getNodesByTask().values().stream().allMatch(node -> node.getSlack() != null));
    }
}
