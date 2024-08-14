package org.opendatamesh.platform.up.executor.gitlabci.dao;

import lombok.Data;
import org.opendatamesh.platform.up.executor.gitlabci.resources.client.gitlab.GitlabRunState;
import org.opendatamesh.platform.up.executor.gitlabci.utils.ListMapConverter;

import javax.persistence.*;
import java.util.List;
import java.util.Map;


@Data
@Entity
@Table(name = "PIPELINE_RUNS")
public class PipelineRun {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "PIPELINE_RUN_ID")
    protected Long pipelineRunId;

    @Column(name = "TASK_ID", unique = true)
    protected Long taskId;

    @Column(name = "RUN_ID")
    protected String runId;

    @Column(name = "PROJECT")
    protected String project;

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    protected GitlabRunState status;

    @Column(name = "VARIABLES")
    @Convert(converter = ListMapConverter.class)
    protected List<Map<String, String>> variables;

    @Column(name = "CREATED_AT")
    protected String createdAt;

    @Column(name = "FINISHED_AT")
    protected String finishedAt;

    @Column(name = "GITLAB_INSTANCE_URL")
    protected String gitlabInstanceUrl;
}
