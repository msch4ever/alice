package cz.los.alice.cpm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static cz.los.alice.TestUtils.createSimpleTasksSet;
import static cz.los.alice.TestUtils.createSingleTasksSet;
import static cz.los.alice.TestUtils.createTasksSetForComplexScenario;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CpmGraphTest {

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
    @DisplayName("Should calculate earliest start and end for each node in graph single task scenario")
    public void calculateCpmMetricsInForwardDirectionSingleTaskTest() {
        CpmProcessor processor = factory.createCpmProcessor(createSingleTasksSet());
        CpmGraph graph =
                new CpmGraph(processor.getAllTasks(), processor.getPredecessorsByTask(), processor.getSuccessorsByTask());

        graph.calculateCpmMetricsInForwardDirection();

        assertTrue(graph.getNodesByTask().values().stream().allMatch(Node::isResolvedForward));
    }

    @Test
    @DisplayName("Should throw an exception if starting node has all successors resolved")
    public void throwsExceptionIfStartingNodeHasAllSuccessorsResolved() {
        CpmProcessor processor = factory.createCpmProcessor(createTasksSetForComplexScenario());
        CpmGraph graph =
                new CpmGraph(processor.getAllTasks(), processor.getPredecessorsByTask(), processor.getSuccessorsByTask());
        graph.getStartNode().getSuccessors().forEach(node -> node.setResolvedForward(true));

        RuntimeException thrown = assertThrows(RuntimeException.class, graph::calculateCpmMetricsInForwardDirection);
        assertEquals("Starting node should have at least one unresolvedSuccessor", thrown.getMessage());
    }

    @Test
    @DisplayName("Should calculate earliest start and end for each node in graph simple scenario")
    public void calculateCpmMetricsInForwardDirectionSimpleTest() {
        CpmProcessor processor = factory.createCpmProcessor(createSimpleTasksSet());
        CpmGraph graph =
                new CpmGraph(processor.getAllTasks(), processor.getPredecessorsByTask(), processor.getSuccessorsByTask());

        graph.calculateCpmMetricsInForwardDirection();

        assertTrue(graph.getNodesByTask().values().stream().allMatch(Node::isResolvedForward));
    }

    @Test
    @DisplayName("Should calculate earliest start and end for each node in graph complex scenario")
    public void calculateCpmMetricsInForwardDirectionComplexTest() {
        CpmProcessor processor = factory.createCpmProcessor(createTasksSetForComplexScenario());
        CpmGraph graph =
                new CpmGraph(processor.getAllTasks(), processor.getPredecessorsByTask(), processor.getSuccessorsByTask());

        graph.calculateCpmMetricsInForwardDirection();

        assertTrue(graph.getNodesByTask().values().stream().allMatch(Node::isResolvedForward));
    }

    /////////

    @Test
    @DisplayName("Should throw an exception if graph is not resolved in forward direction yet")
    public void throwsExceptionIfGraphWasNotResolvedInForwardDirectionYet() {
        CpmProcessor processor = factory.createCpmProcessor(createTasksSetForComplexScenario());
        CpmGraph graph =
                new CpmGraph(processor.getAllTasks(), processor.getPredecessorsByTask(), processor.getSuccessorsByTask());

        RuntimeException thrown = assertThrows(RuntimeException.class, graph::calculateCpmMetricsInBackwardDirection);
        assertEquals("Unresolved Node should have unresolved successor", thrown.getMessage());
    }

    @Test
    @DisplayName("Should calculate latest start and end for each node in graph single task scenario")
    public void calculateCpmMetricsInBackwardDirectionSingleTaskTest() {
        CpmProcessor processor = factory.createCpmProcessor(createSingleTasksSet());
        CpmGraph graph =
                new CpmGraph(processor.getAllTasks(), processor.getPredecessorsByTask(), processor.getSuccessorsByTask());
        graph.calculateCpmMetricsInForwardDirection();

        graph.calculateCpmMetricsInBackwardDirection();

        assertTrue(graph.getNodesByTask().values().stream().allMatch(Node::isResolvedBackward));
    }

    @Test
    @DisplayName("Should throw an exception if starting node has all predecessors resolved")
    public void throwsExceptionIfEndNodeHasAllPredecessorsResolved() {
        CpmProcessor processor = factory.createCpmProcessor(createTasksSetForComplexScenario());
        CpmGraph graph =
                new CpmGraph(processor.getAllTasks(), processor.getPredecessorsByTask(), processor.getSuccessorsByTask());
        graph.getEndNode().getPredecessors().forEach(node -> node.setResolvedBackward(true));

        RuntimeException thrown = assertThrows(RuntimeException.class, graph::calculateCpmMetricsInBackwardDirection);
        assertEquals("Ending node should have at least one unresolved predecessor", thrown.getMessage());
    }

    @Test
    @DisplayName("Should calculate latest start and end for each node in graph simple scenario")
    public void calculateCpmMetricsInBackwardDirectionSimpleTest() {
        CpmProcessor processor = factory.createCpmProcessor(createSimpleTasksSet());
        CpmGraph graph =
                new CpmGraph(processor.getAllTasks(), processor.getPredecessorsByTask(), processor.getSuccessorsByTask());
        graph.calculateCpmMetricsInForwardDirection();

        graph.calculateCpmMetricsInBackwardDirection();

        assertTrue(graph.getNodesByTask().values().stream().allMatch(Node::isResolvedBackward));
    }

    @Test
    @DisplayName("Should calculate latest start and end for each node in graph complex scenario")
    public void calculateCpmMetricsInBackwardDirectionComplexTest() {
        CpmProcessor processor = factory.createCpmProcessor(createTasksSetForComplexScenario());
        CpmGraph graph =
                new CpmGraph(processor.getAllTasks(), processor.getPredecessorsByTask(), processor.getSuccessorsByTask());
        graph.calculateCpmMetricsInForwardDirection();

        graph.calculateCpmMetricsInBackwardDirection();

        assertTrue(graph.getNodesByTask().values().stream().allMatch(Node::isResolvedBackward));
    }

}
