package org.opendatamesh.platform.up.executor.gitlabci.services;

import lombok.RequiredArgsConstructor;
import org.jasypt.encryption.StringEncryptor;
import org.opendatamesh.platform.core.commons.servers.exceptions.ConflictException;
import org.opendatamesh.platform.up.executor.gitlabci.dao.GitlabInstance;
import org.opendatamesh.platform.up.executor.gitlabci.dao.GitlabInstanceRepository;
import org.opendatamesh.platform.up.executor.gitlabci.resources.ExecutorApiStandardErrors;
import org.opendatamesh.platform.up.executor.gitlabci.resources.client.GitlabConfigResource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GitlabConfigService {
    private final GitlabInstanceRepository instanceRepository;
    @Qualifier("jasyptStringEncryptor")
    private final StringEncryptor stringEncryptor;

    public GitlabInstance addConfig(GitlabConfigResource configResource) {
        Optional<GitlabInstance> optInstance = instanceRepository.findById(configResource.getInstanceUrl());
        if (optInstance.isPresent()) {
            throw new ConflictException(
                    ExecutorApiStandardErrors.SC409_02_ALREADY_EXISTS,
                    "The instance already exists: " + configResource.getInstanceUrl()
            );
        }

        String encodedToken = stringEncryptor.encrypt(configResource.getInstanceToken());
        GitlabInstance gitlabInstance = new GitlabInstance();
        gitlabInstance.setInstanceToken(encodedToken);
        gitlabInstance.setInstanceUrl(configResource.getInstanceUrl());
        instanceRepository.save(gitlabInstance);

        return gitlabInstance;
    }

    public List<GitlabInstance> getAllGitlabInstances() {
        return instanceRepository.findAll();
    }

    public void deleteGitlabInstance(String instanceId) {
        instanceRepository.deleteById(instanceId);
    }
}
