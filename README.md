# imp-log-monitor

面向国际营销平台的业务日志监控与异常分析系统，本地开发版。

## 功能

- 上传 `.log` / `.txt` 日志文件
- 解析时间、级别、线程、类名、traceId、接口、消息内容
- 合并 Java 异常栈并生成异常事件
- 按 SQL、RPC、Redis、Kafka、Job、参数、库存、优惠券等类型分类
- 抽取异常模板并统计出现次数
- 根据模板相似度推荐知识库处理方案
- 提供趋势、类型分布、TOP 接口、TOP 异常模板看板

## 本地启动

1. 创建数据库并执行 `sql/schema.sql`。
2. 修改 `backend/src/main/resources/application.yml` 中的 MySQL 账号密码。
3. 启动后端：

```bash
cd backend
mvn spring-boot:run
```

4. 启动前端：

```bash
cd frontend
npm install
npm run dev
```

5. 上传 `sample-logs/marketing-platform-sample.log` 查看分析结果。


