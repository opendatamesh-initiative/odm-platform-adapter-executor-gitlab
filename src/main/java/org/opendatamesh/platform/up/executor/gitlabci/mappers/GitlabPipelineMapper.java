package org.opendatamesh.platform.up.executor.gitlabci.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.opendatamesh.platform.up.executor.gitlabci.resources.ConfigurationResource;
import org.opendatamesh.platform.up.executor.gitlabci.resources.client.gitlab.GitlabPipelineResource;
import org.opendatamesh.platform.up.executor.gitlabci.resources.TemplateResource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface GitlabPipelineMapper {

    /**
     *
     * @param configuration
     * @param template
     * @param callbackRef
     * @return
     */
    @Mapping(source = "configuration.params", target = "variables")
    @Mapping(source = "template.branch", target = "ref")
    default GitlabPipelineResource toGitlabPipelineResource(ConfigurationResource configuration, TemplateResource template, String callbackRef, Long taskId) {
        GitlabPipelineResource gitlabPipelineResource = new GitlabPipelineResource();
        gitlabPipelineResource.setRef(template.getBranch());
        List<Map<String, String>> variables = new ArrayList<>();
        for (Map.Entry<String, String> entry : configuration.getParams().entrySet()) {
            variables.add(Map.of("key", entry.getKey(), "value", entry.getValue()));
        }
        variables.add(Map.of("key", "taskId", "value", taskId.toString()));
        gitlabPipelineResource.setVariables(variables);
        return gitlabPipelineResource;
    }
}