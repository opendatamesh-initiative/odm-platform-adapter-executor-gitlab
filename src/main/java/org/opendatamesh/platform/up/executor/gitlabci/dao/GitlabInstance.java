package org.opendatamesh.platform.up.executor.gitlabci.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
public class GitlabInstance {
    @Id
    private String instanceUrl;
    @JsonIgnore
    private String instanceToken;
}
