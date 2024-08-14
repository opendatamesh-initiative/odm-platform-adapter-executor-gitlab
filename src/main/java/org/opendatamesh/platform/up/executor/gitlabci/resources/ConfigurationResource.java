package org.opendatamesh.platform.up.executor.gitlabci.resources;

import lombok.Data;

import java.util.Map;

@Data
public class ConfigurationResource {
    private Map<String, String> params;
}
