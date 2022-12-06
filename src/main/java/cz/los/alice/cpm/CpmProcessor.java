package cz.los.alice.cpm;

import cz.los.alice.dto.ProcessingResult;
import cz.los.alice.model.Crew;
import cz.los.alice.model.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

public class CpmProcessor {

    public static final String START = "START";
    public static final String END = "END";

    private Set<Task> allTasks;
    private Set<Task> rootTasks;
    private Set<Task> terminalTasks;
    private Map<String, List<Task>> predecessorsByTask;
    private Map<String, List<Task>> successorsByTask;

    public CpmProcessor(Set<Task> allTasks) {
        this.allTasks = new HashSet<>(allTasks);
    }

    public ProcessingResult applyCpm() {
        initRootTasks();
        initTerminalTasks();

        prepareStartingPoint();
        prepareFinishPoint();

        buildPredecessorsByTask();
        buildSuccessorsByTask();

        CpmGraph graph = new CpmGraph(allTasks, predecessorsByTask, successorsByTask);

        graph.calculateCpmMetricsInForwardDirection();
        graph.calculateCpmMetricsInBackwardDirection();

        List<Node> criticalPath = graph.findCriticalPath();

        StringBuilder sb = new StringBuilder("The critical path is: ").append(System.lineSeparator());
        for (var node : criticalPath) {
            sb.append(node.toString()).append(System.lineSeparator());
        }
        System.out.println(sb);

        return new ProcessingResult(sb.toString());
    }

    private void prepareStartingPoint() {
        Task startTask = Task.builder()
                .taskCode(START)
                .duration(0)
                .crew(Crew.builder()
                        .assignment(0)
                        .build())
                .operationName(START)
                .dependencies(Collections.emptyList())
                .build();
        createDependencyOnStartTaskForRootTasks(startTask);
        allTasks.add(startTask);
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

    private void prepareFinishPoint() {
        Task finishTask = Task.builder()
                .taskCode(END)
                .duration(0)
                .crew(Crew.builder()
                        .assignment(0)
                        .build())
                .operationName(END)
                .dependencies(terminalTasks.stream()
                        .map(Task::getTaskCode)
                        .collect(toList()))
                .build();
        allTasks.add(finishTask);
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

    private void initRootTasks() {
        this.rootTasks = allTasks.stream()
                .filter(it -> it.getDependencies().isEmpty())
                .collect(toSet());
    }

    private void initTerminalTasks() {
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
}
