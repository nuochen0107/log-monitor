package com.example.implogmonitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.implogmonitor.entity.AlertEvent;
import com.example.implogmonitor.entity.AlertRule;
import com.example.implogmonitor.entity.AnalysisTask;
import com.example.implogmonitor.entity.ExceptionEvent;
import com.example.implogmonitor.mapper.AlertEventMapper;
import com.example.implogmonitor.mapper.AlertRuleMapper;
import com.example.implogmonitor.mapper.ExceptionEventMapper;
import com.example.implogmonitor.service.AlertService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlertServiceImpl implements AlertService {
    @Resource
    private AlertRuleMapper alertRuleMapper;
    @Resource
    private AlertEventMapper alertEventMapper;
    @Resource
    private ExceptionEventMapper exceptionEventMapper;

    @Override
    public void scanTask(AnalysisTask task) {
        List<AlertRule> rules = alertRuleMapper.selectList(new LambdaQueryWrapper<AlertRule>()
                .eq(AlertRule::getEnabled, 1)
                .and(wrapper -> wrapper.isNull(AlertRule::getDeployUnitId)
                        .or()
                        .eq(AlertRule::getDeployUnitId, task.getDeployUnitId())));
        for (AlertRule rule : rules) {
            int count = countMatches(task, rule);
            if (count >= rule.getThresholdCount()) {
                createAlertEvent(task, rule, count);
            }
        }
    }

    private int countMatches(AnalysisTask task, AlertRule rule) {
        LambdaQueryWrapper<ExceptionEvent> wrapper = new LambdaQueryWrapper<ExceptionEvent>()
                .eq(ExceptionEvent::getTaskId, task.getId())
                .eq(rule.getDeployUnitId() != null, ExceptionEvent::getDeployUnitId, rule.getDeployUnitId())
                .eq(rule.getExceptionType() != null && !rule.getExceptionType().isEmpty(), ExceptionEvent::getExceptionType, rule.getExceptionType())
                .eq(rule.getInterfaceName() != null && !rule.getInterfaceName().isEmpty(), ExceptionEvent::getInterfaceName, rule.getInterfaceName());
        return exceptionEventMapper.selectCount(wrapper).intValue();
    }

    private void createAlertEvent(AnalysisTask task, AlertRule rule, int count) {
        AlertEvent event = new AlertEvent();
        event.setRuleId(rule.getId());
        event.setDeployUnitId(task.getDeployUnitId());
        event.setTaskId(task.getId());
        event.setAlertTitle(rule.getRuleName() + " 触发，异常数 " + count);
        event.setExceptionType(rule.getExceptionType());
        event.setInterfaceName(rule.getInterfaceName());
        event.setTriggerCount(count);
        event.setWindowStart(task.getStartedAt());
        event.setWindowEnd(task.getFinishedAt() == null ? LocalDateTime.now() : task.getFinishedAt());
        event.setSeverity(rule.getSeverity());
        event.setStatus("OPEN");
        alertEventMapper.insert(event);
    }
}
