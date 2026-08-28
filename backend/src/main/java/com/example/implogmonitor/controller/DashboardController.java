package com.example.implogmonitor.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.implogmonitor.common.ApiResponse;
import com.example.implogmonitor.dto.DashboardSummary;
import com.example.implogmonitor.entity.AnalysisTask;
import com.example.implogmonitor.entity.DeployUnit;
import com.example.implogmonitor.entity.ExceptionEvent;
import com.example.implogmonitor.entity.ExceptionTemplate;
import com.example.implogmonitor.entity.LogRecord;
import com.example.implogmonitor.mapper.AnalysisTaskMapper;
import com.example.implogmonitor.mapper.AlertEventMapper;
import com.example.implogmonitor.mapper.DeployUnitMapper;
import com.example.implogmonitor.mapper.ExceptionEventMapper;
import com.example.implogmonitor.mapper.ExceptionTemplateMapper;
import com.example.implogmonitor.mapper.LogRecordMapper;
import com.example.implogmonitor.mapper.RecommendFeedbackMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    @Resource
    private AnalysisTaskMapper analysisTaskMapper;
    @Resource
    private LogRecordMapper logRecordMapper;
    @Resource
    private ExceptionEventMapper exceptionEventMapper;
    @Resource
    private ExceptionTemplateMapper exceptionTemplateMapper;
    @Resource
    private DeployUnitMapper deployUnitMapper;
    @Resource
    private AlertEventMapper alertEventMapper;
    @Resource
    private RecommendFeedbackMapper recommendFeedbackMapper;

    @GetMapping("/summary")
    public ApiResponse<DashboardSummary> summary() {
        DashboardSummary summary = new DashboardSummary();
        summary.setTaskCount(analysisTaskMapper.selectCount(null));
        summary.setLogCount(logRecordMapper.selectCount(null));
        summary.setExceptionCount(exceptionEventMapper.selectCount(null));
        summary.setTemplateCount(exceptionTemplateMapper.selectCount(null));
        summary.setAlertCount(alertEventMapper.selectCount(null));
        summary.setFeedbackCount(recommendFeedbackMapper.selectCount(null));
        return ApiResponse.ok(summary);
    }

    @GetMapping("/type-distribution")
    public ApiResponse<Map<String, Long>> typeDistribution() {
        List<ExceptionEvent> events = exceptionEventMapper.selectList(null);
        return ApiResponse.ok(events.stream().collect(Collectors.groupingBy(ExceptionEvent::getExceptionType, LinkedHashMap::new, Collectors.counting())));
    }

    @GetMapping("/trend")
    public ApiResponse<Map<String, Long>> trend() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00");
        List<ExceptionEvent> events = exceptionEventMapper.selectList(null);
        Map<String, Long> result = events.stream()
                .filter(event -> event.getEventTime() != null)
                .collect(Collectors.groupingBy(event -> event.getEventTime().format(formatter), TreeMap::new, Collectors.counting()));
        return ApiResponse.ok(result);
    }

    @GetMapping("/top-interfaces")
    public ApiResponse<Map<String, Long>> topInterfaces() {
        List<ExceptionEvent> events = exceptionEventMapper.selectList(null);
        Map<String, Long> grouped = events.stream()
                .filter(event -> event.getInterfaceName() != null && !event.getInterfaceName().isEmpty())
                .collect(Collectors.groupingBy(ExceptionEvent::getInterfaceName, Collectors.counting()));
        return ApiResponse.ok(sortTop(grouped, 10));
    }

    @GetMapping("/top-templates")
    public ApiResponse<List<ExceptionTemplate>> topTemplates() {
        return ApiResponse.ok(exceptionTemplateMapper.selectList(new LambdaQueryWrapper<ExceptionTemplate>()
                .orderByDesc(ExceptionTemplate::getOccurCount)
                .last("limit 10")));
    }

    @GetMapping("/deploy-unit-ranking")
    public ApiResponse<List<Map<String, Object>>> deployUnitRanking() {
        List<DeployUnit> units = deployUnitMapper.selectList(null);
        Map<Long, DeployUnit> unitMap = units.stream().collect(Collectors.toMap(DeployUnit::getId, unit -> unit));
        Map<Long, Long> grouped = exceptionEventMapper.selectList(null).stream()
                .filter(event -> event.getDeployUnitId() != null)
                .collect(Collectors.groupingBy(ExceptionEvent::getDeployUnitId, Collectors.counting()));
        List<Map<String, Object>> result = grouped.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(10)
                .map(entry -> {
                    DeployUnit unit = unitMap.get(entry.getKey());
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("deployUnitId", entry.getKey());
                    row.put("unitName", unit == null ? "unknown" : unit.getUnitName());
                    row.put("appCode", unit == null ? "" : unit.getAppCode());
                    row.put("unitType", unit == null ? "" : unit.getUnitType());
                    row.put("value", entry.getValue());
                    return row;
                })
                .collect(Collectors.toList());
        return ApiResponse.ok(result);
    }

    private Map<String, Long> sortTop(Map<String, Long> source, int limit) {
        return source.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
    }
}
