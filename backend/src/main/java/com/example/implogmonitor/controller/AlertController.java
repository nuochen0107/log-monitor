package com.example.implogmonitor.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.implogmonitor.common.ApiResponse;
import com.example.implogmonitor.entity.AlertEvent;
import com.example.implogmonitor.entity.AlertRule;
import com.example.implogmonitor.mapper.AlertEventMapper;
import com.example.implogmonitor.mapper.AlertRuleMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {
    @Resource
    private AlertRuleMapper alertRuleMapper;
    @Resource
    private AlertEventMapper alertEventMapper;

    @GetMapping("/rules")
    public ApiResponse<List<AlertRule>> rules(@RequestParam(required = false) Long deployUnitId) {
        return ApiResponse.ok(alertRuleMapper.selectList(new LambdaQueryWrapper<AlertRule>()
                .eq(deployUnitId != null, AlertRule::getDeployUnitId, deployUnitId)
                .orderByDesc(AlertRule::getId)));
    }

    @PostMapping("/rules")
    public ApiResponse<AlertRule> saveRule(@RequestBody AlertRule rule) {
        if (rule.getWindowMinutes() == null || rule.getWindowMinutes() <= 0) {
            rule.setWindowMinutes(10);
        }
        if (rule.getThresholdCount() == null || rule.getThresholdCount() <= 0) {
            rule.setThresholdCount(1);
        }
        if (rule.getSeverity() == null || rule.getSeverity().isEmpty()) {
            rule.setSeverity("MEDIUM");
        }
        if (rule.getEnabled() == null) {
            rule.setEnabled(1);
        }
        if (rule.getId() == null) {
            alertRuleMapper.insert(rule);
        } else {
            alertRuleMapper.updateById(rule);
        }
        return ApiResponse.ok(rule);
    }

    @GetMapping("/events")
    public ApiResponse<Page<AlertEvent>> events(@RequestParam(required = false) Long deployUnitId,
                                                @RequestParam(required = false) String status,
                                                @RequestParam(defaultValue = "1") long page,
                                                @RequestParam(defaultValue = "20") long size) {
        LambdaQueryWrapper<AlertEvent> wrapper = new LambdaQueryWrapper<AlertEvent>()
                .eq(deployUnitId != null, AlertEvent::getDeployUnitId, deployUnitId)
                .eq(status != null && !status.isEmpty(), AlertEvent::getStatus, status)
                .orderByDesc(AlertEvent::getId);
        return ApiResponse.ok(alertEventMapper.selectPage(new Page<>(page, size), wrapper));
    }

    @PostMapping("/events/{id}/close")
    public ApiResponse<Boolean> close(@PathVariable Long id) {
        AlertEvent event = new AlertEvent();
        event.setId(id);
        event.setStatus("CLOSED");
        event.setClosedAt(LocalDateTime.now());
        return ApiResponse.ok(alertEventMapper.updateById(event) > 0);
    }
}
