package cz.los.alice.dto;

import cz.los.alice.cpm.Node;
import lombok.Value;

import java.util.List;

@Value
public class ProcessingResult {

    List<Node> criticalPath;
    Integer mostBusyDay;
    Integer maxWorkersOnSite;

}
