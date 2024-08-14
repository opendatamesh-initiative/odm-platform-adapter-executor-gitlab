package org.opendatamesh.platform.up.executor.gitlabci.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class PipelineConfiguration {
    @Value("${odm.executors.gitlab.pipelines-config.polling.retries}")
    private Integer pollingNumRetries;
    @Value("${odm.executors.gitlab.pipelines-config.polling.interval}")
    private Integer pollingIntervalSeconds;
}
