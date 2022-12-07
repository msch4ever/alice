package cz.los.alice.service;

import cz.los.alice.cpm.CpmGraph;
import cz.los.alice.cpm.CpmProcessor;
import cz.los.alice.cpm.Node;
import cz.los.alice.dto.ProcessingResult;
import cz.los.alice.inputProcessing.TaskParser;
import cz.los.alice.model.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AliceService {

    private final TaskParser parser;

    public ProcessingResult process() {
        Set<Task> tasks = parser.parseInputFile();
        CpmProcessor cpm = new CpmProcessor(tasks);

        CpmGraph cpmGraph = cpm.applyCpm();
        List<Node> criticalPath = cpm.buildCriticalPath(cpmGraph);
        Map<Integer, Integer> workersOnSiteByDay = cpm.createWorkersOnSiteStatistics(cpmGraph);

        Map.Entry<Integer, Integer> mostBusyDay = workersOnSiteByDay.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElseThrow(() -> new RuntimeException("Could not find max value in provided list"));

        return new ProcessingResult(criticalPath, mostBusyDay.getKey(), mostBusyDay.getValue());
    }
}
