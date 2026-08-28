package com.example.implogmonitor;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.implogmonitor.mapper")
public class ImpLogMonitorApplication {
    public static void main(String[] args) {
        SpringApplication.run(ImpLogMonitorApplication.class, args);
    }
}
