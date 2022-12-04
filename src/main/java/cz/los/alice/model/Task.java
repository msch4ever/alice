package cz.los.alice.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Task {

    @EqualsAndHashCode.Include
    private String taskCode;
    private String operationName;
    private String elementName;
    private Integer duration;
    private Crew crew;
    private List<Equipment> equipment;
    private List<String> dependencies;

}
