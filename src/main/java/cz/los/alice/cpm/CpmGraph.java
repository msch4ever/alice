package cz.los.alice.cpm;

import cz.los.alice.model.Task;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cz.los.alice.cpm.CpmProcessorFactory.END;
import static cz.los.alice.cpm.CpmProcessorFactory.START;
import static java.util.stream.Collectors.toSet;

/**
 * This class is a bidirectional graph with single starting and ending points.<br>
 * {@link #startNode} - {@link Node} that has a pseudo-task called START. It is linked to all RootTasks in forward direction<br>
 * {@link #endNode} - {@link Node} that has a pseudo-task called END. It is linked to all TerminalTasks in backward direction<br>
 * {@link #nodesByTask} - {@link Map} that groups Nodes by corresponding Task. Used for faster navigation in Graph
 */
@Getter
public class CpmGraph {

    private Node startNode;
    private Node endNode;
    private Map<Task, Node> nodesByTask;

    public CpmGraph(Set<Task> tasks,
                    Map<String, List<Task>> predecessorsByTask,
                    Map<String, List<Task>> successorsByTask) {
        this.nodesByTask = tasks.stream().collect(Collectors.toMap(Function.identity(), Node::new));
        createNodeDependencies(predecessorsByTask, successorsByTask);
        initStartAndEndNodes();
    }

    /**
     * Performs a forward traversal of the graph, starting from {@link #startNode} and calculating earliest start and
     * end time of the Task<br>
     * The algorithm is the following:<br>
     * Starting with the {@link #startNode} as a current node it searches for a successor that is not resolved yet.
     * As we start from the pseudo-task START its Node is by default already resolved, all successors of starting node
     * can be calculated. The algorithm will try to resolve the current Node. The current node is eligible for
     * calculation if the following condition is met - all predecessors of this graph Node have to be resolved
     * in forward direction - that is, all predecessors have to have the earliest end already calculated. If it is met,
     * the earliest start and end of current node will be calculated, and it will be marked as resolved in forward
     * direction. Then it tries to find an unresolved successor. If such successor is found, it becomes the current Node
     * for the next iteration of the loop. If and attempt to resolve the current node failed, it means that the
     * condition of eligibility for calculation was not met. In this case the algorithm will find a not resolved
     * predecessor and make it the current Node for the next iteration of the loop.
     * The condition to jump out from the loop is to reach the {@link #endNode} and resolve it.
     * By the end of this method all nodes in Graph should be in "resolved in forward direction" state, otherwise
     * a runtime exception will be thrown indicating that something went wrong
     */
    public void calculateCpmMetricsInForwardDirection() {
        fillEarliestStartAndFinishForStartNode();
        Node current = findUnresolvedSuccessorForwardDirection(startNode)
                .orElseThrow(() -> new RuntimeException("Starting node should have at least one unresolvedSuccessor"));
        while (true) {
            if (current.calculateEarliestStartAndFinish()) {
                if (endNode.equals(current)) {
                    break;
                }
                current = findUnresolvedSuccessorForwardDirection(current)
                        .orElseThrow(() ->
                                new RuntimeException("Just calculated Node with successors cannot have resolved successors"));
            } else {
                current = findUnresolvedPredecessorForwardDirection(current);
            }
        }
        nodesByTask.values().stream().filter(it -> !it.isResolvedForward()).findFirst().ifPresent((brokenNode) -> {
            throw new RuntimeException("All nodes should be in resolved state by now! Broken node:" + brokenNode);
        });
    }

    /**
     * Performs a backward traversal of the graph, starting from {@link #endNode} and calculating earliest start and
     * end time of the Task<br>
     * The algorithm is the following:<br>
     * Starting with the {@link #endNode} as a current node it searches for a predecessor that is not resolved yet.
     * As we start from the pseudo-task END its Node is by default already resolved, all predecessors of ending node
     * can be calculated. The algorithm will try to resolve the current Node. The current node is eligible for
     * calculation if the following condition is met - all successors of this graph Node have to be resolved
     * in backward direction - that is, all successors have to have the latest start already calculated. If it is met,
     * the latest start and end of current node will be calculated, and it will be marked as resolved in backward
     * direction. Then it tries to find an unresolved predecessor. If such predecessor is found, it becomes the current
     * Node for the next iteration of the loop. If and attempt to resolve the current node failed, it means that the
     * condition of eligibility for calculation was not met. In this case the algorithm will find a not resolved
     * successor and make it the current Node for the next iteration of the loop.
     * The condition to jump out from the loop is to reach the {@link #startNode} and resolve it.
     * By the end of this method all nodes in Graph should be in "resolved in backward direction" state, otherwise
     * a runtime exception will be thrown indicating that something went wrong
     */
    public void calculateCpmMetricsInBackwardDirection() {
        fillLatestStartAndFinishForEndNode();
        Node current = findUnresolvedPredecessorBackwardDirection(endNode)
                .orElseThrow(() -> new RuntimeException("Ending node should have at least one unresolved predecessor"));
        while (true) {
            if (current.calculateLatestStartAndFinish()) {
                if (startNode.equals(current)) {
                    break;
                }
                current = findUnresolvedPredecessorBackwardDirection(current)
                        .orElseThrow(() ->
                                new RuntimeException("Just calculated Node with predecessors cannot have resolved predecessors"));
            } else {
                current = findUnresolvedSuccessorBackwardDirection(current);
            }
        }
        nodesByTask.values().stream().filter(it -> !it.isResolvedBackward()).findFirst().ifPresent((brokenNode) -> {
            throw new RuntimeException("All nodes should be in resolved state by now! Broken node:" + brokenNode);
        });
    }

    private void fillEarliestStartAndFinishForStartNode() {
        startNode.setEarliestStart(0);
        startNode.setEarliestFinish(0);
        startNode.setResolvedForward(true);
        startNode.setPredecessors(Collections.emptySet());
    }

    private void fillLatestStartAndFinishForEndNode() {
        endNode.setLatestFinish(endNode.getEarliestFinish());
        endNode.setLatestStart(endNode.getEarliestStart());
        endNode.setSlack(0);
        endNode.setResolvedBackward(true);
    }

    private static Optional<Node> findUnresolvedSuccessorForwardDirection(Node node) {
        if (node.getSuccessors() == null) {
            return Optional.empty();
        }
        return node.getSuccessors().stream()
                .filter(it -> !it.isResolvedForward())
                .findFirst();
    }

    private static Node findUnresolvedPredecessorForwardDirection(Node node) {
        return node.getPredecessors().stream()
                .filter(it -> !it.isResolvedForward())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Unresolved Node should have unresolved predecessor"));
    }

    private static Optional<Node> findUnresolvedPredecessorBackwardDirection(Node node) {
        if (node.getPredecessors() == null) {
            return Optional.empty();
        }
        return node.getPredecessors().stream()
                .filter(it -> !it.isResolvedBackward())
                .findFirst();
    }

    private static Node findUnresolvedSuccessorBackwardDirection(Node node) {
        return node.getSuccessors().stream()
                .filter(it -> !it.isResolvedBackward())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Unresolved Node should have unresolved successor"));
    }

    /**
     * Links each {@link Node} of the graph by filling up lists of predecessor and successor Nodes
     * @param predecessorsByTask - Map with key that is a Task code and value representing List of tasks that this task
     *                           depend on
     * @param successorsByTask - Map with key that is a Task code and value representing List of tasks that depend on
     *                         this task
     */
    private void createNodeDependencies(Map<String, List<Task>> predecessorsByTask,
                                        Map<String, List<Task>> successorsByTask) {
        for (var currentNode : nodesByTask.values()) {
            String taskCode = currentNode.getTask().getTaskCode();
            Set<Node> predecessors = predecessorsByTask.get(taskCode).stream()
                    .map(nodesByTask::get)
                    .collect(toSet());
            currentNode.setPredecessors(predecessors);
            Set<Node> successors = successorsByTask.get(taskCode).stream()
                    .map(nodesByTask::get)
                    .collect(toSet());
            currentNode.setSuccessors(successors);
        }
    }

    private void initStartAndEndNodes() {
        for (var node : nodesByTask.values()) {
            if (START.equals(node.getTask().getTaskCode())) {
                this.startNode = node;
            }
            if (END.equals(node.getTask().getTaskCode())) {
                this.endNode = node;
            }
        }
        if (startNode == null || endNode == null) {
            throw new RuntimeException("Start and End nodes have to exist by this moment");
        }
    }
}