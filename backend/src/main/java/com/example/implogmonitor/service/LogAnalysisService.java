package com.example.implogmonitor.service;

import com.example.implogmonitor.entity.AnalysisTask;
import org.springframework.web.multipart.MultipartFile;

public interface LogAnalysisService {
    AnalysisTask uploadAndAnalyze(MultipartFile file, Long deployUnitId, String logType);
}
