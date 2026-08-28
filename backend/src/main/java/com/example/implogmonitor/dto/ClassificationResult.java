package com.example.implogmonitor.dto;

import com.example.implogmonitor.enums.ExceptionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassificationResult {
    private ExceptionType exceptionType;
    private List<String> matchedKeywords;
    private double score;
}
