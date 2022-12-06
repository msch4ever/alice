package cz.los.alice.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class Crew {
    String name;
    @NonNull
    Integer assignment;
}
