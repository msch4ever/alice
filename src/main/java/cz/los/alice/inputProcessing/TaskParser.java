package cz.los.alice.inputProcessing;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.los.alice.inputProcessing.parsedModels.ParsedTask;
import cz.los.alice.model.Task;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TaskParser {

    @Value("classpath:input/LEO2-BE.json")
    private Resource resourceFile;
    private final Converter converter;

    @SneakyThrows
    public List<Task> parseInputFile() {
        ObjectMapper objectMapper = new ObjectMapper();

        List<ParsedTask> tasks = Arrays.asList(objectMapper.readValue(resourceFile.getFile(), ParsedTask[].class));

        validateTasksSize(tasks);
        validateHasRootTasks(tasks);

        return tasks.stream().map(converter::toTask).collect(Collectors.toList());
    }

    private void validateTasksSize(List<ParsedTask> tasks) {
        if (tasks.size() < 2) {
            throw new IllegalArgumentException("Input JSON file shoud contain at least 1 task");
        }
    }

    private void validateHasRootTasks(List<ParsedTask> tasks) {
        if (tasks.stream().allMatch(it -> it.getDependencies().size() > 0)) {
            throw new IllegalArgumentException("Input JSON file should contain at least one task with no dependencies");
        }
    }

}
