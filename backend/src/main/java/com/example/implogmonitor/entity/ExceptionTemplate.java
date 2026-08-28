package com.example.implogmonitor.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("exception_template")
public class ExceptionTemplate {
    private Long id;
    private String templateHash;
    private String exceptionType;
    private String templateText;
    private Integer occurCount;
    private LocalDateTime firstSeenAt;
    private LocalDateTime lastSeenAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
