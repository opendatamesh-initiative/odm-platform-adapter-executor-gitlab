package org.opendatamesh.platform.up.executor.gitlabci.resources.exceptions;

public interface ODMApiStandardErrors {
    String code();
    String description();

    static ODMApiStandardErrors getNotFoundError(String className) {
        return null;
    }
}
