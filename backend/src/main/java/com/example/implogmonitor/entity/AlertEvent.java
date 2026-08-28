package com.example.implogmonitor.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("alert_event")
public class AlertEvent {
    private Long id;
    private Long ruleId;
    private Long deployUnitId;
    private Long taskId;
    private String alertTitle;
    private String exceptionType;
    private String interfaceName;
    private Integer triggerCount;
    private LocalDateTime windowStart;
    private LocalDateTime windowEnd;
    private String severity;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;
}
