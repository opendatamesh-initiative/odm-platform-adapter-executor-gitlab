package org.opendatamesh.platform.up.executor.gitlabci;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ODMExecutorGitlabApplication {
    public static void main(String[] args) {
        SpringApplication.run(ODMExecutorGitlabApplication.class, args);
    }
}