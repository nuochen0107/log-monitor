package com.example.implogmonitor.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("exception_event")
public class ExceptionEvent {
    private Long id;
    private Long taskId;
    private Long deployUnitId;
    private Long recordId;
    private LocalDateTime eventTime;
    private String exceptionType;
    private BigDecimal classifyScore;
    private String matchedKeywords;
    private String severity;
    private String traceId;
    private String interfaceName;
    private String title;
    private String templateText;
    private String stackTrace;
    private Long recommendId;
    private String recommendTitle;
    private BigDecimal similarity;
    private LocalDateTime createdAt;
}
