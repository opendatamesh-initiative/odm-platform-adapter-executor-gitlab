package org.opendatamesh.platform.up.executor.gitlabci.resources.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitlabVariable {
    private Boolean isSecret;
    private String value;
}
