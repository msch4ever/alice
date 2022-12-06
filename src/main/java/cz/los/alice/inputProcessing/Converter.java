package cz.los.alice.inputProcessing;

import cz.los.alice.inputProcessing.parsedModels.ParsedCrew;
import cz.los.alice.inputProcessing.parsedModels.ParsedEquipment;
import cz.los.alice.inputProcessing.parsedModels.ParsedTask;
import cz.los.alice.model.Crew;
import cz.los.alice.model.Equipment;
import cz.los.alice.model.Task;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class Converter {

    public Task toTask(ParsedTask input) {
        return Task.builder()
                .taskCode(input.getTaskCode())
                .operationName((input.getOperationName()))
                .elementName(input.getElementName())
                .duration(input.getDuration())
                .equipment(input.getEquipment().stream().map(this::toEquipment).collect(Collectors.toList()))
                .crew(toCrew(input.getCrew()))
                .dependencies(
                        ObjectUtils.isEmpty(input.getDependencies()) ?
                                Collections.emptyList() :
                                input.getDependencies())
                .build();
    }

    private Equipment toEquipment(ParsedEquipment input) {
        return Equipment.builder()
                .name(input.getName())
                .quantity(input.getQuantity())
                .build();
    }

    private Crew toCrew(ParsedCrew input) {
        if (input == null) {
            return Crew.builder()
                    .name("CREW_STUB")
                    .assignment(0)
                    .build();
        }
        return Crew.builder()
                .name(input.getName())
                .assignment(input.getAssignment())
                .build();
    }

}
