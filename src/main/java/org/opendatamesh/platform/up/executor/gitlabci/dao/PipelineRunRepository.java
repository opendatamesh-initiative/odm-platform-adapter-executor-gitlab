package org.opendatamesh.platform.up.executor.gitlabci.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PipelineRunRepository extends JpaRepository<PipelineRun, Long>, JpaSpecificationExecutor<PipelineRun> {
}
