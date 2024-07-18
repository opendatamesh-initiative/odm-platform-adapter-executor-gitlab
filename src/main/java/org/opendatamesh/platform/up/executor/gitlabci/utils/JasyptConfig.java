package org.opendatamesh.platform.up.executor.gitlabci.utils;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JasyptConfig {
    private final String secretKey;

    public JasyptConfig(@Value("${spring.security.encryptor.secret}") String secretKey) {
        this.secretKey = secretKey;
    }

    @Bean("jasyptStringEncryptor")
    @Primary
    public StringEncryptor stringEncryptor() {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(secretKey);
        encryptor.setAlgorithm("PBEWithMD5AndDES");
        return encryptor;
    }
}