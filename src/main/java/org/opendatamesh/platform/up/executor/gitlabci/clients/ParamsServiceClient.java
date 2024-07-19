package org.opendatamesh.platform.up.executor.gitlabci.clients;

import org.opendatamesh.platform.core.commons.clients.ODMClient;
import org.opendatamesh.platform.core.commons.servers.exceptions.UnprocessableEntityException;
import org.opendatamesh.platform.up.executor.gitlabci.resources.ExecutorApiStandardErrors;
import org.opendatamesh.platform.up.executor.gitlabci.resources.client.params.ParamResource;
import org.opendatamesh.platform.up.executor.gitlabci.utils.ObjectMapperFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Service class to connect to the OpenDataMesh parameter service.
 */
@Service
public class ParamsServiceClient extends ODMClient {
    /**
     * The client UUID to authenticate with the service, in order to retrieve the secret value decrypted.
     */
    private final String clientUUID;
    /**
     * The client prefix, used to set the prefix to the display name of the parameter.
     */
    @Value("${odm.productplane.params-service.client-prefix}")
    private String clientPrefix;

    public ParamsServiceClient(@Value("${odm.productplane.params-service.address}") String serverAddress, @Value("${odm.productplane.params-service.client-uuid}") String clientUUID) {
        super(serverAddress, ObjectMapperFactory.JSON_MAPPER);
        this.clientUUID = clientUUID;
    }

    /**
     * Create a new parameter in the parameter service through an API request.
     * @param param the parameter to create.
     * @return the created parameter.
     */
    public ResponseEntity<ParamResource> createParam(ParamResource param) {
        HttpEntity<ParamResource> entity = new HttpEntity<ParamResource>(param, getHttpHeaders());
        return rest.postForEntity(
                apiUrl(ParamsServerApiUrl.ADD_PARAM),
                entity,
                ParamResource.class
        );
    }

    /**
     * Get the full list of parameter registered in the parameter service through an API request.
     * @return the response entity containing the list of parameters.
     */
    public ResponseEntity<List<ParamResource>> getParams() {
        HttpEntity<ParamResource> entity = new HttpEntity<>(null, getHttpHeaders());
        return rest.exchange(
                apiUrl(ParamsServerApiUrl.GET_PARAMS),
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );
    }

    /**
     * Get a parameter by its numerical id.
     * @param paramId the numerical id of the parameter.
     * @return the response entity containing the parameter, if exists
     */
    public ResponseEntity<ParamResource> getParamById(Long paramId) {
        HttpEntity<ParamResource> entity = new HttpEntity<>(null, getHttpHeaders());
        return rest.exchange(
                apiUrl(ParamsServerApiUrl.GET_PARAM_BY_ID),
                HttpMethod.GET,
                entity,
                ParamResource.class,
                paramId
        );
    }

    /**
     * Get a parameter by its name.
     * @param paramName the name of the parameter (instance url of the GitLab server)
     * @return the response entity containing the parameter, if exists
     */
    public ResponseEntity<ParamResource> getParamByName(String paramName) {
        HttpEntity<ParamResource> entity = new HttpEntity<>(null, getHttpHeaders());
        return rest.exchange(
                apiUrl(ParamsServerApiUrl.GET_PARAM_BY_NAME),
                HttpMethod.GET,
                entity,
                ParamResource.class,
                paramName
        );
    }

    /**
     * Delete a parameter by its name, if its prefix is coherent with the client id.
     * @param paramName the name of the parameter to delete.
     */
    public void deleteParam(String paramName) {
        HttpEntity<ParamResource> entity = new HttpEntity<>(null, getHttpHeaders());
        ResponseEntity<ParamResource> param = getParamByName(paramName);
        if (param.getStatusCode().is2xxSuccessful() && Objects.requireNonNull(param.getBody()).getDisplayName().startsWith(clientPrefix)) {
            rest.exchange(
                    apiUrl(ParamsServerApiUrl.DELETE_PARAM),
                    HttpMethod.DELETE,
                    entity,
                    HttpEntity.class,
                    Objects.requireNonNull(param.getBody()).getId()
            );
        } else {
            throw new UnprocessableEntityException(
                    ExecutorApiStandardErrors.SC403_01_EXECUTOR_FORBIDDEN,
                    "Can't delete a non-scoped parameter [GITLAB]."
            );
        }
    }

    /**
     * Build the headers for the request.
     * @return the http headers.
     */
    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("client-UUID", clientUUID);
        return headers;
    }
}
