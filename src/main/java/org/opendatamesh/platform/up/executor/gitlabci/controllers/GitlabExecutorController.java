package org.opendatamesh.platform.up.executor.gitlabci.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.opendatamesh.platform.core.commons.clients.resources.ErrorRes;
import org.opendatamesh.platform.core.commons.servers.exceptions.InternalServerException;
import org.opendatamesh.platform.core.commons.servers.exceptions.UnprocessableEntityException;
import org.opendatamesh.platform.up.executor.gitlabci.resources.ConfigurationResource;
import org.opendatamesh.platform.up.executor.gitlabci.resources.ExecutorApiStandardErrors;
import org.opendatamesh.platform.up.executor.gitlabci.resources.TaskResource;
import org.opendatamesh.platform.up.executor.gitlabci.resources.TemplateResource;
import org.opendatamesh.platform.up.executor.gitlabci.services.GitlabPipelineService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/tasks")
@Validated
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Endpoint associated to connector tasks collection.")
public class GitlabExecutorController {

    private final GitlabPipelineService pipelineService;

    private static final String EXAMPLE_ONE = "{\n" + //
            "    \"callbackRef\": \"my/callback/url\",\n" + //
            "    \"template\": \"{\\\"organization\\\":\\\"andreagioia\\\",\\\"project\\\":\\\"opendatamesh\\\",\\\"pipelineId\\\":\\\"3\\\",\\\"branch\\\":\\\"main\\\"}\",\n" + //
            "    \"configurations\": \"{\\\"stagesToSkip\\\":[]}\"\n" + //
            "}";

    private static final TaskResource EXAMPLE_TWO = new TaskResource();
    static {
        EXAMPLE_TWO.setCallbackRef("my/callback/url");
        EXAMPLE_TWO.setTemplate("\"{\\\"organization\\\":\\\"andreagioia\\\",\\\"project\\\":\\\"opendatamesh\\\",\\\"pipelineId\\\":\\\"3\\\",\\\"branch\\\":\\\"main\\\"}\"");
        EXAMPLE_TWO.setConfigurations("\"{\\\"stagesToSkip\\\":[]}\"");
    }
    // ===============================================================================
    // POST /tasks
    // ===============================================================================
    @Operation(
            summary = "Execute task",
            description = "Execute the provided task"
    )
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Task created and started",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TaskResource.class),
                            examples = {
                                    @ExampleObject(name = "one", value = EXAMPLE_ONE)}
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "[Conflict](https://www.rfc-editor.org/rfc/rfc9110.html#name-409-conflict)"
                            + "\r\n - Error Code 40901 - Task is already started",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorRes.class))}
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "[Unprocessable Content](https://www.rfc-editor.org/rfc/rfc9110.html#name-422-unprocessable-content)"
                            + "\r\n - Error Code 42201 - Task is invalid",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorRes.class))}
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "[Internal Server Error](https://www.rfc-editor.org/rfc/rfc9110.html#name-500-internal-server-error)"
                            + "\r\n - Error Code 50001 - Error in in the backend service"
                            + "\r\n - Error Code 50050 - Azure Devops API or not reachable",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorRes.class))}
            ),
            @ApiResponse(
                    responseCode = "501",
                    description = "[Bad Gateway](https://www.rfc-editor.org/rfc/rfc9110.html#name-502-bad-gateway)"
                            + "\r\n - Error Code 50250 - Azure Devops API returns an error",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorRes.class))}
            )
    })
    @PostMapping(
            consumes = {
                    "application/vnd.odmp.v1+json",
                    "application/vnd.odmp+json",
                    "application/json"},
            produces = {
                    "application/vnd.odmp.v1+json",
                    "application/vnd.odmp+json",
                    "application/json"
            }
    )
    public TaskResource createTaskEndpoint(@RequestBody TaskResource task) {
        return createTask(task);
    }

    public TaskResource createTask(TaskResource task) {

        ObjectMapper objectMapper = new ObjectMapper();
        TemplateResource template;
        ConfigurationResource configuration;

        if(task.getId() == null) {
            throw new UnprocessableEntityException(
                    ExecutorApiStandardErrors.SC422_05_TASK_IS_INVALID,
                    "Task ID is not specified in the task definition."
            );
        }
        if(task.getConfigurations() == null) {
            throw new UnprocessableEntityException(
                    ExecutorApiStandardErrors.SC422_05_TASK_IS_INVALID,
                    "Configuration is missing in the task definition"
            );
        }
        if(task.getTemplate() == null) {
            throw new UnprocessableEntityException(
                    ExecutorApiStandardErrors.SC422_05_TASK_IS_INVALID,
                    "Template is not specified in the task definition."
            );
        }

        try {
            template = objectMapper.readValue(task.getTemplate(), TemplateResource.class);
            configuration = objectMapper.readValue(task.getConfigurations(), ConfigurationResource.class);
        } catch (JsonProcessingException e) {
            throw new InternalServerException(ExecutorApiStandardErrors.SC500_50_EXECUTOR_SERVICE_ERROR,
                    "Task service couldn't read template or configuration information. Please check the format of the object");
        }

        String callbackRef = task.getCallbackRef();
        pipelineService.runPipeline(configuration, template, callbackRef, task.getId());
        return task;
    }
}
