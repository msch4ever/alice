package cz.los.alice.service;

import cz.los.alice.cpm.CpmGraph;
import cz.los.alice.cpm.CpmProcessor;
import cz.los.alice.cpm.CpmProcessorFactory;
import cz.los.alice.model.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is a single method stateless service object that is used to process the predefined JSON file that contains
 * a List of Tasks.
 * To parse and validate input from file {@link TaskParser} is used.
 * To get a new instance of {@link CpmProcessor} a {@link CpmProcessorFactory} is used.
 */
@Service
@RequiredArgsConstructor
public class AliceService {

    private final TaskParser parser;
    private final CpmProcessorFactory processorFactory;

    /**
     * This method orchestrates all components involved in processing the predefined JSON file.<br>
     * The logic is the following:<br>
     * A Set of {@link Task} is obtained as a result of parsing a predefined JSON file by {@link AliceService#parser}.<br>
     * Then {@link CpmProcessor} is created by {@link CpmProcessorFactory} based on parsed input.<br>
     * After that processor creates a graph using Critical Path Method.<br>
     * From that graph a List of {@link Task#getTaskCode} is obtained that represent the Tasks that are on the critical path
     * and day-by-day statistics for total workers on a construction site in the worst case scenario.<br>
     * As the last step a {@link ProcessingResult} object is created that contains: overall duration of project,
     * most busy day in the project plan with max number of workers on site, a critical path and the list of Tasks
     * needed to complete the project with start and end intervals, represented as List<{@link cz.los.alice.model.EnrichedTask}
     * @return {@link ProcessingResult} object that contains all information specified in requirements
     */
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
