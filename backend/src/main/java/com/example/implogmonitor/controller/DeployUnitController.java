package com.example.implogmonitor.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.implogmonitor.common.ApiResponse;
import com.example.implogmonitor.entity.DeployUnit;
import com.example.implogmonitor.mapper.DeployUnitMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/deploy-units")
public class DeployUnitController {
    @Resource
    private DeployUnitMapper deployUnitMapper;

    @GetMapping
    public ApiResponse<List<DeployUnit>> list(@RequestParam(required = false) String appCode,
                                              @RequestParam(required = false) String unitType,
                                              @RequestParam(required = false) Integer enabled) {
        LambdaQueryWrapper<DeployUnit> wrapper = new LambdaQueryWrapper<DeployUnit>()
                .eq(appCode != null && !appCode.isEmpty(), DeployUnit::getAppCode, appCode)
                .eq(unitType != null && !unitType.isEmpty(), DeployUnit::getUnitType, unitType)
                .eq(enabled != null, DeployUnit::getEnabled, enabled)
                .orderByAsc(DeployUnit::getAppCode)
                .orderByAsc(DeployUnit::getUnitName);
        return ApiResponse.ok(deployUnitMapper.selectList(wrapper));
    }

    @PostMapping
    public ApiResponse<DeployUnit> save(@RequestBody DeployUnit deployUnit) {
        if (deployUnit.getEnabled() == null) {
            deployUnit.setEnabled(1);
        }
        if (deployUnit.getId() == null) {
            deployUnitMapper.insert(deployUnit);
        } else {
            deployUnitMapper.updateById(deployUnit);
        }
        return ApiResponse.ok(deployUnit);
    }
}
