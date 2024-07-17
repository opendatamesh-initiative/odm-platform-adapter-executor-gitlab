package org.opendatamesh.platform.up.executor.gitlabci.clients;

import org.opendatamesh.platform.core.commons.clients.ODMApiRoutes;

public enum GitlabApiRoutes implements ODMApiRoutes {

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

    @Override
    public String getPath() {
        return path;
    }
}
