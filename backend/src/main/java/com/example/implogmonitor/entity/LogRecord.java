package com.example.implogmonitor.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("log_record")
public class LogRecord {
    private Long id;
    private Long taskId;
    private Long deployUnitId;
    private LocalDateTime logTime;
    private String level;
    private String threadName;
    private String loggerName;
    private String traceId;
    private String interfaceName;
    private String message;
    private String rawLine;
    private Integer lineNo;
    private LocalDateTime createdAt;
}
