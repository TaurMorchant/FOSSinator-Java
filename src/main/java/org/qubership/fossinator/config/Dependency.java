package org.qubership.fossinator.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Dependency {
    @JsonProperty("old-group-id")
    private String oldGroupId;
    @JsonProperty("old-artifact-id")
    private String oldArtifactId;
    @JsonProperty("new-group-id")
    private String newGroupId;
    @JsonProperty("new-artifact-id")
    private String newArtifactId;
    @JsonProperty("new-version")
    private String newVersion;
}
