package cz.los.alice.service;

import cz.los.alice.cpm.CpmGraph;
import cz.los.alice.cpm.CpmProcessor;
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
    private final CpmProcessorFactory processorFactory;

    public ProcessingResult process() {
        Set<Task> tasks = parser.parseInputFile();
        CpmProcessor processor = processorFactory.createCpmProcessor(tasks);

        CpmGraph cpmGraph = processor.buildCpmGraph();
        List<String> criticalPath = processor.buildCriticalPath(cpmGraph);
        Map<Integer, Integer> workersOnSiteByDay = processor.createWorkersOnSiteStatistics(cpmGraph);

        Map.Entry<Integer, Integer> mostBusyDay = workersOnSiteByDay.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElseThrow(() -> new RuntimeException("Could not find max value in provided list"));

        return new ProcessingResult(
                cpmGraph.getEndNode().getLatestFinish(),
                mostBusyDay.getKey(),
                mostBusyDay.getValue(),
                criticalPath,
                processor.createEnrichedTasks(cpmGraph));
    }
}
