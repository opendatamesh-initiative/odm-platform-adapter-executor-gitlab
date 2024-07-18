package org.opendatamesh.platform.up.executor.gitlabci.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface GitlabInstanceRepository extends JpaRepository<GitlabInstance, String>, JpaSpecificationExecutor<GitlabInstance> {
}
