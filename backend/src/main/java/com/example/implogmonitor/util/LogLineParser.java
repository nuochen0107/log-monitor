package com.example.implogmonitor.util;

import com.example.implogmonitor.dto.LogLineParseResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LogLineParser {
    private static final Pattern LINE_PATTERN = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3})\\s+(TRACE|DEBUG|INFO|WARN|ERROR)\\s+\\[([^]]+)]\\s+([^\\s]+)\\s*(.*)$");
    private static final Pattern TRACE_PATTERN = Pattern.compile("\\[traceId=([^]]+)]");
    private static final Pattern INTERFACE_PATTERN = Pattern.compile("\\[interface=([^]]+)]");

    private LogLineParser() {
    }

    public static LogLineParseResult parse(String line) {
        LogLineParseResult result = new LogLineParseResult();
        Matcher matcher = LINE_PATTERN.matcher(line);
        if (!matcher.matches()) {
            result.setMatched(false);
            result.setMessage(line);
            return result;
        }
        result.setMatched(true);
        result.setLogTimeText(matcher.group(1));
        result.setLevel(matcher.group(2));
        result.setThreadName(matcher.group(3));
        result.setLoggerName(matcher.group(4));
        String tail = matcher.group(5);
        Matcher traceMatcher = TRACE_PATTERN.matcher(tail);
        if (traceMatcher.find()) {
            result.setTraceId(traceMatcher.group(1));
        }
        Matcher interfaceMatcher = INTERFACE_PATTERN.matcher(tail);
        if (interfaceMatcher.find()) {
            result.setInterfaceName(interfaceMatcher.group(1));
        }
        result.setMessage(tail.replaceAll("\\[traceId=[^]]+]", "").replaceAll("\\[interface=[^]]+]", "").trim());
        return result;
    }
}
