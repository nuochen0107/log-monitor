package com.example.implogmonitor.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("analysis_task")
public class AnalysisTask {
    private Long id;
    private Long deployUnitId;
    private String taskName;
    private String logType;
    private String status;
    private Integer totalLines;
    private Integer errorCount;
    private Long costMs;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
