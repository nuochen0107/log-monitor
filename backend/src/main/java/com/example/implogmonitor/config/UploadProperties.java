package com.example.implogmonitor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UploadProperties {
    @Value("${log-monitor.upload-dir:./data/uploads}")
    private String uploadDir;

    public String getUploadDir() {
        return uploadDir;
    }
}
