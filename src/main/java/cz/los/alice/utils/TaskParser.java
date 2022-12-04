package cz.los.alice.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.los.alice.model.Task;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class TaskParser {

    @Value("classpath:input/LEO2-BE.json")
    private Resource resourceFile;

    @SneakyThrows
    public List<Task> parseInputFile() {
        ObjectMapper objectMapper = new ObjectMapper();

        List<Task> tasks = Arrays.asList(objectMapper.readValue(resourceFile.getFile(), Task[].class));
        return tasks;
    }

}
