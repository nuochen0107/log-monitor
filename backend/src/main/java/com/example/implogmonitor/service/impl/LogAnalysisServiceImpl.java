package com.example.implogmonitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.implogmonitor.dto.ClassificationResult;
import com.example.implogmonitor.config.UploadProperties;
import com.example.implogmonitor.dto.LogLineParseResult;
import com.example.implogmonitor.dto.RecommendResult;
import com.example.implogmonitor.entity.*;
import com.example.implogmonitor.enums.ExceptionType;
import com.example.implogmonitor.enums.TaskStatus;
import com.example.implogmonitor.mapper.*;
import com.example.implogmonitor.service.AlertService;
import com.example.implogmonitor.service.KnowledgeRecommendService;
import com.example.implogmonitor.service.LogAnalysisService;
import com.example.implogmonitor.util.ExceptionClassifier;
import com.example.implogmonitor.util.LogLineParser;
import com.example.implogmonitor.util.TemplateExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.StringJoiner;

@Service
public class LogAnalysisServiceImpl implements LogAnalysisService {
    private static final DateTimeFormatter LOG_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Resource
    private UploadProperties uploadProperties;
    @Resource
    private AnalysisTaskMapper analysisTaskMapper;
    @Resource
    private LogFileMapper logFileMapper;
    @Resource
    private LogRecordMapper logRecordMapper;
    @Resource
    private ExceptionEventMapper exceptionEventMapper;
    @Resource
    private ExceptionTemplateMapper exceptionTemplateMapper;
    @Resource
    private KnowledgeRecommendService knowledgeRecommendService;
    @Resource
    private DeployUnitMapper deployUnitMapper;
    @Resource
    private AlertService alertService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnalysisTask uploadAndAnalyze(MultipartFile file, Long deployUnitId, String logType) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("日志文件不能为空");
        }
        DeployUnit deployUnit = deployUnitMapper.selectById(deployUnitId);
        if (deployUnit == null || Integer.valueOf(0).equals(deployUnit.getEnabled())) {
            throw new IllegalArgumentException("部署单元不存在或已停用");
        }
        AnalysisTask task = createTask(file.getOriginalFilename(), deployUnitId, logType);
        Path storedPath = storeFile(file, task.getId());
        saveFileMeta(file, task.getId(), storedPath);
        analyzeFile(task, storedPath);
        return analysisTaskMapper.selectById(task.getId());
    }

    private AnalysisTask createTask(String fileName, Long deployUnitId, String logType) {
        AnalysisTask task = new AnalysisTask();
        task.setDeployUnitId(deployUnitId);
        task.setTaskName(fileName == null ? "log-analysis" : fileName);
        task.setLogType(logType == null || logType.isEmpty() ? "APPLICATION" : logType);
        task.setStatus(TaskStatus.RUNNING.name());
        task.setTotalLines(0);
        task.setErrorCount(0);
        task.setStartedAt(LocalDateTime.now());
        analysisTaskMapper.insert(task);
        return task;
    }

    private Path storeFile(MultipartFile file, Long taskId) {
        try {
            Path dir = Paths.get(uploadProperties.getUploadDir()).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            String safeName = taskId + "-" + file.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_");
            Path target = dir.resolve(safeName);
            file.transferTo(target.toFile());
            return target;
        } catch (IOException e) {
            throw new IllegalStateException("保存日志文件失败", e);
        }
    }

    private void saveFileMeta(MultipartFile file, Long taskId, Path storedPath) {
        LogFileEntity logFile = new LogFileEntity();
        logFile.setTaskId(taskId);
        logFile.setOriginalName(file.getOriginalFilename());
        logFile.setStoredPath(storedPath.toString());
        logFile.setFileSize(file.getSize());
        logFileMapper.insert(logFile);
    }

    private void analyzeFile(AnalysisTask task, Path storedPath) {
        int lineNo = 0;
        int errorCount = 0;
        long started = System.currentTimeMillis();
        try (BufferedReader reader = Files.newBufferedReader(storedPath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                LogLineParseResult parsed = LogLineParser.parse(line);
                LogRecord record = toRecord(task, lineNo, line, parsed);
                logRecordMapper.insert(record);
                if (isExceptionLine(parsed, line)) {
                    errorCount++;
                    String stackTrace = collectStackTrace(line, reader);
                    ExceptionEvent event = buildEvent(task.getId(), record, stackTrace);
                    exceptionEventMapper.insert(event);
                    upsertTemplate(event);
                }
            }
            finishTask(task.getId(), TaskStatus.SUCCESS, lineNo, errorCount, System.currentTimeMillis() - started);
            AnalysisTask finishedTask = analysisTaskMapper.selectById(task.getId());
            alertService.scanTask(finishedTask);
        } catch (Exception e) {
            finishTask(task.getId(), TaskStatus.FAILED, lineNo, errorCount, System.currentTimeMillis() - started);
            throw new IllegalStateException("分析日志失败", e);
        }
    }

    private LogRecord toRecord(AnalysisTask task, int lineNo, String rawLine, LogLineParseResult parsed) {
        LogRecord record = new LogRecord();
        record.setTaskId(task.getId());
        record.setDeployUnitId(task.getDeployUnitId());
        record.setLineNo(lineNo);
        record.setRawLine(rawLine);
        record.setLevel(parsed.isMatched() ? parsed.getLevel() : "STACK");
        record.setThreadName(parsed.getThreadName());
        record.setLoggerName(parsed.getLoggerName());
        record.setTraceId(parsed.getTraceId());
        record.setInterfaceName(parsed.getInterfaceName());
        record.setMessage(parsed.getMessage());
        if (parsed.isMatched()) {
            record.setLogTime(LocalDateTime.parse(parsed.getLogTimeText(), LOG_TIME_FORMATTER));
        }
        return record;
    }

    private boolean isExceptionLine(LogLineParseResult parsed, String line) {
        return "ERROR".equals(parsed.getLevel()) || line.contains("Exception") || line.toLowerCase().contains("failed");
    }

    private String collectStackTrace(String firstLine, BufferedReader reader) throws IOException {
        StringBuilder builder = new StringBuilder(firstLine);
        reader.mark(65536);
        String next;
        while ((next = reader.readLine()) != null) {
            if (LogLineParser.parse(next).isMatched()) {
                reader.reset();
                break;
            }
            builder.append('\n').append(next);
            reader.mark(65536);
        }
        return builder.toString();
    }

    private ExceptionEvent buildEvent(Long taskId, LogRecord record, String stackTrace) {
        String source = record.getMessage() + "\n" + stackTrace;
        ClassificationResult classification = ExceptionClassifier.analyze(source);
        ExceptionType type = classification.getExceptionType();
        String template = TemplateExtractor.normalize(source);
        RecommendResult recommend = knowledgeRecommendService.recommend(type.name(), template);
        ExceptionEvent event = new ExceptionEvent();
        event.setTaskId(taskId);
        event.setDeployUnitId(record.getDeployUnitId());
        event.setRecordId(record.getId());
        event.setEventTime(record.getLogTime());
        event.setExceptionType(type.name());
        event.setClassifyScore(BigDecimal.valueOf(classification.getScore()));
        event.setMatchedKeywords(joinKeywords(classification));
        event.setSeverity("ERROR".equals(record.getLevel()) ? "HIGH" : "MEDIUM");
        event.setTraceId(record.getTraceId());
        event.setInterfaceName(record.getInterfaceName());
        event.setTitle(record.getMessage() == null ? record.getRawLine() : record.getMessage());
        event.setTemplateText(template);
        event.setStackTrace(stackTrace);
        event.setRecommendId(recommend.getKnowledgeId());
        event.setRecommendTitle(recommend.getTitle());
        event.setSimilarity(BigDecimal.valueOf(recommend.getSimilarity()));
        return event;
    }

    private String joinKeywords(ClassificationResult classification) {
        StringJoiner joiner = new StringJoiner(",");
        classification.getMatchedKeywords().forEach(joiner::add);
        return joiner.toString();
    }

    private void upsertTemplate(ExceptionEvent event) {
        String hash = TemplateExtractor.sha256(event.getExceptionType() + "|" + event.getTemplateText());
        ExceptionTemplate template = exceptionTemplateMapper.selectOne(new LambdaQueryWrapper<ExceptionTemplate>()
                .eq(ExceptionTemplate::getTemplateHash, hash));
        if (template == null) {
            template = new ExceptionTemplate();
            template.setTemplateHash(hash);
            template.setExceptionType(event.getExceptionType());
            template.setTemplateText(event.getTemplateText());
            template.setOccurCount(1);
            template.setFirstSeenAt(LocalDateTime.now());
            template.setLastSeenAt(LocalDateTime.now());
            exceptionTemplateMapper.insert(template);
        } else {
            template.setOccurCount(template.getOccurCount() + 1);
            template.setLastSeenAt(LocalDateTime.now());
            exceptionTemplateMapper.updateById(template);
        }
    }

    private void finishTask(Long taskId, TaskStatus status, int totalLines, int errorCount, long costMs) {
        AnalysisTask update = new AnalysisTask();
        update.setId(taskId);
        update.setStatus(status.name());
        update.setTotalLines(totalLines);
        update.setErrorCount(errorCount);
        update.setCostMs(costMs);
        update.setFinishedAt(LocalDateTime.now());
        analysisTaskMapper.updateById(update);
    }
}
