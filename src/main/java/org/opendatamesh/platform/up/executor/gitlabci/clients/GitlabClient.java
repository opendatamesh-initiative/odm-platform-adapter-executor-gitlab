package org.opendatamesh.platform.up.executor.gitlabci.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opendatamesh.platform.up.executor.gitlabci.resources.client.gitlab.GitlabPipelineResource;
import org.opendatamesh.platform.up.executor.gitlabci.resources.client.gitlab.GitlabRunResource;
import org.opendatamesh.platform.up.executor.gitlabci.resources.exceptions.GitlabClientException;
import org.opendatamesh.platform.up.executor.gitlabci.utils.ObjectMapperFactory;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Iterator;
import java.util.Map;

/**
 * Service class to connect to the GitLab server instance (either on-premise or self-hosted).
 */
public class GitlabClient {
    private final String gitlabToken;
    protected String serverAddress;
    protected ObjectMapper mapper;
    public TestRestTemplate rest;

    public GitlabClient(String serverAddress, String gitlabToken) {
        this.serverAddress = serverAddress;
        this.mapper = ObjectMapperFactory.JSON_MAPPER;
        this.gitlabToken = gitlabToken;
    }

    /**
     * Create a new task on GitLab pipeline and run it.
     *
     * @param pipelineResource the object that contains pipeline details.
     * @param projectId        the id of the gitlab project.
     * @return the created GitLab pipeline.
     */
    public ResponseEntity<GitlabRunResource> postTask(GitlabPipelineResource pipelineResource, String projectId) {
        HttpEntity<GitlabPipelineResource> entity = new HttpEntity<>(pipelineResource, getHttpHeaders());

        try {
            return rest.exchange(
                    apiUrl(GitlabApiRoutes.GITLAB_PIPELINE_RUN),
                    HttpMethod.POST,
                    entity,
                    GitlabRunResource.class,
                    projectId
            );
        } catch (HttpClientErrorException e) {
            throw new GitlabClientException(e.getRawStatusCode(), e.getResponseBodyAsString());
        }
    }

    /**
     * Check the status of a GitLab pipeline.
     *
     * @param projectId  the id of the GitLab project.
     * @param pipelineId the id of the running pipeline.
     * @return the status of the GitLab pipeline.
     */
    public ResponseEntity<GitlabRunResource> readTask(String projectId, String pipelineId) {
        HttpEntity<GitlabPipelineResource> entity = new HttpEntity<>(getHttpHeaders());
        try {
            return rest.exchange(
                    apiUrl(GitlabApiRoutes.GITLAB_PIPELINE_STATUS),
                    HttpMethod.GET,
                    entity,
                    GitlabRunResource.class,
                    projectId,
                    pipelineId
            );
        }
        catch (HttpClientErrorException e) {
            throw new GitlabClientException(e.getRawStatusCode(), e.getResponseBodyAsString());
        }
    }


    /**
     * Create the http headers object, injecting the bearer token.
     *
     * @return the header objects.
     */
    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(gitlabToken);
        return headers;
    }

    protected String apiUrlFromString(String servicePath) {
        return this.serverAddress + servicePath;
    }

    public String apiUrl(GitlabApiRoutes route, String extension, Map<String, Object> queryParams) {
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

            while (var6.hasNext()) {
                String paramName = (String) var6.next();
                uriBuilder.queryParam(paramName, "{" + paramName + "}");
            }

            urlTemplate = uriBuilder.encode().toUriString();
        }

        return urlTemplate;
    }

    public String apiUrl(GitlabApiRoutes route) {
        return this.apiUrl(route, "", null);
    }

}
