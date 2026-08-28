package com.example.implogmonitor.service;

import com.example.implogmonitor.dto.RecommendResult;

public interface KnowledgeRecommendService {
    RecommendResult recommend(String exceptionType, String templateText);
}
