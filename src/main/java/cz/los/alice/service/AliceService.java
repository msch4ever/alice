package cz.los.alice.service;

import cz.los.alice.cpm.CpmProcessor;
import cz.los.alice.dto.ProcessingResult;
import cz.los.alice.inputProcessing.TaskParser;
import cz.los.alice.model.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AliceService {

    private final TaskParser parser;

    public ProcessingResult process() {
        Set<Task> tasks = parser.parseInputFile();
        CpmProcessor cpm = new CpmProcessor(tasks);

        return cpm.applyCpm();
    }
}
