package cz.los.alice.model;

import lombok.Value;

/**
 * This class represents a {@link Task} that is enriched with startInterval and endInterval<br>
 * {@link EnrichedTask#statInterval} - a range of days the task can be started without increasing the duration of the project<br>
 * {@link EnrichedTask#endInterval} -a range of days the task can be finished without increasing the duration of the project<br>
 */
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
