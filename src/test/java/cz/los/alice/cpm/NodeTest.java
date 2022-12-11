package cz.los.alice.cpm;

import cz.los.alice.model.EnrichedTask;
import cz.los.alice.model.Task;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NodeTest {

    @Test
    @DisplayName("Should calculate earliest start and finish of the node")
    public void calculateEsEfHappyCase() {
        Task predecessorTask = Task.builder()
                .taskCode("predecessorTask")
                .duration(2)
                .dependencies(Collections.emptyList())
                .build();
        Node predecessor = new Node(predecessorTask);
        predecessor.setEarliestFinish(42);
        predecessor.setResolvedForward(true);
        Task task = Task.builder()
                .taskCode("task")
                .duration(2)
                .dependencies(Collections.emptyList())
                .build();
        Node node = new Node(task);
        node.setPredecessors(Set.of(predecessor));

        boolean result = node.calculateEarliestStartAndFinish();

        assertTrue(result);
        assertEquals(42, node.getEarliestStart());
        assertEquals(44, node.getEarliestFinish());
        assertTrue(node.isResolvedForward());
    }

    @Test
    @DisplayName("Should not calculate earliest start and finish of the node if predecessors not resolved yet")
    public void cantCalculateEsEfTest() {
        Task predecessorTask = Task.builder()
                .taskCode("predecessorTask")
                .duration(2)
                .dependencies(Collections.emptyList())
                .build();
        Node predecessor = new Node(predecessorTask);
        Task task = Task.builder()
                .taskCode("task")
                .duration(2)
                .dependencies(Collections.emptyList())
                .build();
        Node node = new Node(task);
        node.setPredecessors(Set.of(predecessor));

        boolean result = node.calculateEarliestStartAndFinish();

        assertFalse(result);
        assertNull(node.getEarliestStart());
        assertNull(node.getEarliestFinish());
        assertFalse(node.isResolvedForward());
    }

    @Test
    @DisplayName("Should calculate earliest start and finish of the node based on the max earliest finish of predecessor")
    public void calculateEsEfMultiplePredecessorsTest() {
        Task firstPredecessorTask = Task.builder()
                .taskCode("predecessorTask")
                .duration(0)
                .dependencies(Collections.emptyList())
                .build();
        Task secondPredecessorTask = Task.builder()
                .taskCode("predecessorTask")
                .duration(0)
                .dependencies(Collections.emptyList())
                .build();
        Task thirdPredecessorTask = Task.builder()
                .taskCode("predecessorTask")
                .duration(0)
                .dependencies(Collections.emptyList())
                .build();
        Node firstPredecessor = new Node(firstPredecessorTask);
        Node secondPredecessor = new Node(secondPredecessorTask);
        Node thirdPredecessor = new Node(thirdPredecessorTask);
        firstPredecessor.setEarliestFinish(12);
        secondPredecessor.setEarliestFinish(86);
        thirdPredecessor.setEarliestFinish(85);
        firstPredecessor.setResolvedForward(true);
        secondPredecessor.setResolvedForward(true);
        thirdPredecessor.setResolvedForward(true);
        Task task = Task.builder()
                .taskCode("task")
                .duration(2)
                .dependencies(Collections.emptyList())
                .build();
        Node node = new Node(task);
        node.setPredecessors(Set.of(firstPredecessor, secondPredecessor, thirdPredecessor));

        boolean result = node.calculateEarliestStartAndFinish();

        assertTrue(result);
        assertEquals(86, node.getEarliestStart());
        assertEquals(88, node.getEarliestFinish());
        assertTrue(node.isResolvedForward());
    }

    @Test
    @DisplayName("Should not calculate earliest start and finish of the node cause one of predecessors is not resolved yet")
    public void cantCalculateEsEfMultiplePredecessorsTest() {
        Task firstPredecessorTask = Task.builder()
                .taskCode("predecessorTask")
                .duration(0)
                .dependencies(Collections.emptyList())
                .build();
        Task secondPredecessorTask = Task.builder()
                .taskCode("predecessorTask")
                .duration(0)
                .dependencies(Collections.emptyList())
                .build();
        Task thirdPredecessorTask = Task.builder()
                .taskCode("predecessorTask")
                .duration(0)
                .dependencies(Collections.emptyList())
                .build();
        Node firstPredecessor = new Node(firstPredecessorTask);
        Node secondPredecessor = new Node(secondPredecessorTask);
        Node thirdPredecessor = new Node(thirdPredecessorTask);
        firstPredecessor.setEarliestFinish(12);
        thirdPredecessor.setEarliestFinish(85);
        firstPredecessor.setResolvedForward(true);
        thirdPredecessor.setResolvedForward(true);
        Task task = Task.builder()
                .taskCode("task")
                .duration(2)
                .dependencies(Collections.emptyList())
                .build();
        Node node = new Node(task);
        node.setPredecessors(Set.of(firstPredecessor, secondPredecessor, thirdPredecessor));

        boolean result = node.calculateEarliestStartAndFinish();

        assertFalse(result);
        assertNull(node.getEarliestStart());
        assertNull(node.getEarliestFinish());
        assertFalse(node.isResolvedForward());
    }

    @Test
    @DisplayName("Should calculate latest start and finish of the node")
    public void calculateLsLfHappyCase() {
        Task successorTask = Task.builder()
                .taskCode("successorTask")
                .duration(2)
                .dependencies(Collections.emptyList())
                .build();
        Node successor = new Node(successorTask);
        successor.setLatestStart(42);
        successor.setResolvedForward(true);
        successor.setResolvedBackward(true);
        Task task = Task.builder()
                .taskCode("task")
                .duration(2)
                .dependencies(Collections.emptyList())
                .build();
        Node node = new Node(task);
        node.setSuccessors(Set.of(successor));
        node.setEarliestFinish(36);

        boolean result = node.calculateLatestStartAndFinish();

        assertTrue(result);
        assertEquals(42, node.getLatestFinish());
        assertEquals(40, node.getLatestStart());
        assertEquals(6, node.getSlack());
        assertTrue(node.isResolvedBackward());
    }

    @Test
    @DisplayName("Should not calculate latest start and finish of the node if successor not resolved yet")
    public void cantCalculateLsLfTest() {
        Task successorTask = Task.builder()
                .taskCode("successorTask")
                .duration(2)
                .dependencies(Collections.emptyList())
                .build();
        Node successor = new Node(successorTask);
        successor.setResolvedForward(true);
        Task task = Task.builder()
                .taskCode("task")
                .duration(2)
                .dependencies(Collections.emptyList())
                .build();
        Node node = new Node(task);
        node.setSuccessors(Set.of(successor));

        boolean result = node.calculateLatestStartAndFinish();

        assertFalse(result);
        assertNull(node.getLatestFinish());
        assertNull(node.getLatestStart());
        assertFalse(node.isResolvedBackward());
    }

    @Test
    @DisplayName("Should calculate latest start and finish of the node based on the min latest start of successor")
    public void calculateLsLfMultipleSuccessorsTest() {
        Task firstSuccessorTask = Task.builder()
                .taskCode("firstSuccessorTask")
                .duration(0)
                .dependencies(Collections.emptyList())
                .build();
        Task secondSuccessorTask = Task.builder()
                .taskCode("secondSuccessorTask")
                .duration(0)
                .dependencies(Collections.emptyList())
                .build();
        Task thirdSuccessorTask = Task.builder()
                .taskCode("thirdSuccessorTask")
                .duration(0)
                .dependencies(Collections.emptyList())
                .build();
        Node firstSuccessor = new Node(firstSuccessorTask);
        Node secondSuccessor = new Node(secondSuccessorTask);
        Node thirdSuccessor = new Node(thirdSuccessorTask);
        firstSuccessor.setLatestStart(12);
        secondSuccessor.setLatestStart(86);
        thirdSuccessor.setLatestStart(13);
        firstSuccessor.setResolvedBackward(true);
        secondSuccessor.setResolvedBackward(true);
        thirdSuccessor.setResolvedBackward(true);
        Task task = Task.builder()
                .taskCode("task")
                .duration(2)
                .dependencies(Collections.emptyList())
                .build();
        Node node = new Node(task);
        node.setSuccessors(Set.of(firstSuccessor, secondSuccessor, thirdSuccessor));
        node.setEarliestFinish(7);

        boolean result = node.calculateLatestStartAndFinish();

        assertTrue(result);
        assertEquals(10, node.getLatestStart());
        assertEquals(12, node.getLatestFinish());
        assertEquals(5, node.getSlack());
        assertTrue(node.isResolvedBackward());
    }

    @Test
    @DisplayName("Should not calculate latest start and finish of the node cause one of Successors is not resolved yet")
    public void cantCalculateLsLfMultipleSuccessorsTest() {
        Task firstSuccessorTask = Task.builder()
                .taskCode("SuccessorTask")
                .duration(0)
                .dependencies(Collections.emptyList())
                .build();
        Task secondSuccessorTask = Task.builder()
                .taskCode("SuccessorTask")
                .duration(0)
                .dependencies(Collections.emptyList())
                .build();
        Task thirdSuccessorTask = Task.builder()
                .taskCode("SuccessorTask")
                .duration(0)
                .dependencies(Collections.emptyList())
                .build();
        Node firstSuccessor = new Node(firstSuccessorTask);
        Node secondSuccessor = new Node(secondSuccessorTask);
        Node thirdSuccessor = new Node(thirdSuccessorTask);
        firstSuccessor.setLatestStart(12);
        thirdSuccessor.setLatestStart(85);
        firstSuccessor.setResolvedBackward(true);
        thirdSuccessor.setResolvedBackward(true);
        Task task = Task.builder()
                .taskCode("task")
                .duration(2)
                .dependencies(Collections.emptyList())
                .build();
        Node node = new Node(task);
        node.setSuccessors(Set.of(firstSuccessor, secondSuccessor, thirdSuccessor));

        boolean result = node.calculateLatestStartAndFinish();

        assertFalse(result);
        assertNull(node.getEarliestStart());
        assertNull(node.getEarliestFinish());
        assertFalse(node.isResolvedForward());
    }

    @Test
    @DisplayName("Should create eriched task with start interval and end interval")
    public void getEnrichedTaskTest() {
        Task task = Task.builder()
                .taskCode("task")
                .duration(2)
                .dependencies(Collections.emptyList())
                .build();
        Node node = new Node(task);
        node.setEarliestStart(12);
        node.setEarliestFinish(20);
        node.setLatestStart(18);
        node.setLatestFinish(26);

        EnrichedTask result = node.getEnrichedTask();

        assertNotNull(result);
        assertEquals(task, result.getTask());
        assertEquals(12, result.getStatInterval().getFrom());
        assertEquals(18, result.getStatInterval().getTo());
        assertEquals(20, result.getEndInterval().getFrom());
        assertEquals(26, result.getEndInterval().getTo());
    }

}
