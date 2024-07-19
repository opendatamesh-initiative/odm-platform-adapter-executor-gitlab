package org.opendatamesh.platform.up.executor.gitlabci.resources.client.gitlab;

public enum GitlabRunState {
    created,
    waiting_for_resource,
    preparing,
    pending,
    running,
    success,
    failed,
    canceled,
    skipped,
    manual,
    scheduled
}
