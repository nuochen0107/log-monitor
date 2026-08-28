# 本地运行手册

## 1. 环境要求

- JDK 8 或 17
- Maven 3.6+
- Node.js 16+
- MySQL 5.7 或 8.0

当前机器检测结果：Java、Node、npm 可用，`mvn` 未在 PATH 中。可以用 IntelliJ IDEA 自带 Maven 打开 `backend/pom.xml`，也可以安装 Maven 后再用命令行启动。

## 2. 初始化数据库

新建数据库时，在 MySQL 中执行：

```sql
source D:/paper/imp-log-monitor/sql/schema.sql;
```

如果使用 Navicat 或 DBeaver，直接打开 `sql/schema.sql` 全量执行即可。

如果已经按旧版本脚本初始化过数据库，不要重建库，改为执行一次增量脚本：

```sql
source D:/paper/imp-log-monitor/sql/upgrade-deploy-unit.sql;
```

如果已经完成部署单元升级，只需要执行第二阶段异常详情升级：

```sql
source D:/paper/imp-log-monitor/sql/upgrade-exception-detail.sql;
```

如果已经完成前两阶段升级，继续执行告警模块升级：

```sql
source D:/paper/imp-log-monitor/sql/upgrade-alert.sql;
```

如果已经完成告警模块升级，继续执行知识库反馈升级：

```sql
source D:/paper/imp-log-monitor/sql/upgrade-feedback.sql;
```

## 3. 修改后端数据库配置

文件：`backend/src/main/resources/application.yml`

默认配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/imp_log_monitor?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: root
```

按你本地 MySQL 账号密码修改。

## 4. 启动后端

命令行方式：

```bash
cd D:/paper/imp-log-monitor/backend
mvn spring-boot:run
```

IDEA 方式：打开 `backend`，运行 `ImpLogMonitorApplication`。

后端地址：`http://localhost:8088`

## 5. 启动前端

```bash
cd D:/paper/imp-log-monitor/frontend
npm install
npm run dev
```

前端地址：`http://localhost:5173`

## 6. 演示流程

1. 打开前端页面。
2. 选择部署单元，例如 `IBU-IMP-CORE-OS / service`。
3. 选择日志类型，默认使用“应用日志”。
4. 点击“上传日志”，选择 `sample-logs/marketing-platform-sample.log`。
5. 查看异常趋势、异常类型分布、部署单元异常排行、TOP 接口、异常事件和知识库推荐。
6. 在异常事件区域按部署单元或异常类型筛选，点击“详情”查看异常模板、异常栈、分类分数和命中关键词。
7. 在异常详情中点击“查看 Trace 链路”，或在 Trace 链路定位区域输入 traceId，查看同一次请求的上下文日志和疑似根因。
8. 在告警中心查看默认告警规则和上传日志后生成的告警事件，可手动新增规则或关闭告警。
9. 在知识库区域新增或编辑处理方案，在异常详情中对推荐方案提交“有效”或“无效”反馈。

## 7. 论文截图建议

- 系统首页看板
- 部署单元选择和异常排行
- 日志上传结果
- 异常事件列表
- 异常详情弹窗
- Trace 链路定位面板
- 告警中心规则和事件列表
- 知识库维护和推荐反馈
- 异常模板列表
- 知识库推荐结果
- 数据库 ER 图
- 系统架构图和核心流程图
