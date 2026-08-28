USE imp_log_monitor;

ALTER TABLE exception_event ADD COLUMN classify_score DECIMAL(8,4) DEFAULT 0 AFTER exception_type;
ALTER TABLE exception_event ADD COLUMN matched_keywords VARCHAR(512) AFTER classify_score;
ALTER TABLE exception_event ADD INDEX idx_exception_type(exception_type);
