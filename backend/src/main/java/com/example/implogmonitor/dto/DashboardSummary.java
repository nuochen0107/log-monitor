package com.example.implogmonitor.dto;

import lombok.Data;

@Data
public class DashboardSummary {
    private Long taskCount;
    private Long logCount;
    private Long exceptionCount;
    private Long templateCount;
    private Long alertCount;
    private Long feedbackCount;
}
