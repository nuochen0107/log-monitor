package com.example.implogmonitor.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("deploy_unit")
public class DeployUnit {
    private Long id;
    private String unitName;
    private String appCode;
    private String unitCode;
    private String unitType;
    private String envName;
    private String ownerName;
    private String description;
    private Integer enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
