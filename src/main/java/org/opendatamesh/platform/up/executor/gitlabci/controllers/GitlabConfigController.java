package org.opendatamesh.platform.up.executor.gitlabci.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.opendatamesh.platform.up.executor.gitlabci.resources.client.gitlab.GitlabConfigResource;
import org.opendatamesh.platform.up.executor.gitlabci.resources.client.params.ParamResource;
import org.opendatamesh.platform.up.executor.gitlabci.services.GitlabConfigService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller to manage the configuration endpoints.
 */
@RestController
@RequestMapping(value = "/config")
@RequiredArgsConstructor
public class GitlabConfigController {
    private final GitlabConfigService gitlabConfigService;

    private final static String EXAMPLE_SUCCESS = "{\n" +
            "  \"id\": 1,\n" +
            "  \"instanceurl\": \"https://gitlab.example.com\",\n" +
            "  \"instanceToken\": \"your-token-here\"\n" +
            "}";

    @Operation(
            summary = "Create new GitLab Instance",
            description = "Register a new Gitlab Instance by passing its URL and Token. The token must have full API access."
    )
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Config created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ParamResource.class),
                            examples = {
                                    @ExampleObject(name = "success", value = EXAMPLE_SUCCESS)
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "[Conflict](https://www.rfc-editor.org/rfc/rfc9110.html#name-409-conflict)"
                            + "\r\n - Error code 40902: A Gitlab Instance with the same URL already exists."
            )
    })
    @PostMapping(
            consumes = {
                    "application/json"
            },
            produces = {
                    "application/json"
            }
    )
    public ParamResource createInstance(@RequestBody GitlabConfigResource configResource) {
        return gitlabConfigService.addConfig(configResource);
    }

    @Operation(
            summary = "Get the list of all configurations.",
            description = "Get the full list of Gitlab configurations (each one containing instance url and token)" +
                    " registered to the config service."
    )
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(
            produces = {
                    "application/json"
            }
    )
    /**
     * @return the list of configurations from the parameter server.
     */
    public List<ParamResource> getInstanceList() {
        return gitlabConfigService.getAllGitlabInstances();
    }

    /**
     * Delete a configuration from the parameter server.
     */
    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    public void deleteInstance(@RequestParam String gitlabInstanceId) {
        gitlabConfigService.deleteGitlabInstance(gitlabInstanceId);
    }
}
