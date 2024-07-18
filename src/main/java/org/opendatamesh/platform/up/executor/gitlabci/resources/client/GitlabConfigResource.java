package org.opendatamesh.platform.up.executor.gitlabci.resources.client;

import lombok.Data;

@Data
public class GitlabConfigResource {
    private String instanceUrl;
    private String instanceToken;
}
