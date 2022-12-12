package cz.los.alice.cpm;

import com.fasterxml.jackson.annotation.JsonIgnore;
import cz.los.alice.model.EnrichedTask;
import cz.los.alice.model.Task;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

/**
 * A node of the bidirectional Graph that contains info of earliest and latest start and end days for this {@link #task}
 * Links between nodes are represented by {@link #predecessors} and {@link #successors}
 */
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

    /**
     * Calculates the earliest start and finish days for this node.
     * The node is valid for this calculation if all predecessors of this node are already resolved in
     * forward direction - which means all predecessors of this node already have the earliest start and finish
     * calculated. If the node has more than one predecessor, the maximum value of the earliest finish among
     * all the predecessors will be used to calculate values for this node.
     * @return true - if calculation of the earliest start and finish happened, false - if calculation of the earliest
     * start and finish is impossible
     */
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

    /**
     * Calculates the latest start and finish days for this node.
     * The node is valid for this calculation if all successors of this node are already resolved in
     * backward direction - which means all successors of this node already have the latest start and finish
     * calculated. If the node has more than one successor, the minimum value of the latest start among
     * all the successors will be used to calculate values for this node.
     * @return true - if calculation of the latest start and finish happened, false - if calculation of the latest
     * start and finish is impossible
     */
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

    /**
     * @return a new instance of {@link EnrichedTask} created using this nodes {@link #task} and the earliest and
     * the latest start and finish days.
     */
    public EnrichedTask getEnrichedTask() {
        return new EnrichedTask(task, earliestStart, latestStart, earliestFinish, latestFinish);
    }
}
