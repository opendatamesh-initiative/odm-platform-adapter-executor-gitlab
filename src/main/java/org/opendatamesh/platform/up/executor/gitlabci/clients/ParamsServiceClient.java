package org.opendatamesh.platform.up.executor.gitlabci.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opendatamesh.platform.up.executor.gitlabci.resources.ExecutorApiStandardErrors;
import org.opendatamesh.platform.up.executor.gitlabci.resources.client.params.ParamResource;
import org.opendatamesh.platform.up.executor.gitlabci.resources.exceptions.GitlabClientException;
import org.opendatamesh.platform.up.executor.gitlabci.resources.exceptions.UnprocessableEntityException;
import org.opendatamesh.platform.up.executor.gitlabci.services.GitlabPipelineService;
import org.opendatamesh.platform.up.executor.gitlabci.utils.ObjectMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Service class to connect to the OpenDataMesh parameter service.
 */
@Service
public class ParamsServiceClient {
    private static final Logger logger = LoggerFactory.getLogger(GitlabPipelineService.class);
    protected String serverAddress;
    protected ObjectMapper mapper;
    public TestRestTemplate rest = new TestRestTemplate();
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
        this.serverAddress = serverAddress;
        this.mapper = ObjectMapperFactory.JSON_MAPPER;
        this.clientUUID = clientUUID;
    }

    /**
     * Create a new parameter in the parameter service through an API request.
     * @param param the parameter to create.
     * @return the created parameter.
     */
    public ResponseEntity<ParamResource> createParam(ParamResource param) {
        HttpEntity<ParamResource> entity = new HttpEntity<ParamResource>(param, getHttpHeaders());

        try {
            return rest.exchange(
                    apiUrl(ParamsServerApiRoutes.ADD_PARAM),
                    HttpMethod.POST,
                    entity,
                    ParamResource.class
            );
        } catch (HttpClientErrorException e) {
            throw new GitlabClientException(e.getRawStatusCode(), e.getResponseBodyAsString());
        }
    }

    /**
     * Get the full list of parameter registered in the parameter service through an API request.
     * @return the response entity containing the list of parameters.
     */
    public ResponseEntity<List<ParamResource>> getParams() {
        HttpEntity<ParamResource> entity = new HttpEntity<>(null, getHttpHeaders());
        logger.info("Contacting param service: {}", apiUrl(ParamsServerApiRoutes.GET_PARAMS));
        try {
            return rest.exchange(
                    apiUrl(ParamsServerApiRoutes.GET_PARAMS),
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {
                    }
            );
        } catch (HttpClientErrorException e) {
            throw new GitlabClientException(e.getRawStatusCode(), e.getResponseBodyAsString());
        }
    }

    /**
     * Get a parameter by its numerical id.
     * @param paramId the numerical id of the parameter.
     * @return the response entity containing the parameter, if exists
     */
    public ResponseEntity<ParamResource> getParamById(Long paramId) {
        HttpEntity<ParamResource> entity = new HttpEntity<>(null, getHttpHeaders());
        try {
            return rest.exchange(
                    apiUrl(ParamsServerApiRoutes.GET_PARAM_BY_ID),
                    HttpMethod.GET,
                    entity,
                    ParamResource.class,
                    paramId
            );
        } catch (HttpClientErrorException e) {
            throw new GitlabClientException(e.getRawStatusCode(), e.getResponseBodyAsString());
        }
    }

    /**
     * Get a parameter by its name.
     * @param paramName the name of the parameter (instance url of the GitLab server)
     * @return the response entity containing the parameter, if exists
     */
    public ResponseEntity<ParamResource> getParamByName(String paramName) {
        HttpEntity<ParamResource> entity = new HttpEntity<>(null, getHttpHeaders());
        try {
            return rest.exchange(
                    apiUrl(ParamsServerApiRoutes.GET_PARAM_BY_NAME),
                    HttpMethod.GET,
                    entity,
                    ParamResource.class,
                    paramName
            );
        } catch (HttpClientErrorException e) {
            throw new GitlabClientException(e.getRawStatusCode(), e.getResponseBodyAsString());
        }
    }

    /**
     * Delete a parameter by its name, if its prefix is coherent with the client id.
     * @param paramName the name of the parameter to delete.
     */
    public void deleteParam(String paramName) {
        HttpEntity<ParamResource> entity = new HttpEntity<>(null, getHttpHeaders());
        ResponseEntity<ParamResource> param = getParamByName(paramName);
        if (param.getStatusCode().is2xxSuccessful() && Objects.requireNonNull(param.getBody()).getDisplayName().startsWith(clientPrefix)) {
            try {
                rest.exchange(
                        apiUrl(ParamsServerApiRoutes.DELETE_PARAM),
                        HttpMethod.DELETE,
                        entity,
                        HttpEntity.class,
                        Objects.requireNonNull(param.getBody()).getId()
                );
            } catch (HttpClientErrorException e) {
                throw new GitlabClientException(e.getRawStatusCode(), e.getResponseBodyAsString());
            }
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

    protected String apiUrlFromString(String servicePath) {
        return this.serverAddress + servicePath;
    }
    public String apiUrl(ParamsServerApiRoutes route, String extension, Map<String, Object> queryParams) {
        String urlTemplate = null;
        String var10000;
        if (extension != null) {
            String var10001 = route.getPath();
            var10000 = this.apiUrlFromString(var10001 + extension);
        } else {
            var10000 = this.apiUrlFromString(route.getPath());
        }

        urlTemplate = var10000;
        if (queryParams != null) {
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(urlTemplate);
            Iterator var6 = queryParams.keySet().iterator();

            while(var6.hasNext()) {
                String paramName = (String)var6.next();
                uriBuilder.queryParam(paramName, "{" + paramName + "}");
            }

            urlTemplate = uriBuilder.encode().toUriString();
        }

        return urlTemplate;
    }
    public String apiUrl(ParamsServerApiRoutes route) {
        return this.apiUrl(route, "", null);
    }
}
