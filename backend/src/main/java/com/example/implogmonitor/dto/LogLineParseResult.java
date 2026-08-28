package com.example.implogmonitor.dto;

import lombok.Data;

@Data
public class LogLineParseResult {
    private boolean matched;
    private String logTimeText;
    private String level;
    private String threadName;
    private String loggerName;
    private String traceId;
    private String interfaceName;
    private String message;
}
