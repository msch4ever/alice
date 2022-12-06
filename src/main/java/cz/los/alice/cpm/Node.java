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
    private Integer earliestStart;
    private Integer earliestFinish;
    private boolean resolvedForward;
    private Integer duration;
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
}
