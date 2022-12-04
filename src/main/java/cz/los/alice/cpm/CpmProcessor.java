package cz.los.alice.cpm;

import cz.los.alice.dto.ProcessingResult;
import cz.los.alice.model.Task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                .collect(Collectors.toUnmodifiableList());
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

        processFromStartingNode(nodesByTask.get(startTask));

        return new ProcessingResult("STUB");
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
        startNode.setLatestStart(0);
        startNode.setLatestFinish(0);
    }

    private void processFromStartingNode(Node startingNode) {

    }
}
