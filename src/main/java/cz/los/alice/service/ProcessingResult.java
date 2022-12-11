package cz.los.alice.service;

import cz.los.alice.model.EnrichedTask;
import lombok.Value;

import java.util.List;

@Value
public class ProcessingResult {

    Integer estimatedProjectDuration;
    Integer mostBusyDay;
    Integer maxWorkersOnSite;
    List<String> criticalPath;
    List<EnrichedTask> tasksWithStartAndEndDates;

}
