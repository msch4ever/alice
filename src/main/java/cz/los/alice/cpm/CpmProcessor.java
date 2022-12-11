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

    public CpmGraph buildCpmGraph() {
        CpmGraph graph = new CpmGraph(allTasks, predecessorsByTask, successorsByTask);

        graph.calculateCpmMetricsInForwardDirection();
        graph.calculateCpmMetricsInBackwardDirection();

        return graph;
    }

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

    public List<EnrichedTask> createEnrichedTasks(CpmGraph graph) {
        return graph.getNodesByTask().values().stream()
                .filter(it -> !it.equals(graph.getStartNode()) && !it.equals(graph.getEndNode()))
                .map(Node::getEnrichedTask)
                .sorted()
                .collect(Collectors.toList());
    }

}
