package com.example.implogmonitor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendResult {
    private Long knowledgeId;
    private String title;
    private double similarity;
}
