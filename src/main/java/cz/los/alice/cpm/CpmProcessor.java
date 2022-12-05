package cz.los.alice.cpm;

import cz.los.alice.dto.ProcessingResult;
import cz.los.alice.model.Task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class CpmProcessor {

    private final List<Task> allTasks;
    private final List<Task> rootTasks;
    private final List<Task> terminalTasks;
    private Map<String, List<Task>> predecessorsByTask;
    private Map<String, List<Task>> successorsByTask;
    private final Map<Task, Node> nodesByTask;

    public CpmProcessor(List<Task> allTasks) {
        this.allTasks = new ArrayList<>(allTasks);
        this.rootTasks = allTasks.stream()
                .filter(it -> it.getDependencies().isEmpty())
                .collect(toList());
        this.terminalTasks = allTasks.stream()
                .filter(it -> {
                    for (var task : allTasks) {
                        if (task.getDependencies().contains(it.getTaskCode())) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toUnmodifiableList());
        this.nodesByTask = new HashMap<>();
    }

    public ProcessingResult applyCpm() {
        Task startTask = prepareStartingPoint();
        Task finishTask = prepareFinishPoint();

        buildPredecessorsByTask();
        buildSuccessorsByTask();

        createNodeForEachTask();
        createLinksBetweenNodes(nodesByTask.values());

        fillCpmMetricsForStartingNode(startTask);

        processForwardDirection(nodesByTask.get(startTask));

        processFromEndingNode(nodesByTask.get(finishTask));

        List<Node> criticalPath = nodesByTask.values().stream().filter(it -> it.getSlack() == 0).sorted(Comparator.comparing(Node::getEarliestStart)).collect(toList());

        StringBuilder sb = new StringBuilder("The critical path is: ").append(System.lineSeparator());
        for (var node : criticalPath) {
            sb.append(node.toString()).append(System.lineSeparator());
        }
        System.out.println(sb);

        return new ProcessingResult(sb.toString());
    }

    private Task prepareStartingPoint() {
        Task startTask = Task.builder()
                .taskCode("START")
                .duration(0)
                .operationName("START")
                .dependencies(Collections.emptyList())
                .build();
        createDependencyOnStartTaskForRootTasks(startTask);
        allTasks.add(0, startTask);
        return startTask;
    }

    private void createDependencyOnStartTaskForRootTasks(Task startTask) {
        for (var rootTask : rootTasks) {
            rootTask.setDependencies(List.of(startTask.getTaskCode()));
        }
    }

    private Task prepareFinishPoint() {
        Task finishTask = Task.builder()
                .taskCode("FINISH")
                .duration(0)
                .operationName("FINISH")
                .build();
        createDependencyForTerminalTasks(finishTask);
        allTasks.add(finishTask);
        return finishTask;
    }

    private void createDependencyForTerminalTasks(Task finishTask) {
        finishTask.setDependencies(
                terminalTasks.stream()
                        .map(Task::getTaskCode)
                        .collect(toList()));
    }

    private void buildPredecessorsByTask() {
        predecessorsByTask = new HashMap<>();
        for (var currentTask : allTasks) {
            predecessorsByTask
                    .put(currentTask.getTaskCode(), getPredecessorTasks(currentTask.getDependencies()));
        }
    }

    private List<Task> getPredecessorTasks(List<String> dependencies) {
        List<Task> predecessorTasks = new ArrayList<>();
        for (String currentCode : dependencies) {
            allTasks.stream()
                    .filter(task -> task.getTaskCode().equals(currentCode))
                    .findFirst()
                    .ifPresent(predecessorTasks::add);
        }
        return predecessorTasks;
    }

    private void buildSuccessorsByTask() {
        successorsByTask = allTasks.stream()
                .collect(toMap(
                        Task::getTaskCode,
                        task -> allTasks.stream()
                                .filter(it -> it.getDependencies().contains(task.getTaskCode()))
                                .collect(toList())));
    }

    private void createNodeForEachTask() {
        for (var currentTask : allTasks) {
            Node currentNode = new Node(currentTask);
            nodesByTask.put(currentTask, currentNode);
        }
    }

    private void createLinksBetweenNodes(Collection<Node> allNodes) {
        for (var currentNode : allNodes) {
            String taskCode = currentNode.getTask().getTaskCode();
            List<Node> predecessors = predecessorsByTask.get(taskCode).stream()
                    .map(nodesByTask::get)
                    .collect(toList());
            currentNode.setPredecessors(predecessors);
            List<Node> successors = successorsByTask.get(taskCode).stream()
                    .map(nodesByTask::get)
                    .collect(toList());
            currentNode.setSuccessors(successors);
        }
    }

    private void fillCpmMetricsForStartingNode(Task startTask) {
        Node startNode = nodesByTask.get(startTask);
        startNode.setEarliestStart(0);
        startNode.setEarliestFinish(0);
        startNode.setResolvedForward(true);
        startNode.setPredecessors(Collections.emptyList());
    }

    private void processForwardDirection(Node startingNode) {
        Node current = findUnresolvedSuccessorForwardDirection(startingNode)
                .orElseThrow(() -> new IllegalStateException("Starting node should have at least one unresolvedSuccessor"));
        while (true) {
            if (isNodeValidForForwardCalculation(current) && !current.isResolvedForward()) {
                calculateEarliestStartAndFinish(current);
                current.setResolvedForward(true);
            } else if (!isNodeValidForForwardCalculation(current)) {
                current = findUnresolvedPredecessorForwardDirection(current);
                continue;
            }
            if (hasNoSuccessors(current)) {
                Optional<Node> unresolvedNode = nodesByTask.values().stream()
                        .filter(it -> !it.isResolvedForward())
                        .findFirst();
                if (unresolvedNode.isPresent()) {
                    current = unresolvedNode.get();
                    continue;
                } else {
                    break;
                }
            }
            current = findUnresolvedSuccessorForwardDirection(current)
                    .orElseThrow(() ->
                            new IllegalStateException("Just calculated Node with successors cannot have resolved successors"));
        }
        nodesByTask.values().stream().filter(it -> !it.isResolvedForward()).findFirst().ifPresent((brokenNode) -> {
            throw new IllegalStateException("All nodes should be in resolved state by now! Broken node:" + brokenNode);
        });
    }

    private boolean hasNoSuccessors(Node node) {
        List<Node> successors = node.getSuccessors();
        return successors == null || successors.isEmpty();
    }

    private boolean hasNoPredecessors(Node node) {
        List<Node> predecessors = node.getPredecessors();
        return predecessors == null || predecessors.isEmpty();
    }

    private static Node findUnresolvedPredecessorForwardDirection(Node node) {
        return node.getPredecessors().stream()
                .filter(it -> !it.isResolvedForward())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unresolved Node should have unresolved predecessor"));
    }

    private static Node findUnresolvedSuccessorBackwardDirection(Node node) {
        return node.getSuccessors().stream()
                .filter(it -> !it.isResolvedBackward())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unresolved Node should have unresolved successor"));
    }

    private static Optional<Node> findUnresolvedSuccessorForwardDirection(Node node) {
        if (node.getSuccessors() == null) {
            return Optional.empty();
        }
        return node.getSuccessors().stream()
                .filter(it -> !it.isResolvedForward())
                .findFirst();
    }

    private static Optional<Node> findUnresolvedPredecessorBackwardDirection(Node node) {
        if (node.getPredecessors() == null) {
            return Optional.empty();
        }
        return node.getPredecessors().stream()
                .filter(it -> !it.isResolvedBackward())
                .findFirst();
    }

    private boolean isNodeValidForForwardCalculation(Node node) {
        return node.getPredecessors().stream()
                .filter(other -> other.getEarliestFinish() == null)
                .findFirst()
                .isEmpty();

    }

    private boolean isNodeValidForBackwardCalculation(Node node) {
        return node.getSuccessors().stream()
                .filter(other -> other.getLatestStart() == null)
                .findFirst()
                .isEmpty();

    }

    private void calculateEarliestStartAndFinish(Node node) {
        node.getPredecessors().stream()
                .map(Node::getEarliestFinish)
                .max(Integer::compareTo)
                .ifPresent(node::setEarliestStart);
        node.setEarliestFinish(node.getDuration() + node.getEarliestStart());
    }

    private void calculateLatestStartAndFinish(Node node) {
        node.getSuccessors().stream()
                .map(Node::getLatestStart)
                .min(Integer::compareTo)
                .ifPresent(node::setLatestFinish);
        node.setLatestStart(node.getLatestFinish() - node.getDuration());
        node.setSlack(node.getLatestFinish() - node.getEarliestFinish());
    }

    private void processFromEndingNode(Node endingNode) {
        endingNode.setLatestFinish(endingNode.getEarliestFinish());
        endingNode.setLatestStart(endingNode.getEarliestStart());
        endingNode.setSlack(0);
        endingNode.setResolvedBackward(true);
        Node current = findUnresolvedPredecessorBackwardDirection(endingNode)
                .orElseThrow(() -> new IllegalStateException("Ending node should have at least one unresolved predecessor"));
        while (true) {
            if (isNodeValidForBackwardCalculation(current) && !current.isResolvedBackward()) {
                calculateLatestStartAndFinish(current);
                current.setResolvedBackward(true);
            } else if (!isNodeValidForBackwardCalculation(current)) {
                current = findUnresolvedSuccessorBackwardDirection(current);
                continue;
            }
            if (hasNoPredecessors(current)) {
                Optional<Node> unresolvedNode = nodesByTask.values().stream()
                        .filter(it -> !it.isResolvedBackward())
                        .findFirst();
                if (unresolvedNode.isPresent()) {
                    current = unresolvedNode.get();
                    continue;
                } else {
                    break;
                }
            }
            current = findUnresolvedPredecessorBackwardDirection(current)
                    .orElseThrow(() ->
                            new IllegalStateException("Just calculated Node with predecessors cannot have resolved predecessors"));
        }
        nodesByTask.values().stream().filter(it -> !it.isResolvedBackward()).findFirst().ifPresent((brokenNode) -> {
            throw new IllegalStateException("All nodes should be in resolved state by now! Broken node:" + brokenNode);
        });
    }
}
