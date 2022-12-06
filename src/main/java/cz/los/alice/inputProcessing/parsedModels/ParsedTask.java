package cz.los.alice.inputProcessing.parsedModels;

import lombok.Data;

import java.util.List;

@Data
public class ParsedTask {

    private String taskCode;
    private String operationName;
    private String elementName;
    private Integer duration;
    private ParsedCrew crew;
    private List<ParsedEquipment> equipment;
    private List<String> dependencies;

}
