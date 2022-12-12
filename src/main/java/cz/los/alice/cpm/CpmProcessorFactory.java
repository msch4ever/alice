package cz.los.alice.cpm;

import cz.los.alice.model.Crew;
import cz.los.alice.model.Task;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

/**
 * A factory class that supplies with new {@link CpmProcessor} instances.
 */
@Component
public class CpmProcessorFactory {

    public static final String START = "START";
    public static final String END = "END";

    /**
     * Constructs a new instance of {@link CpmProcessor} based on a Set of {@link Task}s<br>
     * The logic is as follows:<br>
     * 1) A Set of Tasks that does not have any predecessors is gathered from all Tasks Set.
     * These tasks are considered as "RootTasks" - potential starting points of the project.<br>
     * 2) A Set of Tasks that does not have successors is gathered from all Tasks Set.
     * These tasks are considered as "TerminalTasks" - potential ending points of the project.<br>
     * 3) A pseudo-tasks called START and END with "0" duration and "0" workers are created.
     * Root tasks are linked with the START task and Terminal tasks are linked with the END task.
     * This is needed to narrow the Graph that will be created in future down to a single start and end point.<br>
     * 4) Two Maps are created in the end, that group Tasks by predecessor's and successor's {@link Task#getTaskCode()}.
     * This is needed to ease the searching when constructing a Graph and calculations related to this.
     * @param tasks - a Set of tasks needed to complete the project
     * @return {@link CpmProcessor}
     */
    public CpmProcessor createCpmProcessor(Set<Task> tasks) {
        if (ObjectUtils.isEmpty(tasks)) {
            throw new IllegalArgumentException("Provided tasks Set should not be null and have at least one task");
        }
        CpmProcessor processor = new CpmProcessor(tasks);
        processor.setRootTasks(findRootTasks(processor));
        processor.setTerminalTasks(findTerminalTasks(processor));
        prepareStartPoint(processor);
        prepareEndPoint(processor);
        processor.setPredecessorsByTask(createPredecessorsByTask(processor));
        processor.setSuccessorsByTask(createSuccessorsByTask(processor));
        return processor;
    }

    /**
     * Finds Tasks that does not have any predecessors
     * @param processor {@link CpmProcessor} that is being build.
     * @return a Set of tasks that are considered as potential starting points of the project.
     */
    private Set<Task> findRootTasks(CpmProcessor processor) {
        Set<Task> tasks = processor.getAllTasks();
        return tasks.stream()
                .filter(it -> it.getDependencies().isEmpty())
                .collect(toSet());
    }

    /**
     * Finds Tasks that does not have any successors
     * @param processor {@link CpmProcessor} that is being build.
     * @return a Set of tasks that are considered as potential ending points of the project.
     */
    private Set<Task> findTerminalTasks(CpmProcessor processor) {
        Set<Task> tasks = processor.getAllTasks();
        return tasks.stream().filter(it -> {
            for (var task : tasks) {
                if (task.getDependencies().contains(it.getTaskCode())) {
                    return false;
                }
            }
            return true;
        }).collect(toSet());
    }

    /**
     * Creates a pseudo-tasks called START with "0" duration and "0" workers.
     * Root tasks are linked with the START task.
     * @param processor {@link CpmProcessor} that is being build.
     */
    private void prepareStartPoint(CpmProcessor processor) {
        Set<Task> tasks = processor.getAllTasks();
        Task startTask = Task.builder()
                .taskCode(START)
                .duration(0)
                .crew(Crew.builder()
                        .assignment(0)
                        .build())
                .operationName(START)
                .dependencies(Collections.emptyList())
                .build();
        createDependencyOnStartTaskForRootTasks(processor, startTask);
        tasks.add(startTask);
    }

    private void createDependencyOnStartTaskForRootTasks(CpmProcessor processor, Task startTask) {
        Set<Task> rootTasks = processor.getRootTasks();
        for (var rootTask : rootTasks) {
            rootTask.setDependencies(List.of(startTask.getTaskCode()));
        }
    }

    /**
     * Creates a pseudo-tasks called END with "0" duration and "0" workers.
     * Terminal tasks are linked with the END task.
     * @param processor {@link CpmProcessor} that is being build.
     */
    private void prepareEndPoint(CpmProcessor processor) {
        Set<Task> tasks = processor.getAllTasks();
        Task finishTask = Task.builder()
                .taskCode(END)
                .duration(0)
                .crew(Crew.builder()
                        .assignment(0)
                        .build())
                .operationName(END)
                .dependencies(processor.getTerminalTasks().stream()
                        .map(Task::getTaskCode)
                        .collect(toList()))
                .build();
        tasks.add(finishTask);
    }

    /**
     * @param processor {@link CpmProcessor} that is being build.
     * @return a Map that groups Tasks by predecessor's {@link Task#getTaskCode()}.
     */
    private Map<String, List<Task>> createPredecessorsByTask(CpmProcessor processor) {
        Set<Task> tasks = processor.getAllTasks();
        Map<String, List<Task>> predecessorsByTask = new HashMap<>();
        for (var currentTask : tasks) {
            predecessorsByTask
                    .put(currentTask.getTaskCode(), getPredecessorTasks(tasks, currentTask.getDependencies()));
        }
        return predecessorsByTask;
    }

    private List<Task> getPredecessorTasks(Set<Task> tasks, List<String> dependencies) {
        List<Task> predecessorTasks = new ArrayList<>();
        for (String currentCode : dependencies) {
            tasks.stream()
                    .filter(task -> task.getTaskCode().equals(currentCode))
                    .findFirst()
                    .ifPresent(predecessorTasks::add);
        }
        return predecessorTasks;
    }

    /**
     * @param processor {@link CpmProcessor} that is being build.
     * @return a Map that groups Tasks by successor's {@link Task#getTaskCode()}.
     */
    private Map<String, List<Task>> createSuccessorsByTask(CpmProcessor processor) {
        Set<Task> tasks = processor.getAllTasks();
        return tasks.stream()
                .collect(toMap(
                        Task::getTaskCode,
                        task -> tasks.stream()
                                .filter(it -> it.getDependencies().contains(task.getTaskCode()))
                                .collect(toList())));
    }
}
