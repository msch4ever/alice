package cz.los.alice.cpm;

import com.fasterxml.jackson.annotation.JsonIgnore;
import cz.los.alice.model.EnrichedTask;
import cz.los.alice.model.Task;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@Getter
@Setter
@ToString(exclude = {"predecessors", "successors"})
public class Node {

    private Task task;
    private Integer duration;

    private Integer earliestStart;
    private Integer earliestFinish;
    private boolean resolvedForward;

    private Integer latestStart;
    private Integer latestFinish;
    private boolean resolvedBackward;

    private Integer slack;

    @JsonIgnore
    private Set<Node> predecessors;
    @JsonIgnore
    private Set<Node> successors;

    public Node(Task task) {
        this.task = task;
        this.duration = task.getDuration();
    }

    public boolean calculateEarliestStartAndFinish() {
        if (isValidForForwardCalculation()) {
            predecessors.stream()
                    .map(Node::getEarliestFinish)
                    .max(Integer::compareTo)
                    .ifPresent(this::setEarliestStart);
            setEarliestFinish(duration + earliestStart);
            resolvedForward = true;
            return true;
        }
        return false;
    }

    public boolean calculateLatestStartAndFinish() {
        if (isValidForBackwardCalculation()) {
            successors.stream()
                    .map(Node::getLatestStart)
                    .min(Integer::compareTo)
                    .ifPresent(this::setLatestFinish);
            latestStart = latestFinish - duration;
            slack = latestFinish - earliestFinish;
            resolvedBackward = true;
            return true;
        }
        return false;
    }

    private boolean isValidForForwardCalculation() {
        boolean allPredecessorsResolved = predecessors.stream()
                .filter(other -> other.getEarliestFinish() == null)
                .findFirst()
                .isEmpty();
        return allPredecessorsResolved;
    }

    public boolean isValidForBackwardCalculation() {
        boolean allSuccessorsResolved = successors.stream()
                .filter(other -> other.getLatestStart() == null)
                .findFirst()
                .isEmpty();
        return allSuccessorsResolved;
    }

    public EnrichedTask getEnrichedTask() {
        return new EnrichedTask(task, earliestStart, latestStart, earliestFinish, latestFinish);
    }
}
