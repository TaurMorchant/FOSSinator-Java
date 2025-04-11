package org.qubership.fossinator.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Import {
    @JsonProperty("old-name")
    private String oldName;
    @JsonProperty("new-name")
    private String newName;
}
