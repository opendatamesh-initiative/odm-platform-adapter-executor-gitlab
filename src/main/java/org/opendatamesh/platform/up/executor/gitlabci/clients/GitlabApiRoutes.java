package org.opendatamesh.platform.up.executor.gitlabci.clients;

import lombok.Getter;

/**
 * Enumeration that contains the routes to the gitlab APIs.
 */
@Getter
public enum GitlabApiRoutes {

    GITLAB_PIPELINE_STATUS("/api/v4/projects/{projectId}/pipelines/{pipelineId}"),
    GITLAB_PIPELINE_RUN("/api/v4/projects/{projectId}/pipeline");

    private final String path;

    GitlabApiRoutes(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return this.path;
    }

}
