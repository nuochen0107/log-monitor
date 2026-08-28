package com.example.implogmonitor.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("knowledge_base")
public class KnowledgeBase {
    private Long id;
    private String title;
    private String exceptionType;
    private String keywords;
    private String causeDesc;
    private String solution;
    private Integer enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
