USE imp_log_monitor;

CREATE TABLE IF NOT EXISTS alert_rule (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  rule_name VARCHAR(128) NOT NULL,
  deploy_unit_id BIGINT NULL,
  exception_type VARCHAR(64),
  interface_name VARCHAR(255),
  window_minutes INT NOT NULL DEFAULT 10,
  threshold_count INT NOT NULL DEFAULT 1,
  severity VARCHAR(16) NOT NULL DEFAULT 'MEDIUM',
  enabled TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_deploy_unit(deploy_unit_id),
  INDEX idx_rule_type(exception_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS alert_event (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  rule_id BIGINT NOT NULL,
  deploy_unit_id BIGINT NOT NULL,
  task_id BIGINT NOT NULL,
  alert_title VARCHAR(255) NOT NULL,
  exception_type VARCHAR(64),
  interface_name VARCHAR(255),
  trigger_count INT NOT NULL,
  window_start DATETIME,
  window_end DATETIME,
  severity VARCHAR(16) NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'OPEN',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  closed_at DATETIME,
  INDEX idx_deploy_unit(deploy_unit_id),
  INDEX idx_status(status),
  INDEX idx_task(task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO alert_rule(rule_name, deploy_unit_id, exception_type, interface_name, window_minutes, threshold_count, severity, enabled)
SELECT '单批次任意异常监控', NULL, NULL, NULL, 10, 1, 'MEDIUM', 1
WHERE NOT EXISTS (SELECT 1 FROM alert_rule WHERE rule_name = '单批次任意异常监控');

INSERT INTO alert_rule(rule_name, deploy_unit_id, exception_type, interface_name, window_minutes, threshold_count, severity, enabled)
SELECT 'RPC 调用异常监控', NULL, 'RPC', NULL, 10, 1, 'HIGH', 1
WHERE NOT EXISTS (SELECT 1 FROM alert_rule WHERE rule_name = 'RPC 调用异常监控');

INSERT INTO alert_rule(rule_name, deploy_unit_id, exception_type, interface_name, window_minutes, threshold_count, severity, enabled)
SELECT '数据库异常监控', NULL, 'SQL', NULL, 10, 1, 'HIGH', 1
WHERE NOT EXISTS (SELECT 1 FROM alert_rule WHERE rule_name = '数据库异常监控');

INSERT INTO alert_rule(rule_name, deploy_unit_id, exception_type, interface_name, window_minutes, threshold_count, severity, enabled)
SELECT '消息消费异常监控', NULL, 'KAFKA', NULL, 10, 1, 'MEDIUM', 1
WHERE NOT EXISTS (SELECT 1 FROM alert_rule WHERE rule_name = '消息消费异常监控');
