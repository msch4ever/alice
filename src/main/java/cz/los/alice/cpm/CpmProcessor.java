package cz.los.alice.cpm;

import cz.los.alice.model.EnrichedTask;
import cz.los.alice.model.Task;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * This class keeps the aggregated data from the parsed input and creates a Graph to utilize Critical Path Method.<br>
 * {@link #allTasks} - all tasks needed to finish the project<br>
 * {@link #rootTasks} - a Set of Tasks that does not have any predecessors<br>
 * {@link #terminalTasks} - a Set of Tasks that does not have any successors<br>
 * {@link #predecessorsByTask} - a Map that groups Tasks by predecessor's {@link Task#getTaskCode()}.<br>
 * {@link #successorsByTask} - a Map that groups Tasks by successor's {@link Task#getTaskCode()}.<br>
 */
@Getter
@Setter
public class CpmProcessor {

    private Set<Task> allTasks;
    private Set<Task> rootTasks;
    private Set<Task> terminalTasks;
    private Map<String, List<Task>> predecessorsByTask;
    private Map<String, List<Task>> successorsByTask;

    public CpmProcessor(Set<Task> allTasks) {
        this.allTasks = new HashSet<>(allTasks);
    }

    /**
     * Creates a {@link CpmGraph} and triggers the Critical Path Method calculation that will find the earliest
     * and the latest start and end for each task in the graph
     * @return a {@link CpmGraph}
     */
    public CpmGraph buildCpmGraph() {
        CpmGraph graph = new CpmGraph(allTasks, predecessorsByTask, successorsByTask);

        graph.calculateCpmMetricsInForwardDirection();
        graph.calculateCpmMetricsInBackwardDirection();

        return graph;
    }

    /**
     * Constructs a sorted List of {@link Task}'s task codes that lie on the critical path of the project. The logic
     * starts with the START node and finds a first successor with {@link Node}'s slack == 0. Then the same logic applies
     * to just found node until the END node is reached.
     * @param cpmGraph {@link CpmGraph}
     * @return a sorted List of {@link Task}'s task codes that lie on the critical path of the project.
     */
    public List<String> buildCriticalPath(CpmGraph cpmGraph) {
        LinkedList<Node> criticalPath = new LinkedList<>();
        Node current = cpmGraph.getStartNode();
        while (!cpmGraph.getEndNode().equals(current)) {
            criticalPath.add(current.getSuccessors().stream()
                    .filter(it -> it.getSlack() == 0)
                    .min(Comparator.comparingInt(node -> node.getTask().hashCode()))
                    .orElseThrow(() -> new RuntimeException("Non-ending node should have at least one successor")));
            current = criticalPath.peekLast();
        }
        return criticalPath.stream()
                .filter(node -> !cpmGraph.getEndNode().equals(node))
                .map(node -> node.getTask().getTaskCode())
                .collect(toList());
    }

    /**
     * Aggregates data of how many people is on the construction site for each day of the project duration.
     * This is the worst case scenario, where all tasks that can potentially be executed in parallel are considered.<br>
     * If a given day is in between the earliest start and latest end of the task, the workers associated with this
     * task will participate in the sum of workers for this given day.
     * @param cpmGraph {@link CpmGraph}
     * @return a Map that contains a number of workers for every day of project duration
     */
    public Map<Integer, Integer> createWorkersOnSiteStatistics(CpmGraph cpmGraph) {
        Map<Integer, Integer> workersOnSiteStatistics = new TreeMap<>();

        Collection<Node> allNodes = cpmGraph.getNodesByTask().values();

        int projectDuration = cpmGraph.getEndNode().getLatestFinish();

        for (int day = 0; day <= projectDuration; day++) {
            int currentDay = day;
            workersOnSiteStatistics.put(currentDay, allNodes.stream()
                    .filter(it -> currentDay >= it.getEarliestStart() && currentDay < it.getLatestFinish())
                    .map(it -> it.getTask().getCrew().getAssignment())
                    .reduce(Integer::sum)
                    .orElse(0));
        }
        return workersOnSiteStatistics;
    }

    /**
     * @param graph {@link CpmGraph}
     * @return The list of Tasks needed to complete the project with start and end intervals, represented as List<{@link cz.los.alice.model.EnrichedTask}
     */
    public List<EnrichedTask> createEnrichedTasks(CpmGraph graph) {
        return graph.getNodesByTask().values().stream()
                .filter(it -> !it.equals(graph.getStartNode()) && !it.equals(graph.getEndNode()))
                .map(Node::getEnrichedTask)
                .sorted()
                .collect(Collectors.toList());
    }

}
