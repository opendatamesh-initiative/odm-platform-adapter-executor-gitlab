package org.opendatamesh.platform.up.executor.gitlabci.dao;

import lombok.Data;
import org.opendatamesh.platform.up.executor.gitlabci.resources.client.GitlabRunState;
import org.opendatamesh.platform.up.executor.gitlabci.resources.client.GitlabVariable;
import org.opendatamesh.platform.up.executor.gitlabci.utils.HashMapConverter;
import org.opendatamesh.platform.up.executor.gitlabci.utils.ListMapConverter;

import javax.persistence.*;
import java.util.List;
import java.util.Map;

@Data
@Entity(name = "PielineRun")
@Table(name = "PIPELINE_RUNS", schema = "ODMEXECUTOR")
public class PipelineRun {
    @Id
    @Column(name = "TASK_ID")
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

    protected String gitlabInstanceUrl;
}
