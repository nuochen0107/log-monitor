USE imp_log_monitor;

CREATE TABLE IF NOT EXISTS recommend_feedback (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  event_id BIGINT NOT NULL,
  knowledge_id BIGINT,
  useful TINYINT NOT NULL DEFAULT 1,
  feedback_text VARCHAR(512),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_event_id(event_id),
  INDEX idx_knowledge_id(knowledge_id),
  INDEX idx_useful(useful)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
