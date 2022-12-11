package cz.los.alice.model;

import lombok.Value;

@Value
public class EnrichedTask implements Comparable<EnrichedTask> {

    Task task;
    Interval statInterval;
    Interval endInterval;

    public EnrichedTask(Task task, int startFrom, int startTo, int endFrom, int endTo) {
        this.task = task;
        this.statInterval = new Interval(startFrom, startTo);
        this.endInterval = new Interval(endFrom, endTo);
    }

    @Override
    public int compareTo(EnrichedTask other) {
        return this.statInterval.from.compareTo(other.statInterval.from);
    }

    @Value
    public static class Interval {
        Integer from;
        Integer to;

    }
}
