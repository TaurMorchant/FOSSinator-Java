package org.qubership.fossinator.config.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Dependency {
    @JsonProperty("group-id")
    private String groupId;

    @JsonProperty("artifact-id")
    private String artifactId;

    private String version;

    private String scope;

    private String type;
}
