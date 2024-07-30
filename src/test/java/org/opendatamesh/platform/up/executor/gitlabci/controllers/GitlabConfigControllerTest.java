package org.opendatamesh.platform.up.executor.gitlabci.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendatamesh.platform.core.commons.servers.exceptions.ConflictException;
import org.opendatamesh.platform.up.executor.gitlabci.resources.client.gitlab.GitlabConfigResource;
import org.opendatamesh.platform.up.executor.gitlabci.resources.client.params.ParamResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 8004)
public class GitlabConfigControllerTest {

    WireMockServer wireMockServer;
    ParamResource responseParam = new ParamResource();
    ObjectMapper objectMapper = new ObjectMapper();


    @Autowired
    private GitlabConfigController configController;
    private final static String INSTANCE_URL = "INSTANCE_URL";
    private final static String GITLAB_TOKEN = "SECRET_VALUE";

    private static final String clientPrefix = "DEVOPS_GITLAB_";

    @BeforeEach
    void setup() {
        responseParam.setId(Long.parseLong("1"));
        responseParam.setParamValue(GITLAB_TOKEN);
        responseParam.setParamName(INSTANCE_URL);
        responseParam.setDisplayName(clientPrefix + "_TEST");
        responseParam.setSecret(true);
    }

    @Test
    public void testGitlabConfigCreationSuccess() throws JsonProcessingException {
        wireMockServer = new WireMockServer();

        // The configuration does not exist, so throw a Not Found error.
        WireMock.stubFor(get(urlMatching("/api/v1/pp/params/params/filter\\?name=INSTANCE_URL"))
                .willReturn(aResponse().withStatus(404))
        );
        WireMock.stubFor(post(urlMatching("/api/v1/pp/params/params/"))
                .willReturn(
                        aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(objectMapper.writeValueAsString(responseParam))
                )
        );
        wireMockServer.start();

        GitlabConfigResource gitlabConfigResource = new GitlabConfigResource();
        gitlabConfigResource.setInstanceUrl(INSTANCE_URL);
        gitlabConfigResource.setInstanceToken(GITLAB_TOKEN);
        ParamResource response = configController.createInstance(gitlabConfigResource);
        Assertions.assertEquals(INSTANCE_URL, response.getParamName());
        Assertions.assertEquals(GITLAB_TOKEN, response.getParamValue());
        wireMockServer.stop();
    }

    @Test
    public void testGitlabConfigCreationConflict() {
        wireMockServer = new WireMockServer();

        // The configuration does not exist, so throw a Not Found error.
        WireMock.stubFor(get(urlMatching("/api/v1/pp/params/params/filter\\?name=INSTANCE_URL"))
                .willReturn(aResponse().withStatus(200))
        );

        wireMockServer.start();

        GitlabConfigResource gitlabConfigResource = new GitlabConfigResource();
        gitlabConfigResource.setInstanceUrl(INSTANCE_URL);
        gitlabConfigResource.setInstanceToken(GITLAB_TOKEN);
        Assertions.assertThrows(ConflictException.class, () -> configController.createInstance(gitlabConfigResource));
        wireMockServer.stop();
    }

    @Test
    public void testGitlabConfigDeletion() throws JsonProcessingException {
        wireMockServer = new WireMockServer();

        WireMock.stubFor(get(urlMatching("/api/v1/pp/params/params/filter\\?name=INSTANCE_URL"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(responseParam))
                )
        );
        WireMock.stubFor(delete(urlMatching("/api/v1/pp/params/params/INSTANCE_URL"))
                .willReturn(aResponse().withStatus(200))
        );

        wireMockServer.start();

        configController.deleteInstance(INSTANCE_URL);
        wireMockServer.stop();
    }

    @Test
    public void testGitlabConfigRetrieval() throws JsonProcessingException {
        wireMockServer = new WireMockServer();
        ParamResource paramResource2 = new ParamResource();
        paramResource2.setId(Long.parseLong("2"));
        paramResource2.setDisplayName(clientPrefix + "_TEST_1");
        paramResource2.setParamValue(GITLAB_TOKEN + "_2");
        paramResource2.setParamName(INSTANCE_URL + "_2");
        List<ParamResource> paramResources = List.of(responseParam, paramResource2);
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println(objectMapper.writeValueAsString(paramResources));

        WireMock.stubFor(get(urlMatching("/api/v1/pp/params/params"))
                .willReturn(
                        aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(objectMapper.writeValueAsString(paramResources)))
        );

        wireMockServer.start();

        List<ParamResource> resources = configController.getInstanceList();

        Assertions.assertEquals( 2, resources.size());
        Assertions.assertEquals(resources.get(0).getParamName(), responseParam.getParamName());
        Assertions.assertEquals(resources.get(0).getParamValue(), responseParam.getParamValue());
        Assertions.assertEquals(resources.get(1).getParamName(), paramResource2.getParamName());
        Assertions.assertEquals(resources.get(1).getParamValue(), paramResource2.getParamValue());

        wireMockServer.stop();
    }
}
