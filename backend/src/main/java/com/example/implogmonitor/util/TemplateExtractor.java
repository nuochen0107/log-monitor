package com.example.implogmonitor.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class TemplateExtractor {
    private TemplateExtractor() {
    }

    public static String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replaceAll("\\b1[3-9]\\d{9}\\b", "{mobile}")
                .replaceAll("\\b[A-Z]{2,}\\d{6,}\\b", "{bizNo}")
                .replaceAll("\\b\\d{4}-\\d{2}-\\d{2}[ T]\\d{2}:\\d{2}:\\d{2}(\\.\\d{1,3})?\\b", "{time}")
                .replaceAll("\\b[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\b", "{uuid}")
                .replaceAll("\\b\\d{5,}\\b", "{num}")
                .replaceAll("\\s+", " ")
                .trim();
    }

    public static String sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : bytes) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
