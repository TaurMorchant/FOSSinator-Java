package org.qubership.fossinator.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ImportPattern {
    @JsonProperty("old-package-name")
    private String oldPackageName;
    @JsonProperty("new-package-name")
    private String newPackageName;
}