package org.opendatamesh.platform.up.executor.gitlabci.resources;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
public class TaskResource  {

    @Schema(description = "Auto generated Task ID")
    private Long id;

    @Schema(description = "ID of the parent Activity of the Task")
    private String activityId;

    @JsonIgnore
    @Schema(description = "Logical name of the target task executor service", example = "azure-devops")
    private String executorRef;

    @Schema(description = "Reference for the callback from the executor service")
    private String callbackRef;

    @Schema(description = "Template of the Task")
    private String template;

    @Schema(description = "Configurations for the Task")
    private String configurations;

    @Schema(description = "Task status")
    private TaskStatus status;

    @Schema(description = "Task results in case of successful execution")
    private String results;

    @Schema(description = "Task results in case of failures")
    private String errors;

    @Schema(description = "Creation timestamp of the Task")
    private Date createdAt;

    @Schema(description = "Timestamp of the start of the Task execution")
    private Date startedAt;

    @Schema(description = "Timestamp of the end of the Task execution")
    private Date finishedAt;
}
