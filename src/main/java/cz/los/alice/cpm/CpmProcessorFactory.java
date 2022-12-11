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

@Component
public class CpmProcessorFactory {

    public static final String START = "START";
    public static final String END = "END";

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

    private Set<Task> findRootTasks(CpmProcessor processor) {
        Set<Task> tasks = processor.getAllTasks();
        return tasks.stream()
                .filter(it -> it.getDependencies().isEmpty())
                .collect(toSet());
    }

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
