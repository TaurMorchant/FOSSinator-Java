package org.qubership.fossinator.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Dependency {
    private final static String ANY_WILDCARD = "*";

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

    public boolean isAnyArtifact() {
        return Objects.equals(oldArtifactId, ANY_WILDCARD);
    }

    public boolean isGroupIdMatch(String groupId) {
        return Objects.equals(this.oldGroupId, groupId);
    }

    public boolean isArtifactIdMatch(String artifactId) {
        return isAnyArtifact() || Objects.equals(this.oldArtifactId, artifactId);
    }
}
