package org.opendatamesh.platform.up.executor.gitlabci.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface PipelineRunRepository extends JpaRepository<PipelineRun, Long>, JpaSpecificationExecutor<PipelineRun> {
    Optional<PipelineRun> findByProjectAndRunId(String project, String runId);
}
