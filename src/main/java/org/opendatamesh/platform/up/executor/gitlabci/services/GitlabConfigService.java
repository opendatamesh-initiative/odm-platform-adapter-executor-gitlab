package org.opendatamesh.platform.up.executor.gitlabci.services;

import lombok.RequiredArgsConstructor;
import org.opendatamesh.platform.up.executor.gitlabci.clients.ParamsServiceClient;
import org.opendatamesh.platform.up.executor.gitlabci.config.ParamConfiguration;
import org.opendatamesh.platform.up.executor.gitlabci.resources.ExecutorApiStandardErrors;
import org.opendatamesh.platform.up.executor.gitlabci.resources.client.gitlab.GitlabConfigResource;
import org.opendatamesh.platform.up.executor.gitlabci.resources.client.params.ParamResource;
import org.opendatamesh.platform.up.executor.gitlabci.resources.exceptions.ConflictException;
import org.opendatamesh.platform.up.executor.gitlabci.resources.exceptions.UnprocessableEntityException;
import org.springframework.http.HttpStatus;
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

    private final ParamConfiguration paramConfiguration;

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
        paramResource.setDisplayName(paramConfiguration.getClientPrefix() + configResource.getInstanceUrl());
        paramResource.setSecret(true);

        ResponseEntity<ParamResource> createdParam = paramsServiceClient.createParam(paramResource);
        if (!createdParam.getStatusCode().is2xxSuccessful()) {
            if (createdParam.getStatusCode() == HttpStatus.CONFLICT) {
                throw new ConflictException(
                        ExecutorApiStandardErrors.SC409_02_ALREADY_EXISTS,
                        "The parameter already exists"
                );
            }
            throw new UnprocessableEntityException(
                    ExecutorApiStandardErrors.SC500_50_EXECUTOR_SERVICE_ERROR,
                    "Internal server error"
            );
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
            if (param.getDisplayName().startsWith(paramConfiguration.getClientPrefix())) {
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
