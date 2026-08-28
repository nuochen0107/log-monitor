package com.example.implogmonitor.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("alert_rule")
public class AlertRule {
    private Long id;
    private String ruleName;
    private Long deployUnitId;
    private String exceptionType;
    private String interfaceName;
    private Integer windowMinutes;
    private Integer thresholdCount;
    private String severity;
    private Integer enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
