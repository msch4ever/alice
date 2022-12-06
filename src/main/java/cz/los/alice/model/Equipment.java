package cz.los.alice.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Equipment {
    String name;
    Integer quantity;
}
