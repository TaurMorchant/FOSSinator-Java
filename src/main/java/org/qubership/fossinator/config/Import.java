package org.qubership.fossinator.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Import {
    @JsonProperty("old-name")
    private String oldName;
    @JsonProperty("new-name")
    private String newName;
}
