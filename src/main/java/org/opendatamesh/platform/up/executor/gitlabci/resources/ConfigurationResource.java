package org.opendatamesh.platform.up.executor.gitlabci.resources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigurationResource {
    @JsonProperty("params")
    private Map<String, String> params;
}
