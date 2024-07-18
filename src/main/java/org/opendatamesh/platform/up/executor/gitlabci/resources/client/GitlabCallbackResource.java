package org.opendatamesh.platform.up.executor.gitlabci.resources.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class GitlabCallbackResource {
    @JsonProperty(value = "object_kind")
    private String objectKind;
    @JsonProperty(value = "object_attributes")
    private ObjectAttributes objectAttributes;
    private Project project;

    @Data
    public static class ObjectAttributes {
        private long id;
        private int iid;
        private String name;
        private String ref;
        private boolean tag;
        private String sha;
        private String beforeSha;
        private String source;
        private String status;
        private String detailedStatus;
        private List<String> stages;
        private String createdAt;
        private String finishedAt;
        private Long duration;
        private int queuedDuration;
        private List<Map<String, String>> variables;
        private String url;
    }

    @Data
    public static class Project {
        private long id;
        private String name;
        private String description;
        private String webUrl;
        private String avatarUrl;
        private String gitSshUrl;
        private String gitHttpUrl;
        private String namespace;
        private int visibilityLevel;
        private String pathWithNamespace;
        private String defaultBranch;
        private String ciConfigPath;
    }
}
