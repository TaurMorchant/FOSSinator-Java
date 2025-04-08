package org.qubership.fossinator.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter @Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Config {
    @JsonProperty("java")
    private JavaConfig java;

    @Getter @Setter
    @ToString
    public static class JavaConfig {
        @JsonProperty("imports-to-replace")
        private List<Import> importsToReplace;

        @JsonProperty("dependencies-to-replace")
        private List<Dependency> dependenciesToReplace;

        public Dependency getDependency(String groupId, String artifactId) {
            for (Dependency dep : dependenciesToReplace) {
                if (groupId.equals(dep.getOldGroupId()) && artifactId.equals(dep.getOldArtifactId())) {
                    return dep;
                }
            }
            return null;
        }
    }
}

