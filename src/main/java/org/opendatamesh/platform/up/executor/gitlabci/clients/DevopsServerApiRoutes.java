package org.opendatamesh.platform.up.executor.gitlabci.clients;

import org.opendatamesh.platform.core.commons.clients.ODMApiRoutes;

/**
 * Enumeration that contains the routes to the ODM devops module.
 */
public enum DevopsServerApiRoutes implements ODMApiRoutes {

    STOP_TASK("/api/v1/pp/devops/tasks/{taskId}/status?action=stop");

    private final String path;

    DevopsServerApiRoutes(String path) {
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
