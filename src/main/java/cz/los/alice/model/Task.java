package cz.los.alice.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;

import java.util.List;

@Value
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Task {

    @NonNull
    @EqualsAndHashCode.Include
    String taskCode;
    String operationName;
    String elementName;
    @NonNull
    Integer duration;
    @NonNull
    Crew crew;
    List<Equipment> equipment;
    @NonNull
    List<String> dependencies;

}
