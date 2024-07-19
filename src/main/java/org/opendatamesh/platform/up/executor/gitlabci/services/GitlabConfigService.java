package org.opendatamesh.platform.up.executor.gitlabci.services;

import lombok.RequiredArgsConstructor;
import org.opendatamesh.platform.core.commons.servers.exceptions.ConflictException;
import org.opendatamesh.platform.core.commons.servers.exceptions.UnprocessableEntityException;
import org.opendatamesh.platform.up.executor.gitlabci.clients.ParamsServiceClient;
import org.opendatamesh.platform.up.executor.gitlabci.resources.ExecutorApiStandardErrors;
import org.opendatamesh.platform.up.executor.gitlabci.resources.client.gitlab.GitlabConfigResource;
import org.opendatamesh.platform.up.executor.gitlabci.resources.client.params.ParamResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration service for GitLab parameter store.
 */
@Service
@RequiredArgsConstructor
public class GitlabConfigService {
    private final ParamsServiceClient paramsServiceClient;

    @Value("${odm.productplane.params-service.client-prefix}")
    private String clientPrefix;

    /**
     * Add configurations to the parameter service.
     * @param configResource the configuration to be added
     * @return the added configuration
     */
    public ParamResource addConfig(GitlabConfigResource configResource) {
        ResponseEntity<ParamResource> param = paramsServiceClient.getParamByName(configResource.getInstanceUrl());
        if (param.getStatusCode().is2xxSuccessful()) {
            throw new ConflictException(
                    ExecutorApiStandardErrors.SC409_02_ALREADY_EXISTS,
                    "The instance already exists: " + configResource.getInstanceUrl()
            );
        }
        ParamResource paramResource = new ParamResource();
        paramResource.setParamName(configResource.getInstanceUrl());
        paramResource.setParamValue(configResource.getInstanceToken());
        paramResource.setDisplayName(clientPrefix + configResource.getInstanceUrl());
        paramResource.setSecret(true);

        ResponseEntity<ParamResource> createdParam = paramsServiceClient.createParam(paramResource);
        if (!createdParam.getStatusCode().is2xxSuccessful()) {
            switch (createdParam.getStatusCode()) {
                case CONFLICT:
                    throw new ConflictException(
                            ExecutorApiStandardErrors.SC409_02_ALREADY_EXISTS,
                            "The parameter already exists"
                    );
                default:
                    throw new UnprocessableEntityException(
                            ExecutorApiStandardErrors.SC500_50_EXECUTOR_SERVICE_ERROR,
                            "Internal server error"
                    );
            }
        }
        return createdParam.getBody();
    }

    /**
     * @return all the GitLab instance configurations.
     */
    public List<ParamResource> getAllGitlabInstances() {
        List<ParamResource> params = paramsServiceClient.getParams().getBody();

        List<ParamResource> result = new ArrayList<>();
        for (ParamResource param : params) {
            if (param.getDisplayName().startsWith(clientPrefix)) {
                result.add(param);
            }
        }

        return result;
    }

    /**
     * Delete the gitlab configuration by its url.
     * @param instanceId the url of the GitLab instance to be deleted.
     */
    public void deleteGitlabInstance(String instanceId) {
        paramsServiceClient.deleteParam(instanceId);
    }
}
