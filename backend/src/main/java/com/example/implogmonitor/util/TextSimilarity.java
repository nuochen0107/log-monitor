package com.example.implogmonitor.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class TextSimilarity {
    private TextSimilarity() {
    }

    public static double jaccard(String left, String right) {
        Set<String> a = tokenize(left);
        Set<String> b = tokenize(right);
        if (a.isEmpty() || b.isEmpty()) {
            return 0D;
        }
        Set<String> intersection = new HashSet<>(a);
        intersection.retainAll(b);
        Set<String> union = new HashSet<>(a);
        union.addAll(b);
        return (double) intersection.size() / union.size();
    }

    private static Set<String> tokenize(String text) {
        if (text == null) {
            return new HashSet<>();
        }
        return Arrays.stream(text.toLowerCase().split("[^a-z0-9\\u4e00-\\u9fa5]+"))
                .filter(token -> token.length() > 1)
                .collect(Collectors.toSet());
    }
}
