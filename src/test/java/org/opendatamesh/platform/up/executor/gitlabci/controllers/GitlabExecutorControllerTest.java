package org.opendatamesh.platform.up.executor.gitlabci.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendatamesh.platform.core.commons.servers.exceptions.NotFoundException;
import org.opendatamesh.platform.core.commons.servers.exceptions.UnprocessableEntityException;
import org.opendatamesh.platform.up.executor.gitlabci.dao.PipelineRun;
import org.opendatamesh.platform.up.executor.gitlabci.dao.PipelineRunRepository;
import org.opendatamesh.platform.up.executor.gitlabci.resources.ConfigurationResource;
import org.opendatamesh.platform.up.executor.gitlabci.resources.TaskResource;
import org.opendatamesh.platform.up.executor.gitlabci.resources.TaskStatus;
import org.opendatamesh.platform.up.executor.gitlabci.resources.TemplateResource;
import org.opendatamesh.platform.up.executor.gitlabci.resources.client.gitlab.GitlabRunResource;
import org.opendatamesh.platform.up.executor.gitlabci.resources.client.gitlab.GitlabRunState;
import org.opendatamesh.platform.up.executor.gitlabci.resources.client.params.ParamResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest
@AutoConfigureWireMock(port = 8004)
public class GitlabExecutorControllerTest {
    WireMockServer wireMockServer;
    ObjectMapper objectMapper = new ObjectMapper();
    ParamResource responseParam = new ParamResource();
    GitlabRunResource gitlabRunResource = new GitlabRunResource();

    @Autowired
    private GitlabExecutorController executorController;
    @Autowired
    private PipelineRunRepository pipelineRunRepository;
    private final static String INSTANCE_URL = "http://localhost:8004";
    private final static String GITLAB_TOKEN = "SECRET_VALUE";
    private final String UUIDCode = UUID.randomUUID().toString();

    @BeforeEach
    void setup() {
        objectMapper.registerModule(new JavaTimeModule());
        responseParam.setId(Long.parseLong("1"));
        responseParam.setDisplayName(INSTANCE_URL);
        responseParam.setParamName(INSTANCE_URL);
        responseParam.setParamValue(GITLAB_TOKEN);

        gitlabRunResource.setId("1");
        gitlabRunResource.setIid("1");
        gitlabRunResource.setProjectId(1000);
        gitlabRunResource.setStatus(GitlabRunState.success.toString());
        gitlabRunResource.setCreatedAt(ZonedDateTime.now());
        gitlabRunResource.setUpdatedAt(ZonedDateTime.now());
        gitlabRunResource.setFinishedAt(ZonedDateTime.now());
        gitlabRunResource.setStartedAt(ZonedDateTime.now());
        gitlabRunResource.setCommittedAt(ZonedDateTime.now());
        GitlabRunResource.Message msg = new GitlabRunResource.Message();
        msg.setBase(List.of(""));
        gitlabRunResource.setMessage(msg);
        GitlabRunResource.User user = new GitlabRunResource.User();
        user.setId(1);
        user.setName("first");
        user.setUsername("first");
        user.setAvatarUrl("");
        gitlabRunResource.setUser(user);
    }

    @Test
    public void testCreateTaskSuccess() throws Exception {
        wireMockServer = new WireMockServer();

        WireMock.stubFor(post(urlMatching("/api/v4/projects/1000/pipeline"))
                .willReturn(
                        aResponse()
                                .withStatus(201)
                                .withHeader("Content-Type", "application/json")
                                .withBody(objectMapper.writeValueAsString(gitlabRunResource))
                ))
        ;

        WireMock.stubFor(get(urlMatching("/api/v1/pp/params/params/filter\\?name=.*"))
                .willReturn(
                        aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(objectMapper.writeValueAsString(responseParam))
                )
        );
        wireMockServer.start();
        TaskResource requestTask = new TaskResource();
        requestTask.setId(Long.parseLong("1"));
        requestTask.setStatus(TaskStatus.PLANNED);
        requestTask.setActivityId(UUIDCode);
        ConfigurationResource configurationResource = new ConfigurationResource();
        configurationResource.setParams(Map.of(
                "gitlabInstanceUrl", INSTANCE_URL
        ));
        TemplateResource templateResource = new TemplateResource();
        templateResource.setProjectId("1000");
        templateResource.setBranch("master");
        requestTask.setConfigurations(objectMapper.writeValueAsString(configurationResource));
        requestTask.setTemplate(objectMapper.writeValueAsString(templateResource));

        TaskResource response = executorController.createTaskEndpoint(requestTask);

        Assertions.assertEquals(Long.parseLong("1"), response.getId());
        Assertions.assertEquals(TaskStatus.PLANNED, response.getStatus());
        Assertions.assertEquals(UUIDCode, response.getActivityId());
        wireMockServer.stop();
    }

    @Test
    public void createTaskWithoutTaskId() throws Exception {
        TaskResource requestTask = new TaskResource();
        requestTask.setStatus(TaskStatus.PLANNED);
        requestTask.setActivityId(UUIDCode);
        ConfigurationResource configurationResource = new ConfigurationResource();
        configurationResource.setParams(Map.of(
                "gitlabInstanceUrl", INSTANCE_URL
        ));
        TemplateResource templateResource = new TemplateResource();
        templateResource.setProjectId("1000");
        templateResource.setBranch("master");
        requestTask.setConfigurations(objectMapper.writeValueAsString(configurationResource));
        requestTask.setTemplate(objectMapper.writeValueAsString(templateResource));

        Assertions.assertThrows(UnprocessableEntityException.class, () -> executorController.createTaskEndpoint(requestTask));
    }

    @Test
    public void createTaskWithoutConfigurations() throws Exception {
        TaskResource requestTask = new TaskResource();
        requestTask.setId(Long.parseLong("1"));
        requestTask.setStatus(TaskStatus.PLANNED);
        requestTask.setActivityId(UUIDCode);
        TemplateResource templateResource = new TemplateResource();
        templateResource.setProjectId("1000");
        templateResource.setBranch("master");
        requestTask.setTemplate(objectMapper.writeValueAsString(templateResource));

        Assertions.assertThrows(UnprocessableEntityException.class, () -> executorController.createTaskEndpoint(requestTask));
    }

    @Test
    public void createTaskWithoutTemplate() throws Exception {
        TaskResource requestTask = new TaskResource();
        requestTask.setId(Long.parseLong("1"));
        requestTask.setStatus(TaskStatus.PLANNED);
        requestTask.setActivityId(UUIDCode);
        ConfigurationResource configurationResource = new ConfigurationResource();
        configurationResource.setParams(Map.of(
                "gitlabInstanceUrl", INSTANCE_URL
        ));
        requestTask.setConfigurations(objectMapper.writeValueAsString(configurationResource));

        Assertions.assertThrows(UnprocessableEntityException.class, () -> executorController.createTaskEndpoint(requestTask));
    }

    @Test
    public void createTaskWithoutGitlabInstanceId() throws Exception {
        TaskResource requestTask = new TaskResource();
        requestTask.setId(Long.parseLong("1"));
        requestTask.setStatus(TaskStatus.PLANNED);
        requestTask.setActivityId(UUIDCode);

        ConfigurationResource configurationResource = new ConfigurationResource();
        configurationResource.setParams(Map.of());
        requestTask.setConfigurations(objectMapper.writeValueAsString(configurationResource));

        TemplateResource templateResource = new TemplateResource();
        templateResource.setProjectId("1000");
        templateResource.setBranch("master");
        requestTask.setTemplate(objectMapper.writeValueAsString(templateResource));

        Assertions.assertThrows(UnprocessableEntityException.class, () -> executorController.createTaskEndpoint(requestTask));
    }

    @Test
    public void createTask401() throws Exception {
        wireMockServer = new WireMockServer();
        WireMock.stubFor(post(urlMatching("/api/v4/projects/1000/pipeline"))
                .willReturn(
                        aResponse()
                                .withStatus(401)
                ))
        ;

        WireMock.stubFor(get(urlMatching("/api/v1/pp/params/params/filter\\?name=.*"))
                .willReturn(
                        aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(objectMapper.writeValueAsString(responseParam))
                )
        );

        wireMockServer.start();
        TaskResource requestTask = new TaskResource();
        requestTask.setId(Long.parseLong("1"));
        requestTask.setStatus(TaskStatus.PLANNED);
        requestTask.setActivityId(UUIDCode);
        ConfigurationResource configurationResource = new ConfigurationResource();
        configurationResource.setParams(Map.of(
                "gitlabInstanceUrl", INSTANCE_URL
        ));
        TemplateResource templateResource = new TemplateResource();
        templateResource.setProjectId("1000");
        templateResource.setBranch("master");
        requestTask.setConfigurations(objectMapper.writeValueAsString(configurationResource));
        requestTask.setTemplate(objectMapper.writeValueAsString(templateResource));

        Assertions.assertThrows(UnprocessableEntityException.class, () -> executorController.createTaskEndpoint(requestTask));

        wireMockServer.stop();
    }

    @Test
    public void createTask403() throws Exception {
        wireMockServer = new WireMockServer();
        WireMock.stubFor(post(urlMatching("/api/v4/projects/1000/pipeline"))
                .willReturn(
                        aResponse()
                                .withStatus(403)
                ))
        ;

        WireMock.stubFor(get(urlMatching("/api/v1/pp/params/params/filter\\?name=.*"))
                .willReturn(
                        aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(objectMapper.writeValueAsString(responseParam))
                )
        );

        wireMockServer.start();
        TaskResource requestTask = new TaskResource();
        requestTask.setId(Long.parseLong("1"));
        requestTask.setStatus(TaskStatus.PLANNED);
        requestTask.setActivityId(UUIDCode);
        ConfigurationResource configurationResource = new ConfigurationResource();
        configurationResource.setParams(Map.of(
                "gitlabInstanceUrl", INSTANCE_URL
        ));
        TemplateResource templateResource = new TemplateResource();
        templateResource.setProjectId("1000");
        templateResource.setBranch("master");
        requestTask.setConfigurations(objectMapper.writeValueAsString(configurationResource));
        requestTask.setTemplate(objectMapper.writeValueAsString(templateResource));

        Assertions.assertThrows(UnprocessableEntityException.class, () -> executorController.createTaskEndpoint(requestTask));

        wireMockServer.stop();
    }

    @Test
    public void createTask400() throws Exception {
        wireMockServer = new WireMockServer();

        WireMock.stubFor(post(urlMatching("/api/v4/projects/1000/pipeline"))
                .willReturn(
                        aResponse()
                                .withStatus(400)
                ))
        ;

        WireMock.stubFor(get(urlMatching("/api/v1/pp/params/params/filter\\?name=.*"))
                .willReturn(
                        aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(objectMapper.writeValueAsString(responseParam))
                )
        );

        wireMockServer.start();
        TaskResource requestTask = new TaskResource();
        requestTask.setId(Long.parseLong("1"));
        requestTask.setStatus(TaskStatus.PLANNED);
        requestTask.setActivityId(UUIDCode);
        ConfigurationResource configurationResource = new ConfigurationResource();
        configurationResource.setParams(Map.of(
                "gitlabInstanceUrl", INSTANCE_URL
        ));
        TemplateResource templateResource = new TemplateResource();
        templateResource.setProjectId("1000");
        templateResource.setBranch("master");
        requestTask.setConfigurations(objectMapper.writeValueAsString(configurationResource));
        requestTask.setTemplate(objectMapper.writeValueAsString(templateResource));

        Assertions.assertThrows(UnprocessableEntityException.class, () -> executorController.createTaskEndpoint(requestTask));

        wireMockServer.stop();
    }

    @BeforeEach
    public void populateDb() {
        PipelineRun pipelineRun = new PipelineRun();
        pipelineRun.setRunId("1");
        pipelineRun.setFinishedAt(new Date().toString());
        pipelineRun.setProject("1000");
        pipelineRun.setVariables(List.of());
        pipelineRun.setTaskId(1L);
        pipelineRun.setStatus(GitlabRunState.success);
        pipelineRun.setGitlabInstanceUrl(INSTANCE_URL);
        pipelineRun.setCreatedAt(new Date().toString());
        pipelineRunRepository.saveAndFlush(pipelineRun);
    }

    @Test
    public void testReadTaskSuccess() throws JsonProcessingException {
        wireMockServer = new WireMockServer();

        WireMock.stubFor(get(urlMatching("/api/v4/projects/1000/pipelines/1"))
                .willReturn(
                        aResponse()
                                .withStatus(201)
                                .withHeader("Content-Type", "application/json")
                                .withBody(objectMapper.writeValueAsString(gitlabRunResource))
                ))
        ;

        WireMock.stubFor(get(urlMatching("/api/v1/pp/params/params/filter\\?name=.*"))
                .willReturn(
                        aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(objectMapper.writeValueAsString(responseParam))
                )
        );

        TaskStatus status = executorController.readTaskStatus(1L);
        Assertions.assertEquals(TaskStatus.PROCESSED, status);
        wireMockServer.stop();
    }

    @Test
    public void testReadTaskNotExistingPipeline() {
        Assertions.assertThrows(RuntimeException.class, () -> executorController.readTaskStatus(4L));
    }

    @Test
    public void testReadTask400ErrorServer() throws JsonProcessingException {
        wireMockServer = new WireMockServer();

        WireMock.stubFor(get(urlMatching("/api/v4/projects/1000/pipelines/1"))
                .willReturn(
                        aResponse()
                                .withStatus(400)
                                .withHeader("Content-Type", "application/json")
                                .withBody(objectMapper.writeValueAsString(gitlabRunResource))
                ))
        ;

        WireMock.stubFor(get(urlMatching("/api/v1/pp/params/params/filter\\?name=.*"))
                .willReturn(
                        aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(objectMapper.writeValueAsString(responseParam))
                )
        );

        Assertions.assertThrows(RuntimeException.class, () -> executorController.readTaskStatus(1L));
        wireMockServer.stop();

    }

    @Test
    public void testReadTask401ErrorServer() throws JsonProcessingException {
        wireMockServer = new WireMockServer();

        WireMock.stubFor(get(urlMatching("/api/v4/projects/1000/pipelines/1"))
                .willReturn(
                        aResponse()
                                .withStatus(401)
                                .withHeader("Content-Type", "application/json")
                                .withBody(objectMapper.writeValueAsString(gitlabRunResource))
                ))
        ;

        WireMock.stubFor(get(urlMatching("/api/v1/pp/params/params/filter\\?name=.*"))
                .willReturn(
                        aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(objectMapper.writeValueAsString(responseParam))
                )
        );

        Assertions.assertThrows(RuntimeException.class, () -> executorController.readTaskStatus(1L));
        wireMockServer.stop();
    }

    @Test
    public void testReadTask403ErrorServer() throws JsonProcessingException {
        wireMockServer = new WireMockServer();

        WireMock.stubFor(get(urlMatching("/api/v4/projects/1000/pipelines/1"))
                .willReturn(
                        aResponse()
                                .withStatus(403)
                                .withHeader("Content-Type", "application/json")
                                .withBody(objectMapper.writeValueAsString(gitlabRunResource))
                ))
        ;

        WireMock.stubFor(get(urlMatching("/api/v1/pp/params/params/filter\\?name=.*"))
                .willReturn(
                        aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(objectMapper.writeValueAsString(responseParam))
                )
        );

        Assertions.assertThrows(RuntimeException.class, () -> executorController.readTaskStatus(1L));
        wireMockServer.stop();
    }
}
