package cz.los.alice.cpm;

import cz.los.alice.model.Task;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;
import java.util.UUID;

@Data
@ToString(exclude = {"predecessors", "successors"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Node {

    @EqualsAndHashCode.Include
    private UUID id;

    private Task task;
    private Integer duration;

    private Integer earliestStart;
    private Integer earliestFinish;
    private boolean resolvedForward;

    private Integer latestStart;
    private Integer latestFinish;
    private boolean resolvedBackward;

    private Integer slack;

    private Set<Node> predecessors;
    private Set<Node> successors;

    public Node(Task task) {
        this.id = UUID.randomUUID();
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
}
