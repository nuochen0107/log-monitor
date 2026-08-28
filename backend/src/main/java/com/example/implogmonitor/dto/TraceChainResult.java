package com.example.implogmonitor.dto;

import com.example.implogmonitor.entity.LogRecord;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TraceChainResult {
    private String traceId;
    private Integer logCount;
    private Integer exceptionCount;
    private LocalDateTime firstLogTime;
    private LocalDateTime lastLogTime;
    private LogRecord rootRecord;
    private String rootSummary;
    private List<LogRecord> records;
}
