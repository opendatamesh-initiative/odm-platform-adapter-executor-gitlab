package org.opendatamesh.platform.up.executor.gitlabci.services;

import lombok.RequiredArgsConstructor;
import org.opendatamesh.platform.up.executor.gitlabci.clients.GitlabClient;
import org.opendatamesh.platform.up.executor.gitlabci.clients.ParamsServiceClient;
import org.opendatamesh.platform.up.executor.gitlabci.config.PipelineConfiguration;
import org.opendatamesh.platform.up.executor.gitlabci.dao.PipelineRun;
import org.opendatamesh.platform.up.executor.gitlabci.dao.PipelineRunRepository;
import org.opendatamesh.platform.up.executor.gitlabci.mappers.GitlabPipelineMapper;
import org.opendatamesh.platform.up.executor.gitlabci.resources.ConfigurationResource;
import org.opendatamesh.platform.up.executor.gitlabci.resources.ExecutorApiStandardErrors;
import org.opendatamesh.platform.up.executor.gitlabci.resources.TaskStatus;
import org.opendatamesh.platform.up.executor.gitlabci.resources.TemplateResource;
import org.opendatamesh.platform.up.executor.gitlabci.resources.client.gitlab.GitlabPipelineResource;
import org.opendatamesh.platform.up.executor.gitlabci.resources.client.gitlab.GitlabRunResourceResponse;
import org.opendatamesh.platform.up.executor.gitlabci.resources.client.gitlab.GitlabRunState;
import org.opendatamesh.platform.up.executor.gitlabci.resources.client.params.ParamResource;
import org.opendatamesh.platform.up.executor.gitlabci.resources.exceptions.InternalServerException;
import org.opendatamesh.platform.up.executor.gitlabci.resources.exceptions.NotFoundException;
import org.opendatamesh.platform.up.executor.gitlabci.resources.exceptions.UnprocessableEntityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;


/**
 * Pipeline service for GitLab pipeline orchestration.
 */
@Service
@RequiredArgsConstructor
public class GitlabPipelineService {
    private final GitlabPipelineMapper pipelineMapper;
    private final PipelineRunRepository pipelineRunRepository;
    private final ParamsServiceClient paramsServiceClient;
    private final PipelineConfiguration pipelineConfiguration;

    private static final Logger logger = LoggerFactory.getLogger(GitlabPipelineService.class);

    /**
     * Run the GitLab pipeline.
     * @param configurationResource the configuration for the task.
     * @param templateResource the template resource given in the data product descriptor.
     * @param callbackRef the callbck reference.
     * @param taskId the id of the task to be executed.
     * @param gitlabInstanceUrl the instance url of the GitLab server.
     * @return the created and running pipeline.
     */
    public GitlabRunResourceResponse runPipeline(ConfigurationResource configurationResource,
                                                 TemplateResource templateResource,
                                                 String callbackRef,
                                                 Long taskId,
                                                 String gitlabInstanceUrl) throws UnprocessableEntityException {
        GitlabPipelineResource pipelineResource = pipelineMapper.toGitlabPipelineResource(
                configurationResource, templateResource, callbackRef, taskId
        );

        if (templateResource.getBranch() == null || templateResource.getProjectId() == null) {
            throw new UnprocessableEntityException(
                    ExecutorApiStandardErrors.SC422_05_TASK_IS_INVALID,
                    "Cannot send pipeline trigger to GitLab. Template parameter missing (project id, branch)."
            );
        }

        ResponseEntity<ParamResource> optGitlabInstance = paramsServiceClient.getParamByName(gitlabInstanceUrl);
        if (optGitlabInstance.getStatusCode().is4xxClientError()) {
            throw new UnprocessableEntityException(
                    ExecutorApiStandardErrors.SC404_01_PIPELINE_RUN_NOT_FOUND,
                    "Cannot find Gitlab instance with url: " + gitlabInstanceUrl
            );
        }

        GitlabClient gitlabClient = new GitlabClient(
                Objects.requireNonNull(optGitlabInstance.getBody()).getParamName(),
                Objects.requireNonNull(optGitlabInstance.getBody()).getParamValue()
        );

        ResponseEntity<GitlabRunResourceResponse> gitlabResponse = gitlabClient.postTask(
                pipelineResource,
                templateResource.getProjectId()
        );
        GitlabRunResourceResponse gitlabRunResourceResponse = gitlabResponse.getBody();

        processGitlabResponseStatus(gitlabResponse, gitlabRunResourceResponse);

        savePipelineRun(templateResource, taskId, gitlabInstanceUrl, gitlabRunResourceResponse, pipelineResource);
        return gitlabRunResourceResponse;
    }

    /**
     * Prepare and save the pipeline run entry.
     * @param templateResource
     * @param taskId the id of the devops task.
     * @param gitlabInstanceUrl the url of the GitLab instance.
     * @param gitlabRunResourceResponse the response from the GitLab server.
     * @param pipelineResource the pipeline request resource.
     */
    private void savePipelineRun(TemplateResource templateResource, Long taskId, String gitlabInstanceUrl, GitlabRunResourceResponse gitlabRunResourceResponse, GitlabPipelineResource pipelineResource) {
        try {
            PipelineRun pipelineRun = new PipelineRun();
            if(gitlabRunResourceResponse !=null) {
                createPipelineRunEntry(templateResource, taskId, gitlabInstanceUrl, pipelineRun, gitlabRunResourceResponse, pipelineResource);
                pipelineRunRepository.saveAndFlush(pipelineRun);
                logger.info("Pipeline run triggered successfully");
            }
        } catch (Exception e) {
            logger.error("Error during the creation of PipelineRun entry: {}", e.getMessage());
        }
    }

    /**
     * Create a pipeline run instance in the database.
     * @param templateResource
     * @param taskId the id of the devops task.
     * @param gitlabInstanceUrl the url of the GitLab instance.
     * @param pipelineRun the db reference.
     * @param gitlabRunResourceResponse the response from the GitLab server.
     * @param pipelineResource the pipeline request resource.
     */
    private static void createPipelineRunEntry(TemplateResource templateResource, Long taskId, String gitlabInstanceUrl, PipelineRun pipelineRun, GitlabRunResourceResponse gitlabRunResourceResponse, GitlabPipelineResource pipelineResource) {
        pipelineRun.setTaskId(taskId);
        pipelineRun.setRunId(gitlabRunResourceResponse.getId());
        pipelineRun.setProject(templateResource.getProjectId());
        pipelineRun.setStatus(GitlabRunState.valueOf(gitlabRunResourceResponse.getStatus()));
        pipelineRun.setCreatedAt(gitlabRunResourceResponse.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME));
        pipelineRun.setFinishedAt(
                gitlabRunResourceResponse.getFinishedAt() != null
                        ? gitlabRunResourceResponse.getFinishedAt().format(DateTimeFormatter.ISO_DATE_TIME)
                        : null);
        pipelineRun.setVariables(pipelineResource.getVariables());
        pipelineRun.setGitlabInstanceUrl(gitlabInstanceUrl);
    }

    /**
     * Process response coming from GitLab server, raise an exception if needed.
     * @param gitlabResponse the response from the server.
     * @param gitlabRunResourceResponse the body of the response.
     */
    private static void processGitlabResponseStatus(ResponseEntity<GitlabRunResourceResponse> gitlabResponse, GitlabRunResourceResponse gitlabRunResourceResponse) {
        if (!gitlabResponse.getStatusCode().is2xxSuccessful()) {
            switch (gitlabResponse.getStatusCode()) {
                case UNAUTHORIZED:
                    throw new UnprocessableEntityException(
                            ExecutorApiStandardErrors.SC401_01_EXECUTOR_UNATHORIZED,
                            "Missing credentials: " + gitlabRunResourceResponse
                    );
                case FORBIDDEN:
                    throw new UnprocessableEntityException(
                            ExecutorApiStandardErrors.SC403_01_EXECUTOR_FORBIDDEN,
                            "User does not have permission to run the pipeline: " + gitlabRunResourceResponse
                    );
                case BAD_REQUEST:
                    throw new UnprocessableEntityException(
                            ExecutorApiStandardErrors.SC422_05_TASK_IS_INVALID,
                            "GitLab responded with a 400 error. Please make sure to have a valid gitlab-ci file and settings."
                    );
                default:
                    throw new InternalServerException(
                            ExecutorApiStandardErrors.SC500_50_EXECUTOR_SERVICE_ERROR,
                            "Gitlab responded with an error: " + gitlabRunResourceResponse
                    );
            }
        }
    }

    /**
     * Poll the GitLab server asking the status of the running pipeline.
     * @param taskId the id of the task to be checked.
     * @return the status of the pipeline.
     */
    @Async
    public CompletableFuture<TaskStatus> getPipelineStatus(Long taskId) {
        Optional<PipelineRun> optionalPipelineRun = pipelineRunRepository.findByTaskId(taskId);
        if (optionalPipelineRun.isEmpty()) {
            throw new NotFoundException(ExecutorApiStandardErrors.SC404_01_PIPELINE_RUN_NOT_FOUND,
                    "Pipeline run with id " + taskId + " not found.");
        }
        PipelineRun pipelineRun = optionalPipelineRun.get();

        int counter = 0;
        ResponseEntity<GitlabRunResourceResponse> gitlabResponse = null;

        ResponseEntity<ParamResource> optGitlabInstance = paramsServiceClient.getParamByName(pipelineRun.getGitlabInstanceUrl());
        if (optGitlabInstance.getStatusCode().is4xxClientError()) {
            throw new UnprocessableEntityException(
                    ExecutorApiStandardErrors.SC404_01_PIPELINE_RUN_NOT_FOUND,
                    "Cannot find Gitlab instance with url: " + pipelineRun.getGitlabInstanceUrl()
            );
        }
        GitlabClient gitlabClient = new GitlabClient(
                Objects.requireNonNull(optGitlabInstance.getBody()).getParamName(),
                Objects.requireNonNull(optGitlabInstance.getBody()).getParamValue()
        );

        while (counter < pipelineConfiguration.getPollingNumRetries()) {
            gitlabResponse = gitlabClient.readTask(pipelineRun.getProject(), pipelineRun.getRunId());
            if (gitlabResponse.getStatusCode().is2xxSuccessful() && Objects.requireNonNull(gitlabResponse.getBody()).getStatus().equals(GitlabRunState.success.toString())) {
                break;
            }
            try {
                Thread.sleep((long) pipelineConfiguration.getPollingIntervalSeconds());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Polling thread error: " + e.getMessage());
                throw new InternalServerException("Polling thread error: " + e.getMessage());
            }
            counter++;
        }
        if(gitlabResponse!=null) {
            GitlabRunResourceResponse responseBody = gitlabResponse.getBody();
            if (!gitlabResponse.getStatusCode().is2xxSuccessful()) {
                switch (gitlabResponse.getStatusCode()) {
                    case UNAUTHORIZED:
                        throw new InternalServerException(
                                ExecutorApiStandardErrors.SC401_01_EXECUTOR_UNATHORIZED,
                                "Missing credentials - " + gitlabResponse
                        );
                    case FORBIDDEN:
                        throw new InternalServerException(
                                ExecutorApiStandardErrors.SC403_01_EXECUTOR_FORBIDDEN,
                                "User does not have the permission to get the run infos - " + gitlabResponse
                        );
                    default:
                        throw new InternalServerException(
                                ExecutorApiStandardErrors.SC500_50_EXECUTOR_SERVICE_ERROR,
                                "Azure DevOps responded with an error: " + gitlabResponse
                        );
                }
            }
            else if(responseBody==null) {
                throw new InternalServerException(
                        ExecutorApiStandardErrors.SC401_01_EXECUTOR_UNATHORIZED,
                        "Response body is null!"
                );
            }
            pipelineRun.setStatus(GitlabRunState.valueOf(responseBody.getStatus()));
            pipelineRun.setFinishedAt(responseBody.getFinishedAt().format(DateTimeFormatter.ISO_DATE_TIME));

            pipelineRunRepository.saveAndFlush(pipelineRun);
        }

        switch (pipelineRun.getStatus()) {
            case success:
                return CompletableFuture.completedFuture(TaskStatus.PROCESSED);
            case failed:
                return CompletableFuture.completedFuture(TaskStatus.FAILED);
            case canceled:
                return CompletableFuture.completedFuture(TaskStatus.ABORTED);
            default:
                return CompletableFuture.completedFuture(null);
        }
    }
}
