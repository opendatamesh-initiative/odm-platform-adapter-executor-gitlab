package org.opendatamesh.platform.up.executor.gitlabci.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.opendatamesh.platform.up.executor.gitlabci.resources.ConfigurationResource;
import org.opendatamesh.platform.up.executor.gitlabci.resources.client.GitlabPipelineResource;
import org.opendatamesh.platform.up.executor.gitlabci.resources.TemplateResource;

@Mapper(componentModel = "spring")
public interface GitlabPipelineMapper {

    @Mapping(source = "configuration.params", target = "variables")
    @Mapping(source = "template.branch", target = "ref")
    GitlabPipelineResource toGitlabPipelineResource(ConfigurationResource configuration, TemplateResource template, String callbackRef);
}