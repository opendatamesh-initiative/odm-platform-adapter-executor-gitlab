package org.opendatamesh.platform.up.executor.gitlabci.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class ParamConfiguration {
    /**
     * The client prefix, used to set the prefix to the display name of the parameter.
     */
    @Value("${odm.productplane.params-service.client-prefix}")
    private String clientPrefix;
    @Value("${odm.productplane.params-service.address}")
    private String serverAddress;
    @Value("${odm.productplane.params-service.client-uuid}")
    private String clientUUID;
}
