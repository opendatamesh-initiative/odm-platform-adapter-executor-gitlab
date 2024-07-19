package org.opendatamesh.platform.up.executor.gitlabci.clients;

import org.opendatamesh.platform.core.commons.clients.ODMApiRoutes;

/**
 * Enumeration that contains the routes to the ODM parameters module.
 */
public enum ParamsServerApiUrl implements ODMApiRoutes {
    ADD_PARAM("/api/v1/pp/params/params/"),
    GET_PARAMS("/api/v1/pp/params/params"),
    GET_PARAM_BY_ID("/api/v1/pp/params/params/{paramId}"),
    GET_PARAM_BY_NAME("/api/v1/pp/params/params/filter?name={paramName}"),
    DELETE_PARAM("/api/v1/pp/params/params/{paramId}");

    private final String path;

    ParamsServerApiUrl(String path) {
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
