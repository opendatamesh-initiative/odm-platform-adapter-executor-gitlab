package org.opendatamesh.platform.up.executor.gitlabci.resources.client.gitlab;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GitlabPipelineResource {
    private String ref;
    private List<Map<String, String>> variables;
}
