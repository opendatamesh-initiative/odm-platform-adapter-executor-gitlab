package org.opendatamesh.platform.up.executor.gitlabci.dao;

import lombok.Data;
import org.opendatamesh.platform.up.executor.gitlabci.resources.client.GitlabRunState;
import org.opendatamesh.platform.up.executor.gitlabci.resources.client.GitlabVariable;
import org.opendatamesh.platform.up.executor.gitlabci.utils.HashMapConverter;

import javax.persistence.*;
import java.util.Map;

@Data
@Entity(name = "PielineRun")
@Table(name = "PIPELINE_RUNS", schema = "ODMEXECUTOR")
public class PipelineRun {
    @Id
    @Column(name="TASK_ID")
    protected Long taskId;

    @Column(name="RUN_ID")
    protected String runId;

    @Column(name="PROJECT")
    protected String project;

    @Column(name="STATUS")
    @Enumerated(EnumType.STRING)
    protected GitlabRunState status;

    @Column(name = "VARIABLES")
    @Convert(converter = HashMapConverter.class)
    protected Map<String, String> variables;

    @Column(name = "CREATED_AT")
    protected String createdAt;

    @Column(name = "FINISHED_AT")
    protected String finishedAt;
}
