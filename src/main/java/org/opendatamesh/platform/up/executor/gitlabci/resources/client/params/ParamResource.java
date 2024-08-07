package org.opendatamesh.platform.up.executor.gitlabci.resources.client.params;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
public class ParamResource {

    @Schema(description = "Auto generated Param ID")
    private Long id;

    @Schema(description = "Name of the parameter (e.g., 'server.port')", required = true)
    private String paramName;

    @Schema(description = "Value for the parameter", required = true)
    private String paramValue;

    @Schema(description = "Human-readable name of the parameter")
    private String displayName;

    @Schema(description = "Description of the parameter")
    private String description;

    @Schema(description = "Whether the value of the parameter is a secret or not", defaultValue = "false")
    private Boolean secret;

    @Schema(description = "Timestamp of the Param creation")
    private Date createdAt;

    @Schema(description = "Timestamp of the last Param update")
    private Date updatedAt;

}