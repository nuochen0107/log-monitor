package com.example.implogmonitor.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("log_file")
public class LogFileEntity {
    private Long id;
    private Long taskId;
    private String originalName;
    private String storedPath;
    private Long fileSize;
    private LocalDateTime createdAt;
}
