package com.example.implogmonitor.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.implogmonitor.common.ApiResponse;
import com.example.implogmonitor.dto.TraceChainResult;
import com.example.implogmonitor.entity.AnalysisTask;
import com.example.implogmonitor.entity.LogRecord;
import com.example.implogmonitor.mapper.AnalysisTaskMapper;
import com.example.implogmonitor.mapper.LogRecordMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class LogController {
    @Resource
    private AnalysisTaskMapper analysisTaskMapper;
    @Resource
    private LogRecordMapper logRecordMapper;

    @GetMapping("/tasks")
    public ApiResponse<List<AnalysisTask>> tasks(@RequestParam(required = false) Long deployUnitId) {
        return ApiResponse.ok(analysisTaskMapper.selectList(new LambdaQueryWrapper<AnalysisTask>()
                .eq(deployUnitId != null, AnalysisTask::getDeployUnitId, deployUnitId)
                .orderByDesc(AnalysisTask::getId)));
    }

    @GetMapping("/tasks/{id}")
    public ApiResponse<AnalysisTask> taskDetail(@PathVariable Long id) {
        return ApiResponse.ok(analysisTaskMapper.selectById(id));
    }

    @GetMapping("/records")
    public ApiResponse<Page<LogRecord>> records(@RequestParam(required = false) Long taskId,
                                                @RequestParam(required = false) Long deployUnitId,
                                                @RequestParam(required = false) String traceId,
                                                @RequestParam(defaultValue = "1") long page,
                                                @RequestParam(defaultValue = "20") long size) {
        LambdaQueryWrapper<LogRecord> wrapper = new LambdaQueryWrapper<LogRecord>()
                .eq(taskId != null, LogRecord::getTaskId, taskId)
                .eq(deployUnitId != null, LogRecord::getDeployUnitId, deployUnitId)
                .eq(traceId != null && !traceId.isEmpty(), LogRecord::getTraceId, traceId)
                .orderByDesc(LogRecord::getId);
        return ApiResponse.ok(logRecordMapper.selectPage(new Page<>(page, size), wrapper));
    }

    @GetMapping("/trace")
    public ApiResponse<TraceChainResult> trace(@RequestParam String traceId) {
        if (traceId == null || traceId.trim().isEmpty()) {
            throw new IllegalArgumentException("traceId 不能为空");
        }
        List<LogRecord> records = logRecordMapper.selectList(new LambdaQueryWrapper<LogRecord>()
                .eq(LogRecord::getTraceId, traceId.trim())
                .orderByAsc(LogRecord::getLogTime)
                .orderByAsc(LogRecord::getLineNo));
        TraceChainResult result = new TraceChainResult();
        result.setTraceId(traceId.trim());
        result.setRecords(records);
        result.setLogCount(records.size());
        result.setExceptionCount((int) records.stream().filter(this::isExceptionRecord).count());
        records.stream()
                .filter(record -> record.getLogTime() != null)
                .min(Comparator.comparing(LogRecord::getLogTime))
                .ifPresent(record -> result.setFirstLogTime(record.getLogTime()));
        records.stream()
                .filter(record -> record.getLogTime() != null)
                .max(Comparator.comparing(LogRecord::getLogTime))
                .ifPresent(record -> result.setLastLogTime(record.getLogTime()));
        records.stream().filter(this::isExceptionRecord).findFirst().ifPresent(record -> {
            result.setRootRecord(record);
            result.setRootSummary(buildRootSummary(record));
        });
        if (result.getRootSummary() == null && !records.isEmpty()) {
            result.setRootSummary("未发现明确 ERROR/Exception 日志，可按时间顺序查看请求上下文。");
        }
        return ApiResponse.ok(result);
    }

    private boolean isExceptionRecord(LogRecord record) {
        String level = record.getLevel() == null ? "" : record.getLevel();
        String rawLine = record.getRawLine() == null ? "" : record.getRawLine();
        String message = record.getMessage() == null ? "" : record.getMessage();
        return "ERROR".equals(level) || rawLine.contains("Exception") || message.toLowerCase().contains("failed");
    }

    private String buildRootSummary(LogRecord record) {
        String interfaceName = record.getInterfaceName() == null ? "未知接口" : record.getInterfaceName();
        String message = record.getMessage() == null || record.getMessage().isEmpty() ? record.getRawLine() : record.getMessage();
        return "疑似根因出现在接口 " + interfaceName + "，日志摘要：" + message;
    }
}
