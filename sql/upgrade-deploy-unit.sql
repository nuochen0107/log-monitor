USE imp_log_monitor;

CREATE TABLE IF NOT EXISTS deploy_unit (
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

INSERT IGNORE INTO deploy_unit(unit_name, app_code, unit_code, unit_type, env_name, owner_name, description) VALUES
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

ALTER TABLE analysis_task ADD COLUMN deploy_unit_id BIGINT NOT NULL DEFAULT 1 AFTER id;
ALTER TABLE analysis_task ADD COLUMN log_type VARCHAR(32) NOT NULL DEFAULT 'APPLICATION' AFTER task_name;
ALTER TABLE analysis_task ADD COLUMN cost_ms BIGINT DEFAULT 0 AFTER error_count;
ALTER TABLE analysis_task ADD INDEX idx_deploy_unit(deploy_unit_id);

ALTER TABLE log_record ADD COLUMN deploy_unit_id BIGINT NOT NULL DEFAULT 1 AFTER task_id;
ALTER TABLE log_record ADD INDEX idx_deploy_unit(deploy_unit_id);

ALTER TABLE exception_event ADD COLUMN deploy_unit_id BIGINT NOT NULL DEFAULT 1 AFTER task_id;
ALTER TABLE exception_event ADD COLUMN classify_score DECIMAL(8,4) DEFAULT 0 AFTER exception_type;
ALTER TABLE exception_event ADD COLUMN matched_keywords VARCHAR(512) AFTER classify_score;
ALTER TABLE exception_event ADD INDEX idx_deploy_unit(deploy_unit_id);
ALTER TABLE exception_event ADD INDEX idx_exception_type(exception_type);
