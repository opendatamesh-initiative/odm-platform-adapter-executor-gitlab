package org.opendatamesh.platform.up.executor.gitlabci.resources.exceptions;

import org.opendatamesh.platform.up.executor.gitlabci.resources.client.exceptions.ClientException;

public class GitlabClientException extends ClientException {
    public GitlabClientException(int statusCode, String responseBody) {
        super(statusCode, responseBody);
    }
}
