package com.example.implogmonitor.controller;

import com.example.implogmonitor.common.ApiResponse;
import com.example.implogmonitor.entity.AnalysisTask;
import com.example.implogmonitor.service.LogAnalysisService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/upload")
public class UploadController {
    @Resource
    private LogAnalysisService logAnalysisService;

    @PostMapping("/log")
    public ApiResponse<AnalysisTask> upload(@RequestParam("file") MultipartFile file,
                                            @RequestParam("deployUnitId") Long deployUnitId,
                                            @RequestParam(defaultValue = "APPLICATION") String logType) {
        return ApiResponse.ok(logAnalysisService.uploadAndAnalyze(file, deployUnitId, logType));
    }
}
