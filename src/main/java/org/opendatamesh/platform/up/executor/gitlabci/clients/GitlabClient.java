package org.opendatamesh.platform.up.executor.gitlabci.clients;

import org.opendatamesh.platform.core.commons.clients.ODMClient;
import org.opendatamesh.platform.up.executor.gitlabci.resources.client.GitlabPipelineResource;
import org.opendatamesh.platform.up.executor.gitlabci.resources.client.GitlabRunResource;
import org.opendatamesh.platform.up.executor.gitlabci.utils.ObjectMapperFactory;
import org.springframework.http.*;

public class GitlabClient extends ODMClient {
    private final String gitlabToken;

    public GitlabClient(String serverAddress, String gitlabToken) {
        super(serverAddress, ObjectMapperFactory.JSON_MAPPER);
        this.gitlabToken = gitlabToken;
    }

    public ResponseEntity<GitlabRunResource> postTask(GitlabPipelineResource pipelineResource, String projectId) {
        HttpEntity<GitlabPipelineResource> entity = new HttpEntity<>(pipelineResource, getHttpHeaders());

        return rest.postForEntity(
                apiUrl(GitlabApiRoutes.GITLAB_PIPELINE_RUN),
                entity,
                GitlabRunResource.class,
                projectId
        );
    }

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


    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(gitlabToken);
        return headers;
    }

}
