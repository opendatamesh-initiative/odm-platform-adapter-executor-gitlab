package org.opendatamesh.platform.up.executor.gitlabci.resources.client.gitlab;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitlabPipelineResource {
    private String ref;
    private List<Map<String, String>> variables;
}
