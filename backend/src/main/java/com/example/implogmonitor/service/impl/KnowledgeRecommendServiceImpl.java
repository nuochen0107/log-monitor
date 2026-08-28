package com.example.implogmonitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.implogmonitor.dto.RecommendResult;
import com.example.implogmonitor.entity.KnowledgeBase;
import com.example.implogmonitor.mapper.KnowledgeBaseMapper;
import com.example.implogmonitor.service.KnowledgeRecommendService;
import com.example.implogmonitor.util.TextSimilarity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class KnowledgeRecommendServiceImpl implements KnowledgeRecommendService {
    @Resource
    private KnowledgeBaseMapper knowledgeBaseMapper;

    @Override
    public RecommendResult recommend(String exceptionType, String templateText) {
        List<KnowledgeBase> candidates = knowledgeBaseMapper.selectList(new LambdaQueryWrapper<KnowledgeBase>()
                .eq(KnowledgeBase::getEnabled, 1)
                .eq(KnowledgeBase::getExceptionType, exceptionType));
        double bestScore = 0D;
        KnowledgeBase best = null;
        for (KnowledgeBase candidate : candidates) {
            double score = TextSimilarity.jaccard(templateText, candidate.getKeywords() + " " + candidate.getTitle() + " " + candidate.getCauseDesc());
            if (score > bestScore) {
                bestScore = score;
                best = candidate;
            }
        }
        if (best == null) {
            return new RecommendResult(null, null, 0D);
        }
        return new RecommendResult(best.getId(), best.getTitle(), bestScore);
    }
}
