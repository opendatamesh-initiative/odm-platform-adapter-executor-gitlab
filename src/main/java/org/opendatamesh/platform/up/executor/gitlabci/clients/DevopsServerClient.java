package org.opendatamesh.platform.up.executor.gitlabci.clients;

import org.opendatamesh.platform.core.commons.clients.ODMClient;
import org.opendatamesh.platform.up.executor.gitlabci.utils.ObjectMapperFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DevopsServerClient extends ODMClient {

    public DevopsServerClient(@Value("${odm.productplane.devopsservice.address}") String serverAddress) {
        super(serverAddress, ObjectMapperFactory.JSON_MAPPER);
    }

    public void stopTask(Long taskId) {
        String httpUrl = apiUrl(DevopsServerApiRoutes.STOP_TASK);
        rest.exchange(
                httpUrl,
                HttpMethod.PATCH,
                new HttpEntity<>(null),
                Map.class,
                taskId
        );
    }
}
