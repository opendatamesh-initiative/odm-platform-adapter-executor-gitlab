package org.opendatamesh.platform.up.executor.gitlabci.resources.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Data
public class GitlabRunResource {
    private String id;
    private String iid;
    @JsonProperty("project_id")
    private int projectId;
    private String sha;
    private String ref;
    private String status;
    @JsonProperty("before_sha")
    private String beforeSha;
    private boolean tag;
    @JsonProperty("yaml_errors")
    private String yamlErrors;
    private User user;
    @JsonProperty("created_at")
    private ZonedDateTime createdAt;
    @JsonProperty("updated_at")
    private ZonedDateTime updatedAt;
    @JsonProperty("started_at")
    private ZonedDateTime startedAt;
    @JsonProperty("finished_at")
    private ZonedDateTime finishedAt;
    @JsonProperty("committed_at")
    private ZonedDateTime committedAt;
    private Double duration;
    @JsonProperty("queued_duration")
    private Double queuedDuration;
    private String coverage;
    @JsonProperty("web_url")
    private String webUrl;
    private Message message;

    @Data
    public static class User {
        private String name;
        private String username;
        private int id;
        private String state;
        @JsonProperty("avatar_url")
        private String avatarUrl;
        @JsonProperty("web_url")
        private String webUrl;

    }

    @Data
    public static class Message {
        private List<String> base;
    }
}
