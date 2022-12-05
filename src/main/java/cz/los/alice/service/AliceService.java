package cz.los.alice.service;

import cz.los.alice.cpm.CpmProcessor;
import cz.los.alice.dto.ProcessingResult;
import cz.los.alice.model.Task;
import cz.los.alice.utils.TaskParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AliceService {

    private final TaskParser parser;

    public ProcessingResult process() {
        List<Task> tasks = parser.parseInputFile();
        CpmProcessor cpm = new CpmProcessor(tasks);

        return cpm.applyCpm();
    }
}
