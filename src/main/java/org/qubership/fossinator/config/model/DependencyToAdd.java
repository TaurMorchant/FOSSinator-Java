package org.qubership.fossinator.config.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class DependencyToAdd {
    @JsonProperty("if-dependency-exists")
    private Dependency ifDependencyExists;

    @JsonProperty("add-dependency")
    private Dependency addDependency;
}
