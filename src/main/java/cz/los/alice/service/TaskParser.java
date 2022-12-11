package cz.los.alice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.los.alice.model.Crew;
import cz.los.alice.model.Task;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskParser {

    private static final Crew crewStub = Crew.builder()
            .name("CREW_STUB")
            .assignment(0)
            .build();

    @Value("classpath:input/LEO2-BE.json")
    private Resource resourceFile;

    public TaskParser(Resource resourceFile) {
        this.resourceFile = resourceFile;
    }

    @SneakyThrows
    public Set<Task> parseInputFile() {
        ObjectMapper objectMapper = new ObjectMapper();

        Set<Task> tasks = new HashSet<>(Arrays.asList(objectMapper.readValue(resourceFile.getFile(), Task[].class)));

        validateTasksSize(tasks);
        validateParsedData(tasks);

        return tasks;
    }

    private void validateTasksSize(Set<Task> tasks) {
        if (tasks.size() < 1) {
            throw new IllegalArgumentException("Input JSON file should contain at least 1 task");
        }
    }

    private void validateParsedData(Set<Task> tasks) {
        boolean hasRootTasks = false;
        for (var task : tasks) {
            if (task.getDependencies().size() == 0 && !hasRootTasks) {
                hasRootTasks = true;
            }
            if (task.getDuration() == null) {
                log.warn("Task [{}] had no duration in the provided file. Setting duration to 0.", task.getTaskCode());
                task.setDuration(0);
            }
            if (task.getCrew() == null) {
                log.warn("Task [{}] had no crew in the provided file. Setting crew assignment to 0.", task.getTaskCode());
                task.setCrew(crewStub);
            }
        }
        if (!hasRootTasks) {
            throw new IllegalArgumentException("Input JSON file should contain at least one task with no dependencies");
        }
    }

}
