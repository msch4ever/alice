package cz.los.alice.cpm;

import cz.los.alice.model.Task;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cz.los.alice.cpm.CpmProcessor.END;
import static cz.los.alice.cpm.CpmProcessor.START;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

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

    public List<Node> findCriticalPath() {
        return nodesByTask.values().stream()
                .filter(it -> it.getSlack() == 0)
                .sorted(Comparator.comparing(Node::getEarliestStart)).collect(toList());
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