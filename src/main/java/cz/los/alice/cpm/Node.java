package cz.los.alice.cpm;

import cz.los.alice.model.Task;
import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

@Data
@ToString(exclude = {"predecessors", "successors"})
public class Node {

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

    private List<Node> predecessors;
    private List<Node> successors;

    public Node(Task task) {
        this.id = UUID.randomUUID();
        this.task = task;
        this.duration = task.getDuration() == null ? 0 : task.getDuration();
    }
}
