package cz.los.alice.cpm;

import cz.los.alice.model.Task;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class Node {

    private UUID id;

    private Task task;
    private Integer earliestStart;
    private Integer earliestFinish;
    private Integer duration;
    private Integer latestStart;
    private Integer latestFinish;

    private Integer slack;

    private List<Node> predecessors;
    private List<Node> successors;

    public Node(Task task) {
        this.id = UUID.randomUUID();
        this.task = task;
        this.duration = task.getDuration() == null ? 0 : task.getDuration();
    }
}
