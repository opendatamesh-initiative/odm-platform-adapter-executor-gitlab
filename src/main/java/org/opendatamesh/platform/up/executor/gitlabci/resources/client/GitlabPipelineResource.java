package org.opendatamesh.platform.up.executor.gitlabci.resources.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitlabPipelineResource {
    private String ref;
    private Map<String, String> variables;
}
