package com.example.implogmonitor.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.implogmonitor.common.ApiResponse;
import com.example.implogmonitor.entity.ExceptionEvent;
import com.example.implogmonitor.entity.RecommendFeedback;
import com.example.implogmonitor.mapper.ExceptionEventMapper;
import com.example.implogmonitor.mapper.RecommendFeedbackMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {
    @Resource
    private RecommendFeedbackMapper recommendFeedbackMapper;
    @Resource
    private ExceptionEventMapper exceptionEventMapper;

    @GetMapping
    public ApiResponse<List<RecommendFeedback>> list(@RequestParam(required = false) Long eventId,
                                                     @RequestParam(required = false) Integer useful) {
        LambdaQueryWrapper<RecommendFeedback> wrapper = new LambdaQueryWrapper<RecommendFeedback>()
                .eq(eventId != null, RecommendFeedback::getEventId, eventId)
                .eq(useful != null, RecommendFeedback::getUseful, useful)
                .orderByDesc(RecommendFeedback::getId);
        return ApiResponse.ok(recommendFeedbackMapper.selectList(wrapper));
    }

    @PostMapping
    public ApiResponse<RecommendFeedback> save(@RequestBody RecommendFeedback feedback) {
        if (feedback.getEventId() == null) {
            throw new IllegalArgumentException("异常事件不能为空");
        }
        ExceptionEvent event = exceptionEventMapper.selectById(feedback.getEventId());
        if (event == null) {
            throw new IllegalArgumentException("异常事件不存在");
        }
        if (feedback.getKnowledgeId() == null) {
            feedback.setKnowledgeId(event.getRecommendId());
        }
        if (feedback.getUseful() == null) {
            feedback.setUseful(1);
        }
        recommendFeedbackMapper.insert(feedback);
        return ApiResponse.ok(feedback);
    }
}
