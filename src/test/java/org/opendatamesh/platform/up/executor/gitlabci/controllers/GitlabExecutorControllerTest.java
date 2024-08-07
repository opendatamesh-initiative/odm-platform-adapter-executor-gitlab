package org.opendatamesh.platform.up.executor.gitlabci.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendatamesh.platform.up.executor.gitlabci.dao.PipelineRun;
import org.opendatamesh.platform.up.executor.gitlabci.dao.PipelineRunRepository;
import org.opendatamesh.platform.up.executor.gitlabci.resources.ConfigurationResource;
import org.opendatamesh.platform.up.executor.gitlabci.resources.TaskResource;
import org.opendatamesh.platform.up.executor.gitlabci.resources.TaskStatus;
import org.opendatamesh.platform.up.executor.gitlabci.resources.TemplateResource;
import org.opendatamesh.platform.up.executor.gitlabci.resources.client.gitlab.GitlabRunResourceResponse;
import org.opendatamesh.platform.up.executor.gitlabci.resources.client.gitlab.GitlabRunState;
import org.opendatamesh.platform.up.executor.gitlabci.resources.client.params.ParamResource;
import org.opendatamesh.platform.up.executor.gitlabci.resources.exceptions.UnprocessableEntityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest
@AutoConfigureWireMock(port = 8004)
public class GitlabExecutorControllerTest {
    WireMockServer wireMockServer;
    ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    ParamResource responseParam = new ParamResource();
    GitlabRunResourceResponse gitlabRunResourceResponse = new GitlabRunResourceResponse();

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

        gitlabRunResourceResponse.setId("1");
        gitlabRunResourceResponse.setIid("1");
        gitlabRunResourceResponse.setProjectId(1000);
        gitlabRunResourceResponse.setStatus(GitlabRunState.success.toString());
        gitlabRunResourceResponse.setCreatedAt(ZonedDateTime.now());
        gitlabRunResourceResponse.setUpdatedAt(ZonedDateTime.now());
        gitlabRunResourceResponse.setFinishedAt(ZonedDateTime.now());
        gitlabRunResourceResponse.setStartedAt(ZonedDateTime.now());
        gitlabRunResourceResponse.setCommittedAt(ZonedDateTime.now());
        GitlabRunResourceResponse.Message msg = new GitlabRunResourceResponse.Message();
        msg.setBase(List.of(""));
        gitlabRunResourceResponse.setMessage(msg);
        GitlabRunResourceResponse.User user = new GitlabRunResourceResponse.User();
        user.setId(1);
        user.setName("first");
        user.setUsername("first");
        user.setAvatarUrl("");
        gitlabRunResourceResponse.setUser(user);
    }

    @Test
    public void testCreateTaskSuccess() throws Exception {
        wireMockServer = new WireMockServer();

        WireMock.stubFor(post(urlMatching("/api/v4/projects/1000/pipeline"))
                .willReturn(
                        aResponse()
                                .withStatus(201)
                                .withHeader("Content-Type", "application/json")
                                .withBody(objectMapper.writeValueAsString(gitlabRunResourceResponse))
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
        List<PipelineRun> runs = pipelineRunRepository.findAll();
        if (pipelineRunRepository.findById(1L).isEmpty()) {
            PipelineRun pipelineRun = new PipelineRun();
            pipelineRun.setPipelineRunId(1L);
            pipelineRun.setRunId("2");
            pipelineRun.setFinishedAt(new Date().toString());
            pipelineRun.setProject("1000");
            pipelineRun.setVariables(List.of());
            pipelineRun.setTaskId(2L);
            pipelineRun.setStatus(GitlabRunState.success);
            pipelineRun.setGitlabInstanceUrl(INSTANCE_URL);
            pipelineRun.setCreatedAt(new Date().toString());
            pipelineRunRepository.saveAndFlush(pipelineRun);
        }
    }

    @Test
    public void testReadTaskSuccess() throws JsonProcessingException, ExecutionException, InterruptedException {
        wireMockServer = new WireMockServer();

        WireMock.stubFor(get(urlMatching("/api/v4/projects/1000/pipelines/1"))
                .willReturn(
                        aResponse()
                                .withStatus(201)
                                .withHeader("Content-Type", "application/json")
                                .withBody(objectMapper.writeValueAsString(gitlabRunResourceResponse))
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
        Assertions.assertThrows(ExecutionException.class, () -> executorController.readTaskStatus(4L));
    }

    @Test
    public void testReadTask400ErrorServer() throws JsonProcessingException {
        wireMockServer = new WireMockServer();

        WireMock.stubFor(get(urlMatching("/api/v4/projects/1000/pipelines/1"))
                .willReturn(
                        aResponse()
                                .withStatus(400)
                                .withHeader("Content-Type", "application/json")
                                .withBody(objectMapper.writeValueAsString(gitlabRunResourceResponse))
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

        Assertions.assertThrows(ExecutionException.class, () -> executorController.readTaskStatus(1L));
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
                                .withBody(objectMapper.writeValueAsString(gitlabRunResourceResponse))
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

        Assertions.assertThrows(ExecutionException.class, () -> executorController.readTaskStatus(1L));
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
                                .withBody(objectMapper.writeValueAsString(gitlabRunResourceResponse))
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

        Assertions.assertThrows(ExecutionException.class, () -> executorController.readTaskStatus(1L));
        wireMockServer.stop();
    }
}
