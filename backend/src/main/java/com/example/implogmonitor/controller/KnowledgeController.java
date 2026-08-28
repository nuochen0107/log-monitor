package com.example.implogmonitor.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.implogmonitor.common.ApiResponse;
import com.example.implogmonitor.entity.KnowledgeBase;
import com.example.implogmonitor.mapper.KnowledgeBaseMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {
    @Resource
    private KnowledgeBaseMapper knowledgeBaseMapper;

    @GetMapping
    public ApiResponse<List<KnowledgeBase>> list(@RequestParam(required = false) String exceptionType,
                                                 @RequestParam(required = false) Integer enabled) {
        return ApiResponse.ok(knowledgeBaseMapper.selectList(new LambdaQueryWrapper<KnowledgeBase>()
                .eq(exceptionType != null && !exceptionType.isEmpty(), KnowledgeBase::getExceptionType, exceptionType)
                .eq(enabled != null, KnowledgeBase::getEnabled, enabled)
                .orderByDesc(KnowledgeBase::getId)));
    }

    @PostMapping
    public ApiResponse<KnowledgeBase> save(@Valid @RequestBody KnowledgeBase knowledgeBase) {
        if (knowledgeBase.getId() == null) {
            knowledgeBase.setEnabled(knowledgeBase.getEnabled() == null ? 1 : knowledgeBase.getEnabled());
            knowledgeBaseMapper.insert(knowledgeBase);
        } else {
            knowledgeBaseMapper.updateById(knowledgeBase);
        }
        return ApiResponse.ok(knowledgeBase);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        return ApiResponse.ok(knowledgeBaseMapper.deleteById(id) > 0);
    }
}
