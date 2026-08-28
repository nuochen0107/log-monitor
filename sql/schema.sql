CREATE DATABASE IF NOT EXISTS imp_log_monitor DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE imp_log_monitor;

DROP TABLE IF EXISTS knowledge_base;
DROP TABLE IF EXISTS alert_event;
DROP TABLE IF EXISTS alert_rule;
DROP TABLE IF EXISTS recommend_feedback;
DROP TABLE IF EXISTS exception_template;
DROP TABLE IF EXISTS exception_event;
DROP TABLE IF EXISTS log_record;
DROP TABLE IF EXISTS log_file;
DROP TABLE IF EXISTS analysis_task;
DROP TABLE IF EXISTS deploy_unit;

CREATE TABLE deploy_unit (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  unit_name VARCHAR(128) NOT NULL,
  app_code VARCHAR(64) NOT NULL,
  unit_code VARCHAR(64),
  unit_type VARCHAR(32) NOT NULL,
  env_name VARCHAR(32) NOT NULL DEFAULT 'TEST',
  owner_name VARCHAR(128),
  description VARCHAR(512),
  enabled TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_unit_env(unit_name, app_code, env_name),
  INDEX idx_app_code(app_code),
  INDEX idx_unit_type(unit_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE analysis_task (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  deploy_unit_id BIGINT NOT NULL,
  task_name VARCHAR(128) NOT NULL,
  log_type VARCHAR(32) NOT NULL DEFAULT 'APPLICATION',
  status VARCHAR(32) NOT NULL,
  total_lines INT DEFAULT 0,
  error_count INT DEFAULT 0,
  cost_ms BIGINT DEFAULT 0,
  started_at DATETIME,
  finished_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_deploy_unit(deploy_unit_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE log_file (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  task_id BIGINT NOT NULL,
  original_name VARCHAR(255) NOT NULL,
  stored_path VARCHAR(512) NOT NULL,
  file_size BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_task_id(task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE log_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  task_id BIGINT NOT NULL,
  deploy_unit_id BIGINT NOT NULL,
  log_time DATETIME NULL,
  level VARCHAR(16) NOT NULL,
  thread_name VARCHAR(128),
  logger_name VARCHAR(255),
  trace_id VARCHAR(128),
  interface_name VARCHAR(255),
  message TEXT,
  raw_line TEXT NOT NULL,
  line_no INT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_task_level(task_id, level),
  INDEX idx_deploy_unit(deploy_unit_id),
  INDEX idx_trace_id(trace_id),
  INDEX idx_interface(interface_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE exception_event (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  task_id BIGINT NOT NULL,
  deploy_unit_id BIGINT NOT NULL,
  record_id BIGINT,
  event_time DATETIME NULL,
  exception_type VARCHAR(64) NOT NULL,
  classify_score DECIMAL(8,4) DEFAULT 0,
  matched_keywords VARCHAR(512),
  severity VARCHAR(16) NOT NULL,
  trace_id VARCHAR(128),
  interface_name VARCHAR(255),
  title VARCHAR(512) NOT NULL,
  template_text TEXT NOT NULL,
  stack_trace MEDIUMTEXT,
  recommend_id BIGINT,
  recommend_title VARCHAR(255),
  similarity DECIMAL(8,4) DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_deploy_unit(deploy_unit_id),
  INDEX idx_task_type(task_id, exception_type),
  INDEX idx_exception_type(exception_type),
  INDEX idx_template(task_id, exception_type, severity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE exception_template (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  template_hash VARCHAR(64) NOT NULL UNIQUE,
  exception_type VARCHAR(64) NOT NULL,
  template_text TEXT NOT NULL,
  occur_count INT NOT NULL DEFAULT 1,
  first_seen_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  last_seen_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE knowledge_base (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(255) NOT NULL,
  exception_type VARCHAR(64) NOT NULL,
  keywords VARCHAR(512) NOT NULL,
  cause_desc TEXT NOT NULL,
  solution TEXT NOT NULL,
  enabled TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE alert_rule (
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

CREATE TABLE alert_event (
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

CREATE TABLE recommend_feedback (
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

INSERT INTO knowledge_base(title, exception_type, keywords, cause_desc, solution) VALUES
('优惠券库存不足', 'COUPON', 'coupon,stock,inventory,库存,券,发放失败', '活动券库存不足或库存扣减并发冲突。', '检查活动券模板库存、发放限制和库存扣减日志，必要时补充库存后重试。'),
('远程服务调用超时', 'RPC', 'timeout,read timed out,connect timed out,rpc,feign,http', '依赖系统响应超时或网络波动。', '确认依赖服务状态、接口耗时和重试配置，必要时降级或重放失败请求。'),
('数据库执行异常', 'SQL', 'sql,mysql,duplicate,deadlock,DataIntegrityViolation', 'SQL 执行失败、唯一键冲突、死锁或字段约束不满足。', '查看 SQL 错误码、入参和表约束，修复数据后重新执行。'),
('Kafka 消息消费异常', 'KAFKA', 'kafka,consumer,message,offset,deserialize', '消息格式异常或消费处理逻辑失败。', '检查消息体、消费位点和业务幂等记录，必要时修正数据后重新投递。'),
('参数校验失败', 'PARAMETER', 'param,argument,validation,参数,不能为空,invalid', '请求参数缺失、格式错误或业务校验不通过。', '核对接口文档和调用入参，补充必填字段并修正字段格式。');

INSERT INTO deploy_unit(unit_name, app_code, unit_code, unit_type, env_name, owner_name, description) VALUES
('manage', 'IBU-IMP-CORE', '51010', 'SECOND_PARTY_LIB', 'TEST', 'owner-01382761', '国际营销平台管理侧二方库'),
('imp-service-blue', 'IBU-IMP-CORE-OS', '50844', 'DEPLOY_UNIT', 'TEST', 'owner-01382761', '国际营销平台 OS 服务蓝部署单元'),
('admin-web-oss', 'IBU-IMP-CORE', '47545', 'STATIC_RESOURCE', 'TEST', 'owner-01393020', 'OSS 管理端静态资源'),
('imp-backend', 'IBU-IMP-CORE', '44276', 'DEPLOY_UNIT', 'TEST', 'owner-01404276', '国际营销平台后端部署单元'),
('imp-backend-os', 'IBU-IMP-CORE-OS', '43714', 'DEPLOY_UNIT', 'TEST', 'owner-01404276', '国际营销平台 OS 后端部署单元'),
('web-h5', 'IBU-IMP-CORE-OS', '39921', 'DEPLOY_UNIT', 'TEST', 'owner-01382761', '国际营销平台 H5 部署单元'),
('manage', 'IBU-IMP-CORE-OS', '39722', 'SECOND_PARTY_LIB', 'TEST', 'owner-01382761', '国际营销平台 OS 管理侧二方库'),
('admin-web', 'IBU-IMP-CORE-OS', '39721', 'DEPLOY_UNIT', 'TEST', 'owner-01382761', '国际营销平台 OS 管理端部署单元'),
('service', 'IBU-IMP-CORE-OS', '39720', 'DEPLOY_UNIT', 'TEST', 'owner-01382761', '国际营销平台 OS 服务部署单元'),
('activity-h5', 'IBU-IMP-CORE', '36857', 'DEPLOY_UNIT', 'TEST', 'owner-01382761', '活动 H5 部署单元'),
('components', 'IBU-IMP-CORE', '31245', 'SECOND_PARTY_LIB', 'TEST', 'owner-01382761', '国际营销平台公共组件二方库'),
('imp-promotion', 'IBU-IMP-CORE', '22043', 'DEPLOY_UNIT', 'TEST', 'owner-01382761', '国际营销平台促销部署单元'),
('imp-wechat', 'IBU-IMP-CORE', '21343', 'DEPLOY_UNIT', 'TEST', 'owner-01382761', '国际营销平台微信侧部署单元'),
('imp-proxy', 'IBU-IMP-CORE', '21315', 'DEPLOY_UNIT', 'TEST', 'owner-01382761', '国际营销平台代理部署单元'),
('activity', 'IBU-IMP-CORE', '20110', 'DEPLOY_UNIT', 'TEST', 'owner-01382761', '国际营销平台活动部署单元'),
('ibu-imp-core-admin-web', 'IBU-IMP-CORE', '19835', 'DEPLOY_UNIT', 'TEST', 'owner-01382761', '国际营销平台管理端部署单元');

INSERT INTO alert_rule(rule_name, deploy_unit_id, exception_type, interface_name, window_minutes, threshold_count, severity, enabled) VALUES
('单批次任意异常监控', NULL, NULL, NULL, 10, 1, 'MEDIUM', 1),
('RPC 调用异常监控', NULL, 'RPC', NULL, 10, 1, 'HIGH', 1),
('数据库异常监控', NULL, 'SQL', NULL, 10, 1, 'HIGH', 1),
('消息消费异常监控', NULL, 'KAFKA', NULL, 10, 1, 'MEDIUM', 1);
