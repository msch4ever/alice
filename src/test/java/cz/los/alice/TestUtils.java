package cz.los.alice;

import cz.los.alice.model.Crew;
import cz.los.alice.model.Task;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestUtils {

    public static final Crew SIMPLE_CREW = Crew.builder().assignment(1).build();


    public static Set<Task> createSingleTasksSet() {
        Task firstTask = Task.builder()
                .taskCode("first")
                .dependencies(Collections.emptyList())
                .duration(1)
                .crew(SIMPLE_CREW)
                .build();

        return Set.of(firstTask);
    }

    public static Set<Task> createSimpleTasksSet() {
        Task firstTask = Task.builder()
                .taskCode("first")
                .dependencies(Collections.emptyList())
                .duration(1)
                .crew(SIMPLE_CREW)
                .build();
        Task intermediateTask = Task.builder()
                .taskCode("intermediate")
                .dependencies(List.of("first"))
                .duration(1)
                .crew(SIMPLE_CREW)
                .build();
        Task lastTask = Task.builder()
                .taskCode("last")
                .dependencies(List.of("intermediate"))
                .duration(1)
                .crew(SIMPLE_CREW)
                .build();

        return Set.of(firstTask, intermediateTask, lastTask);
    }

    public static Set<Task> createTasksSetWithMultipleRootsAndTerminalTasks() {
        Task firstRoot = Task.builder()
                .taskCode("firstRoot")
                .dependencies(Collections.emptyList())
                .duration(1)
                .crew(SIMPLE_CREW)
                .build();
        Task secondRoot = Task.builder()
                .taskCode("secondRoot")
                .dependencies(Collections.emptyList())
                .duration(1)
                .crew(SIMPLE_CREW)
                .build();
        Task thirdRoot = Task.builder()
                .taskCode("thirdRoot")
                .dependencies(Collections.emptyList())
                .duration(1)
                .crew(SIMPLE_CREW)
                .build();
        Task intermediateTask = Task.builder()
                .taskCode("intermediate")
                .dependencies(List.of("firstRoot", "secondRoot", "thirdRoot"))
                .duration(1)
                .crew(SIMPLE_CREW)
                .build();
        Task firstTerminal = Task.builder()
                .taskCode("firstTerminal")
                .dependencies(List.of("intermediate"))
                .duration(1)
                .crew(SIMPLE_CREW)
                .build();
        Task secondTerminal = Task.builder()
                .taskCode("secondTerminal")
                .dependencies(List.of("intermediate"))
                .duration(1)
                .crew(SIMPLE_CREW)
                .build();
        Task thirdTerminal = Task.builder()
                .taskCode("thirdTerminal")
                .dependencies(List.of("intermediate"))
                .duration(1)
                .crew(SIMPLE_CREW)
                .build();
        Task fourthTerminal = Task.builder()
                .taskCode("fourthTerminal")
                .dependencies(List.of("intermediate"))
                .duration(1)
                .crew(SIMPLE_CREW)
                .build();


        return Set.of(
                firstRoot,
                secondRoot,
                thirdRoot,
                intermediateTask,
                firstTerminal,
                secondTerminal,
                thirdTerminal,
                fourthTerminal);
    }

    public static Set<Task> createTasksSetForComplexScenario() {
        Task firstRoot = Task.builder()
                .taskCode("firstRoot")
                .dependencies(Collections.emptyList())
                .duration(1)
                .crew(SIMPLE_CREW)
                .build();
        Task secondRoot = Task.builder()
                .taskCode("secondRoot")
                .dependencies(Collections.emptyList())
                .duration(1)
                .crew(SIMPLE_CREW)
                .build();
        Task firstIntermediateTask = Task.builder()
                .taskCode("firstIntermediateTask")
                .dependencies(List.of("secondRoot"))
                .duration(1)
                .crew(SIMPLE_CREW)
                .build();
        Task secondIntermediateTask = Task.builder()
                .taskCode("secondIntermediateTask")
                .dependencies(List.of("firstIntermediateTask"))
                .duration(1)
                .crew(SIMPLE_CREW)
                .build();
        Task thirdIntermediateTask = Task.builder()
                .taskCode("thirdIntermediateTask")
                .dependencies(List.of("firstRoot"))
                .duration(1)
                .crew(SIMPLE_CREW)
                .build();
        Task fourthIntermediateTask = Task.builder()
                .taskCode("fourthIntermediateTask")
                .dependencies(List.of("firstIntermediateTask", "thirdIntermediateTask"))
                .duration(1)
                .crew(SIMPLE_CREW)
                .build();
        Task firstTerminal = Task.builder()
                .taskCode("firstTerminal")
                .dependencies(List.of("fourthIntermediateTask"))
                .duration(1)
                .crew(SIMPLE_CREW)
                .build();
        Task secondTerminal = Task.builder()
                .taskCode("secondTerminal")
                .dependencies(List.of("secondRoot"))
                .duration(1)
                .crew(SIMPLE_CREW)
                .build();
        Task thirdTerminal = Task.builder()
                .taskCode("thirdTerminal")
                .dependencies(List.of("secondIntermediateTask"))
                .duration(1)
                .crew(SIMPLE_CREW)
                .build();
        Task fourthTerminal = Task.builder()
                .taskCode("fourthTerminal")
                .dependencies(List.of("thirdIntermediateTask", "firstIntermediateTask", "firstRoot"))
                .duration(1)
                .crew(SIMPLE_CREW)
                .build();

        return Set.of(
                firstRoot,
                secondRoot,
                firstIntermediateTask,
                secondIntermediateTask,
                thirdIntermediateTask,
                fourthIntermediateTask,
                firstTerminal,
                secondTerminal,
                thirdTerminal,
                fourthTerminal);
    }
}
