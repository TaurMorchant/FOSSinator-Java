package org.qubership.fossinator.config.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Builder
public class Dependency {
    @JsonProperty("group-id")
    private String groupId;

    @JsonProperty("artifact-id")
    private String artifactId;

    private String version;

    private String scope;
}
