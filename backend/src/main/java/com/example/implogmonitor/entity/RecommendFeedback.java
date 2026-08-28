package com.example.implogmonitor.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("recommend_feedback")
public class RecommendFeedback {
    private Long id;
    private Long eventId;
    private Long knowledgeId;
    private Integer useful;
    private String feedbackText;
    private LocalDateTime createdAt;
}
