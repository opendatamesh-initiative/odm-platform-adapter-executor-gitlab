package org.opendatamesh.platform.up.executor.gitlabci.resources;

import lombok.Data;

@Data
public class TemplateResource {
    private String projectId;
    private String branch;
}
