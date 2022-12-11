package cz.los.alice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Task {

    @NonNull
    @EqualsAndHashCode.Include
    String taskCode;
    String operationName;
    String elementName;
    Integer duration;
    Crew crew;
    List<Equipment> equipment;
    @NonNull
    List<String> dependencies;

}
