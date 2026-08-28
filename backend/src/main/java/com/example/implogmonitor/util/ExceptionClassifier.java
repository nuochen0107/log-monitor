package com.example.implogmonitor.util;

import com.example.implogmonitor.dto.ClassificationResult;
import com.example.implogmonitor.enums.ExceptionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ExceptionClassifier {
    private static final List<Rule> RULES = Arrays.asList(
            new Rule(ExceptionType.COUPON, "coupon", "inventory", "stock", "couponTemplateId", "券", "库存"),
            new Rule(ExceptionType.RPC, "timeout", "timed out", "http", "rpc", "feign", "remote call"),
            new Rule(ExceptionType.SQL, "sql", "mysql", "deadlock", "duplicate", "dataintegrity", "jdbc"),
            new Rule(ExceptionType.KAFKA, "kafka", "consumer", "offset", "message", "deserialize"),
            new Rule(ExceptionType.JOB, "job", "schedule", "saturn", "resend", "batchNo"),
            new Rule(ExceptionType.REDIS, "redis", "lock", "cache", "jedis"),
            new Rule(ExceptionType.PARAMETER, "param", "argument", "validation", "invalid", "blank", "参数")
    );

    private ExceptionClassifier() {
    }

    public static ExceptionType classify(String text) {
        return analyze(text).getExceptionType();
    }

    public static ClassificationResult analyze(String text) {
        String lower = text == null ? "" : text.toLowerCase();
        Rule bestRule = null;
        List<String> bestKeywords = new ArrayList<>();
        for (Rule rule : RULES) {
            List<String> matched = new ArrayList<>();
            for (String keyword : rule.keywords) {
                if (lower.contains(keyword.toLowerCase())) {
                    matched.add(keyword);
                }
            }
            if (matched.size() > bestKeywords.size()) {
                bestRule = rule;
                bestKeywords = matched;
            }
        }
        if (bestRule == null) {
            return new ClassificationResult(ExceptionType.SYSTEM, new ArrayList<>(), 0D);
        }
        double score = Math.min(1D, 0.45D + bestKeywords.size() * 0.15D);
        return new ClassificationResult(bestRule.exceptionType, bestKeywords, score);
    }

    private static class Rule {
        private final ExceptionType exceptionType;
        private final List<String> keywords;

        private Rule(ExceptionType exceptionType, String... keywords) {
            this.exceptionType = exceptionType;
            this.keywords = Arrays.asList(keywords);
        }
    }
}
