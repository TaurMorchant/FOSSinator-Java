package org.qubership.fossinator.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Collections;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
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

        @JsonProperty("imports-to-replace-by-pattern")
        private List<ImportPattern> importsToReplaceByPattern;

        @JsonProperty("dependencies-to-replace")
        private List<Dependency> dependenciesToReplace;

        public Dependency getDependency(String groupId, String artifactId) {
            for (Dependency dep : dependenciesToReplace) {
                if (dep.isGroupIdMatch(groupId) && dep.isArtifactIdMatch(artifactId)) {
                    return dep;
                }
            }
            return null;
        }

        public List<Import> getImportsToReplace() {
            return importsToReplace != null ? importsToReplace : Collections.emptyList();
        }
    }
}

