package cz.los.alice.cpm;

import cz.los.alice.dto.ProcessingResult;
import cz.los.alice.model.Crew;
import cz.los.alice.model.Task;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

public class CpmProcessor {

    private Set<Task> allTasks;
    private Set<Task> rootTasks;
    private Set<Task> terminalTasks;
    private Map<String, List<Task>> predecessorsByTask;
    private Map<String, List<Task>> successorsByTask;
    private final Map<Task, Node> nodesByTask;

    public CpmProcessor(Set<Task> allTasks) {
        this.allTasks = new HashSet<>(allTasks);
        initRootTasks(allTasks);
        initTerminalTasks(allTasks);
        this.nodesByTask = new HashMap<>();
    }

    private void initRootTasks(Set<Task> allTasks) {
        this.rootTasks = allTasks.stream()
                .filter(it -> it.getDependencies().isEmpty())
                .collect(toSet());
    }

    private void initTerminalTasks(Set<Task> allTasks) {
        this.terminalTasks = allTasks.stream()
                .filter(it -> {
                    for (var task : allTasks) {
                        if (task.getDependencies().contains(it.getTaskCode())) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(toSet());
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
                .crew(Crew.builder()
                        .assignment(0)
                        .build())
                .operationName("START")
                .dependencies(Collections.emptyList())
                .build();
        createDependencyOnStartTaskForRootTasks(startTask);
        allTasks.add(startTask);
        return startTask;
    }

    private void createDependencyOnStartTaskForRootTasks(Task startTask) {
        Set<Task> modifiedRootTasks = new HashSet<>();
        for (var rootTask : rootTasks) {
            modifiedRootTasks.add(
                    rootTask.toBuilder()
                            .dependencies(List.of(startTask.getTaskCode()))
                            .build());
        }
        allTasks.removeAll(rootTasks);
        rootTasks = modifiedRootTasks;
        allTasks.addAll(rootTasks);
    }

    private Task prepareFinishPoint() {
        Task finishTask = Task.builder()
                .taskCode("FINISH")
                .duration(0)
                .crew(Crew.builder()
                        .assignment(0)
                        .build())
                .operationName("FINISH")
                .dependencies(terminalTasks.stream()
                        .map(Task::getTaskCode)
                        .collect(toList()))
                .build();
        allTasks.add(finishTask);
        return finishTask;
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

    private void fillCpmMetricsForStartingNode(Task startTask) {
        Node startNode = nodesByTask.get(startTask);
        startNode.setEarliestStart(0);
        startNode.setEarliestFinish(0);
        startNode.setResolvedForward(true);
        startNode.setPredecessors(Collections.emptySet());
    }

    private void processForwardDirection(Node startingNode) {
        Node current = findUnresolvedSuccessorForwardDirection(startingNode)
                .orElseThrow(() -> new RuntimeException("Starting node should have at least one unresolvedSuccessor"));
        while (true) {
            if (isNodeValidForForwardCalculation(current)) {
                calculateEarliestStartAndFinish(current);
                if (isTerminalNode(current)) {
                    break;
                }
                current = findUnresolvedSuccessorForwardDirection(current)
                        .orElseThrow(() ->
                                new RuntimeException("Just calculated Node with successors cannot have resolved successors"));
                continue;
            }
            current = findUnresolvedPredecessorForwardDirection(current);
        }
        nodesByTask.values().stream().filter(it -> !it.isResolvedForward()).findFirst().ifPresent((brokenNode) -> {
            throw new RuntimeException("All nodes should be in resolved state by now! Broken node:" + brokenNode);
        });
    }

    private boolean isTerminalNode(Node node) {
        return ObjectUtils.isEmpty(node.getSuccessors());
    }

    private boolean isStartingNode(Node node) {
        return ObjectUtils.isEmpty(node.getPredecessors());
    }

    private static Node findUnresolvedPredecessorForwardDirection(Node node) {
        return node.getPredecessors().stream()
                .filter(it -> !it.isResolvedForward())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Unresolved Node should have unresolved predecessor"));
    }

    private static Node findUnresolvedSuccessorBackwardDirection(Node node) {
        return node.getSuccessors().stream()
                .filter(it -> !it.isResolvedBackward())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Unresolved Node should have unresolved successor"));
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
        boolean allPredecessorsResolved = node.getPredecessors().stream()
                .filter(other -> other.getEarliestFinish() == null)
                .findFirst()
                .isEmpty();
        boolean notYetResolved = !node.isResolvedForward();
        return allPredecessorsResolved && notYetResolved;

    }

    private boolean isNodeValidForBackwardCalculation(Node node) {
        boolean allSuccessorsResolved = node.getSuccessors().stream()
                .filter(other -> other.getLatestStart() == null)
                .findFirst()
                .isEmpty();
        boolean notYetResolved = !node.isResolvedBackward();
        return allSuccessorsResolved && notYetResolved;

    }

    private void calculateEarliestStartAndFinish(Node node) {
        node.getPredecessors().stream()
                .map(Node::getEarliestFinish)
                .max(Integer::compareTo)
                .ifPresent(node::setEarliestStart);
        node.setEarliestFinish(node.getDuration() + node.getEarliestStart());
        node.setResolvedForward(true);
    }

    private void calculateLatestStartAndFinish(Node node) {
        node.getSuccessors().stream()
                .map(Node::getLatestStart)
                .min(Integer::compareTo)
                .ifPresent(node::setLatestFinish);
        node.setLatestStart(node.getLatestFinish() - node.getDuration());
        node.setSlack(node.getLatestFinish() - node.getEarliestFinish());
        node.setResolvedBackward(true);
    }

    private void processFromEndingNode(Node endingNode) {
        endingNode.setLatestFinish(endingNode.getEarliestFinish());
        endingNode.setLatestStart(endingNode.getEarliestStart());
        endingNode.setSlack(0);
        endingNode.setResolvedBackward(true);
        Node current = findUnresolvedPredecessorBackwardDirection(endingNode)
                .orElseThrow(() -> new RuntimeException("Ending node should have at least one unresolved predecessor"));
        while (true) {
            if (isNodeValidForBackwardCalculation(current)) {
                calculateLatestStartAndFinish(current);
                if (isStartingNode(current)) {
                    break;
                }
                current = findUnresolvedPredecessorBackwardDirection(current)
                        .orElseThrow(() ->
                                new RuntimeException("Just calculated Node with predecessors cannot have resolved predecessors"));
                continue;
            }
            current = findUnresolvedSuccessorBackwardDirection(current);
        }
        nodesByTask.values().stream().filter(it -> !it.isResolvedBackward()).findFirst().ifPresent((brokenNode) -> {
            throw new RuntimeException("All nodes should be in resolved state by now! Broken node:" + brokenNode);
        });
    }
}
