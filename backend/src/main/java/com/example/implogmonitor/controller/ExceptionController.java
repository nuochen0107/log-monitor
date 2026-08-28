package com.example.implogmonitor.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.implogmonitor.common.ApiResponse;
import com.example.implogmonitor.entity.ExceptionEvent;
import com.example.implogmonitor.entity.ExceptionTemplate;
import com.example.implogmonitor.mapper.ExceptionEventMapper;
import com.example.implogmonitor.mapper.ExceptionTemplateMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/exceptions")
public class ExceptionController {
    @Resource
    private ExceptionEventMapper exceptionEventMapper;
    @Resource
    private ExceptionTemplateMapper exceptionTemplateMapper;

    @GetMapping("/events")
    public ApiResponse<Page<ExceptionEvent>> events(@RequestParam(required = false) Long taskId,
                                                    @RequestParam(required = false) Long deployUnitId,
                                                    @RequestParam(required = false) String type,
                                                    @RequestParam(defaultValue = "1") long page,
                                                    @RequestParam(defaultValue = "20") long size) {
        LambdaQueryWrapper<ExceptionEvent> wrapper = new LambdaQueryWrapper<ExceptionEvent>()
                .eq(taskId != null, ExceptionEvent::getTaskId, taskId)
                .eq(deployUnitId != null, ExceptionEvent::getDeployUnitId, deployUnitId)
                .eq(type != null && !type.isEmpty(), ExceptionEvent::getExceptionType, type)
                .orderByDesc(ExceptionEvent::getId);
        return ApiResponse.ok(exceptionEventMapper.selectPage(new Page<>(page, size), wrapper));
    }

    @GetMapping("/templates")
    public ApiResponse<List<ExceptionTemplate>> templates() {
        return ApiResponse.ok(exceptionTemplateMapper.selectList(new LambdaQueryWrapper<ExceptionTemplate>()
                .orderByDesc(ExceptionTemplate::getOccurCount)));
    }
}
