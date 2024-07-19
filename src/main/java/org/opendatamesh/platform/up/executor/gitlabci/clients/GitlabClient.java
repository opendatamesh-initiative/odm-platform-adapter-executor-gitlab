package org.opendatamesh.platform.up.executor.gitlabci.clients;

import org.opendatamesh.platform.core.commons.clients.ODMClient;
import org.opendatamesh.platform.up.executor.gitlabci.resources.client.gitlab.GitlabPipelineResource;
import org.opendatamesh.platform.up.executor.gitlabci.resources.client.gitlab.GitlabRunResource;
import org.opendatamesh.platform.up.executor.gitlabci.utils.ObjectMapperFactory;
import org.springframework.http.*;

/**
 * Service class to connect to the GitLab server instance (either on-premise or self-hosted).
 */
public class GitlabClient extends ODMClient {
    private final String gitlabToken;

    public GitlabClient(String serverAddress, String gitlabToken) {
        super(serverAddress, ObjectMapperFactory.JSON_MAPPER);
        this.gitlabToken = gitlabToken;
    }

    /**
     * Create a new task on GitLab pipeline and run it.
     * @param pipelineResource the object that contains pipeline details.
     * @param projectId the id of the gitlab project.
     * @return the created GitLab pipeline.
     */
    public ResponseEntity<GitlabRunResource> postTask(GitlabPipelineResource pipelineResource, String projectId) {
        HttpEntity<GitlabPipelineResource> entity = new HttpEntity<>(pipelineResource, getHttpHeaders());

        return rest.postForEntity(
                apiUrl(GitlabApiRoutes.GITLAB_PIPELINE_RUN),
                entity,
                GitlabRunResource.class,
                projectId
        );
    }

    /**
     * Check the status of a GitLab pipeline.
     * @param projectId the id of the GitLab project.
     * @param pipelineId the id of the running pipeline.
     * @return the status of the GitLab pipeline.
     */
    public ResponseEntity<GitlabRunResource> readTask(String projectId, String pipelineId) {
        HttpEntity<GitlabPipelineResource> entity = new HttpEntity<>(getHttpHeaders());
        return rest.exchange(
                apiUrl(GitlabApiRoutes.GITLAB_PIPELINE_STATUS),
                HttpMethod.GET,
                entity,
                GitlabRunResource.class,
                projectId,
                pipelineId
        );
    }


    /**
     * Create the http headers object, injecting the bearer token.
     * @return the header objects.
     */
    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(gitlabToken);
        return headers;
    }

}
