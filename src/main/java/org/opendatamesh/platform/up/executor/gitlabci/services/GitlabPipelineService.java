package org.opendatamesh.platform.up.executor.gitlabci.services;

import lombok.RequiredArgsConstructor;
import org.opendatamesh.platform.core.commons.servers.exceptions.InternalServerException;
import org.opendatamesh.platform.core.commons.servers.exceptions.NotFoundException;
import org.opendatamesh.platform.core.commons.servers.exceptions.UnprocessableEntityException;
import org.opendatamesh.platform.up.executor.gitlabci.clients.GitlabClient;
import org.opendatamesh.platform.up.executor.gitlabci.clients.ParamsServiceClient;
import org.opendatamesh.platform.up.executor.gitlabci.dao.PipelineRun;
import org.opendatamesh.platform.up.executor.gitlabci.dao.PipelineRunRepository;
import org.opendatamesh.platform.up.executor.gitlabci.mappers.GitlabPipelineMapper;
import org.opendatamesh.platform.up.executor.gitlabci.resources.ConfigurationResource;
import org.opendatamesh.platform.up.executor.gitlabci.resources.ExecutorApiStandardErrors;
import org.opendatamesh.platform.up.executor.gitlabci.resources.TaskStatus;
import org.opendatamesh.platform.up.executor.gitlabci.resources.TemplateResource;
import org.opendatamesh.platform.up.executor.gitlabci.resources.client.gitlab.GitlabPipelineResource;
import org.opendatamesh.platform.up.executor.gitlabci.resources.client.gitlab.GitlabRunResource;
import org.opendatamesh.platform.up.executor.gitlabci.resources.client.gitlab.GitlabRunState;
import org.opendatamesh.platform.up.executor.gitlabci.resources.client.params.ParamResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${polling.retries}")
    private Integer pollingNumRetries;
    @Value("${polling.interval}")
    private Integer pollingIntervalSeconds;
    private final ParamsServiceClient paramsServiceClient;

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
    public GitlabRunResource runPipeline(ConfigurationResource configurationResource,
                                         TemplateResource templateResource,
                                         String callbackRef,
                                         Long taskId,
                                         String gitlabInstanceUrl) {
        GitlabPipelineResource pipelineResource = pipelineMapper.toGitlabPipelineResource(
                configurationResource, templateResource, callbackRef
        );

        if (templateResource.getBranch() == null || templateResource.getProjectId() == null) {
            throw new UnprocessableEntityException(
                    ExecutorApiStandardErrors.SC422_05_TASK_IS_INVALID,
                    "Cannot send pipeline trigger to GitLab. Template parameter missing (project id, branch)."
            );
        }
        logger.info("Calling Gitlab Pipeline API...");

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

        ResponseEntity<GitlabRunResource> gitlabResponse = gitlabClient.postTask(
                pipelineResource,
                templateResource.getProjectId()
        );
        GitlabRunResource gitlabRunResource = gitlabResponse.getBody();

        if (!gitlabResponse.getStatusCode().is2xxSuccessful()) {
            switch (gitlabResponse.getStatusCode()) {
                case UNAUTHORIZED:
                    throw new UnprocessableEntityException(
                            ExecutorApiStandardErrors.SC401_01_EXECUTOR_UNATHORIZED,
                            "Missing credentials: " + gitlabRunResource
                    );
                case FORBIDDEN:
                    throw new UnprocessableEntityException(
                            ExecutorApiStandardErrors.SC403_01_EXECUTOR_FORBIDDEN,
                            "User does not have permission to run the pipeline: " + gitlabRunResource
                    );
                case BAD_REQUEST:
                    throw new UnprocessableEntityException(
                            ExecutorApiStandardErrors.SC422_05_TASK_IS_INVALID,
                            "GitLab responded with a 400 error. Please make sure to have a valid gitlab-ci file and settings."
                    );
                default:
                    throw new InternalServerException(
                            ExecutorApiStandardErrors.SC500_50_EXECUTOR_SERVICE_ERROR,
                            "Gitlab responded with an error: " + gitlabRunResource
                    );
            }
        }
        try {
            PipelineRun pipelineRun = new PipelineRun();
            if(gitlabRunResource!=null) {
                pipelineRun.setTaskId(taskId);
                pipelineRun.setRunId(gitlabRunResource.getId());
                pipelineRun.setProject(templateResource.getProjectId());
                pipelineRun.setStatus(GitlabRunState.valueOf(gitlabRunResource.getStatus()));
                pipelineRun.setCreatedAt(gitlabRunResource.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME));
                pipelineRun.setFinishedAt(
                        gitlabRunResource.getFinishedAt() != null
                                ? gitlabRunResource.getFinishedAt().format(DateTimeFormatter.ISO_DATE_TIME)
                                : null);
                pipelineRun.setVariables(pipelineResource.getVariables());
                pipelineRun.setGitlabInstanceUrl(gitlabInstanceUrl);
                pipelineRunRepository.saveAndFlush(pipelineRun);
            }
        } catch (Exception e) {
            logger.error("Error during the creation of PipelineRun entry: {}", e.getMessage());
        }
        logger.info("Pipeline run triggered successfully");
        return gitlabRunResource;
    }

    /**
     * Poll the GitLab server asking the status of the running pipeline.
     * @param taskId the id of the task to be checked.
     * @return the status of the pipeline.
     */
    @Async
    public CompletableFuture<TaskStatus> getPipelineStatus(Long taskId) {
        Optional<PipelineRun> optionalPipelineRun = pipelineRunRepository.findById(taskId);
        if (optionalPipelineRun.isEmpty()) {
            throw new NotFoundException(ExecutorApiStandardErrors.SC404_01_PIPELINE_RUN_NOT_FOUND,
                    "Pipeline run with id " + taskId + " not found.");
        }
        PipelineRun pipelineRun = optionalPipelineRun.get();

        int counter = 0;
        ResponseEntity<GitlabRunResource> gitlabResponse = null;

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

        while (counter < pollingNumRetries) {
            gitlabResponse = gitlabClient.readTask(pipelineRun.getProject(), pipelineRun.getRunId());
            if (gitlabResponse.getStatusCode().is2xxSuccessful() && Objects.requireNonNull(gitlabResponse.getBody()).getStatus().equals(GitlabRunState.success.toString())) {
                break;
            }
            try {
                Thread.sleep((long) pollingIntervalSeconds);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Polling thread error: " + e.getMessage());
            }
            counter++;
        }
        if(gitlabResponse!=null) {
            GitlabRunResource responseBody = gitlabResponse.getBody();
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
