package cz.los.alice.cpm;

import cz.los.alice.dto.ProcessingResult;
import cz.los.alice.model.Crew;
import cz.los.alice.model.Task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;

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

    public CpmGraph applyCpm() {
        initRootTasks();
        initTerminalTasks();

        prepareStartingPoint();
        prepareFinishPoint();

        buildPredecessorsByTask();
        buildSuccessorsByTask();

        CpmGraph graph = new CpmGraph(allTasks, predecessorsByTask, successorsByTask);

        graph.calculateCpmMetricsInForwardDirection();
        graph.calculateCpmMetricsInBackwardDirection();

        return graph;
    }

    public List<Node> buildCriticalPath(CpmGraph cpmGraph) {
        LinkedList<Node> criticalPath = new LinkedList<>();
        Node current = cpmGraph.getStartNode();
        while (!cpmGraph.getEndNode().equals(current)) {
            current.getSuccessors().stream()
                    .filter(it -> it.getSlack() == 0)
                    .findFirst()
                    .ifPresent(criticalPath::add);
            current = criticalPath.peekLast();
        }
        return criticalPath;
    }

    public Map<Integer, Integer> createWorkersOnSiteStatistics(CpmGraph cpmGraph) {
        Map<Integer, Integer> workersOnSiteStatistics = new TreeMap<>();

        Collection<Node> allNodes = cpmGraph.getNodesByTask().values();

        int projectDuration = cpmGraph.getEndNode().getLatestFinish();

        for (int day = 0; day <= projectDuration; day++) {
            int currentDay = day;
            workersOnSiteStatistics.put(day, allNodes.stream()
                            .filter(it -> currentDay >= it.getEarliestStart() && currentDay < it.getEarliestFinish())
                            .map(it -> it.getTask().getCrew().getAssignment())
                            .reduce(Integer::sum).orElse(0));
        }
        return workersOnSiteStatistics;
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
