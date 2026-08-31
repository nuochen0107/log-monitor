<template>
  <main class="app-shell">
    <section class="topbar">
      <div>
        <h1>国际营销平台日志监控</h1>
        <p>多部署单元日志导入、异常识别、模板提取和知识库推荐</p>
      </div>
      <div class="upload-tools">
        <el-select v-model="selectedDeployUnitId" placeholder="选择部署单元" filterable class="unit-select">
          <el-option
            v-for="unit in deployUnits"
            :key="unit.id"
            :label="`${unit.appCode} / ${unit.unitName}`"
            :value="unit.id"
          >
            <span>{{ unit.appCode }} / {{ unit.unitName }}</span>
            <small>{{ unit.unitType }}</small>
          </el-option>
        </el-select>
        <el-select v-model="selectedLogType" class="log-type-select">
          <el-option label="应用日志" value="APPLICATION" />
          <el-option label="访问日志" value="ACCESS" />
          <el-option label="任务日志" value="JOB" />
          <el-option label="消费日志" value="CONSUMER" />
        </el-select>
        <el-upload :auto-upload="false" :show-file-list="false" :on-change="handleFileChange" accept=".log,.txt">
          <el-button type="primary">上传日志</el-button>
        </el-upload>
      </div>
    </section>

    <section class="summary-grid">
      <div class="metric"><span>分析任务</span><strong>{{ summary.taskCount || 0 }}</strong></div>
      <div class="metric"><span>日志总数</span><strong>{{ summary.logCount || 0 }}</strong></div>
      <div class="metric"><span>异常事件</span><strong>{{ summary.exceptionCount || 0 }}</strong></div>
      <div class="metric"><span>异常模板</span><strong>{{ summary.templateCount || 0 }}</strong></div>
      <div class="metric"><span>告警事件</span><strong>{{ summary.alertCount || 0 }}</strong></div>
      <div class="metric"><span>推荐反馈</span><strong>{{ summary.feedbackCount || 0 }}</strong></div>
    </section>

    <section class="panel-grid">
      <div class="panel"><div ref="trendRef" class="chart"></div></div>
      <div class="panel"><div ref="typeRef" class="chart"></div></div>
    </section>

    <section class="panel task-panel">
      <div class="panel-head">
        <h2>分析任务</h2>
        <div class="task-filter">
          <el-select v-model="taskFilters.deployUnitId" placeholder="全部部署单元" clearable filterable>
            <el-option
              v-for="unit in deployUnits"
              :key="unit.id"
              :label="`${unit.appCode} / ${unit.unitName}`"
              :value="unit.id"
            />
          </el-select>
          <el-button type="primary" plain @click="loadTasks">查询</el-button>
        </div>
      </div>
      <el-table :data="tasks" size="small" max-height="300">
        <el-table-column prop="id" label="任务ID" width="90" />
        <el-table-column prop="taskName" label="文件名" min-width="220" show-overflow-tooltip />
        <el-table-column prop="logType" label="日志类型" width="100" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'SUCCESS' ? 'success' : row.status === 'FAILED' ? 'danger' : 'warning'" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalLines" label="总行数" width="90" />
        <el-table-column prop="errorCount" label="异常数" width="90" />
        <el-table-column prop="costMs" label="耗时ms" width="100" />
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="filterByTask(row)">查看异常</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <div class="workbench-layout">
      <div class="workbench-main">
        <section class="panel exception-panel">
        <div class="panel-head"><h2>异常事件</h2><el-button text @click="loadAll">刷新</el-button></div>
        <div class="filter-bar">
          <el-select v-model="eventFilters.deployUnitId" placeholder="全部部署单元" clearable filterable class="filter-select">
            <el-option
              v-for="unit in deployUnits"
              :key="unit.id"
              :label="`${unit.appCode} / ${unit.unitName}`"
              :value="unit.id"
            />
          </el-select>
          <el-select v-model="eventFilters.type" placeholder="全部类型" clearable class="type-filter-select">
            <el-option v-for="type in exceptionTypes" :key="type" :label="type" :value="type" />
          </el-select>
          <el-button type="primary" plain @click="queryEvents">查询</el-button>
        </div>
        <div v-if="activeTaskFilter" class="active-filter">
          <span>当前仅查看任务 #{{ activeTaskFilter.id }} 的异常</span>
          <el-button text type="primary" @click="clearTaskFilter">清除</el-button>
        </div>
        <el-table :data="events" max-height="360" size="small" empty-text="暂无异常事件">
          <el-table-column prop="eventTime" label="时间" width="170" />
          <el-table-column prop="exceptionType" label="类型" width="100" />
          <el-table-column prop="classifyScore" label="分数" width="80" />
          <el-table-column prop="severity" label="级别" width="80" />
          <el-table-column prop="interfaceName" label="接口" width="220" />
          <el-table-column prop="title" label="异常摘要" min-width="320" show-overflow-tooltip />
          <el-table-column prop="recommendTitle" label="推荐方案" width="180" />
          <el-table-column label="操作" width="90" fixed="right">
            <template #default="{ row }">
              <el-button text type="primary" @click="openEventDetail(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
        </section>

        <section class="panel trace-panel">
          <div class="panel-head">
            <h2>Trace 链路定位</h2>
            <div class="trace-search">
              <el-input v-model="traceKeyword" placeholder="输入 traceId" clearable />
              <el-button type="primary" plain @click="loadTraceChain(traceKeyword)">查询</el-button>
            </div>
          </div>
          <div v-if="traceChain" class="trace-summary">
            <span>日志 {{ traceChain.logCount || 0 }} 条</span>
            <span>异常 {{ traceChain.exceptionCount || 0 }} 条</span>
            <strong>{{ traceChain.rootSummary }}</strong>
          </div>
          <el-table :data="traceChain?.records || []" size="small" max-height="320" row-key="id">
            <el-table-column prop="logTime" label="时间" width="170" />
            <el-table-column prop="level" label="级别" width="80">
              <template #default="{ row }">
                <el-tag :type="row.level === 'ERROR' ? 'danger' : row.level === 'WARN' ? 'warning' : 'info'" size="small">{{ row.level }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="interfaceName" label="接口" width="220" />
            <el-table-column prop="loggerName" label="类名" width="260" show-overflow-tooltip />
            <el-table-column prop="message" label="日志内容" min-width="360" show-overflow-tooltip />
          </el-table>
        </section>

        <section class="panel alert-panel">
          <div class="panel-head"><h2>告警中心</h2><el-button text @click="loadAlerts">刷新</el-button></div>
          <div class="alert-layout">
            <div>
              <div class="sub-head">新建告警规则</div>
              <el-form label-position="top" class="alert-editor">
                <el-form-item label="规则名称">
                  <el-input v-model="alertRuleForm.ruleName" placeholder="例如：RPC 超时告警" />
                </el-form-item>
                <el-form-item label="适用部署单元">
                  <el-select v-model="alertRuleForm.deployUnitId" placeholder="全部部署单元" clearable filterable>
                    <el-option
                      v-for="unit in deployUnits"
                      :key="unit.id"
                      :label="`${unit.appCode} / ${unit.unitName}`"
                      :value="unit.id"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="异常类型">
                  <el-select v-model="alertRuleForm.exceptionType" placeholder="任意异常类型" clearable>
                    <el-option v-for="type in exceptionTypes" :key="type" :label="type" :value="type" />
                  </el-select>
                </el-form-item>
                <el-form-item label="触发次数">
                  <el-input-number v-model="alertRuleForm.thresholdCount" :min="1" :max="100" controls-position="right" />
                </el-form-item>
                <el-form-item label="告警级别">
                  <el-select v-model="alertRuleForm.severity">
                    <el-option label="高" value="HIGH" />
                    <el-option label="中" value="MEDIUM" />
                    <el-option label="低" value="LOW" />
                  </el-select>
                </el-form-item>
                <el-button type="primary" @click="saveRule">保存规则</el-button>
              </el-form>
            </div>
            <div>
              <div class="sub-head">已配置规则</div>
              <el-table :data="alertRules" size="small" max-height="250" empty-text="暂无告警规则">
                <el-table-column prop="ruleName" label="规则" min-width="160" show-overflow-tooltip />
                <el-table-column label="条件" min-width="260" show-overflow-tooltip>
                  <template #default="{ row }">{{ formatRule(row) }}</template>
                </el-table-column>
                <el-table-column label="级别" width="80">
                  <template #default="{ row }">
                    <el-tag :type="severityTag(row.severity)" size="small">{{ severityText(row.severity) }}</el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>

          <div class="sub-head alert-event-head">告警事件</div>
          <el-table :data="alertEvents" size="small" max-height="300" empty-text="暂无告警事件">
            <el-table-column prop="createdAt" label="时间" width="170" />
            <el-table-column prop="severity" label="级别" width="90">
              <template #default="{ row }">
                <el-tag :type="severityTag(row.severity)" size="small">{{ severityText(row.severity) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 'CLOSED' ? 'info' : 'danger'" size="small">{{ row.status === 'CLOSED' ? '已关闭' : '待处理' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="exceptionType" label="类型" width="110" />
            <el-table-column prop="triggerCount" label="次数" width="80" />
            <el-table-column prop="alertTitle" label="告警内容" min-width="320" show-overflow-tooltip />
            <el-table-column prop="closedAt" label="关闭时间" width="170" />
            <el-table-column label="操作" width="90" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.status !== 'CLOSED'" text type="primary" @click="closeAlert(row)">关闭</el-button>
                <span v-else class="closed-text">已处理</span>
              </template>
            </el-table-column>
          </el-table>
        </section>

        <section class="panel knowledge-panel">
          <div class="panel-head"><h2>知识库</h2><el-button type="primary" plain @click="openKnowledgeEditor()">新增知识</el-button></div>
          <el-table :data="knowledge" size="small">
            <el-table-column prop="exceptionType" label="类型" width="120" />
            <el-table-column prop="title" label="标题" width="180" />
            <el-table-column prop="keywords" label="关键词" min-width="260" />
            <el-table-column prop="solution" label="处理建议" min-width="360" show-overflow-tooltip />
            <el-table-column prop="enabled" label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.enabled === 1 ? 'success' : 'info'" size="small">{{ row.enabled === 1 ? '启用' : '停用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="90" fixed="right">
              <template #default="{ row }">
                <el-button text type="primary" @click="openKnowledgeEditor(row)">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
        </section>
      </div>

      <section class="panel side-panel">
        <div class="panel-head"><h2>部署单元异常排行</h2></div>
        <div v-for="item in deployUnitRanking" :key="item.deployUnitId" class="rank-row">
          <span>{{ item.appCode }} / {{ item.unitName }}</span><strong>{{ item.value }}</strong>
        </div>

        <div class="panel-head"><h2>TOP 接口</h2></div>
        <div v-for="item in topInterfaces" :key="item.name" class="rank-row">
          <span>{{ item.name }}</span><strong>{{ item.value }}</strong>
        </div>

        <div class="panel-head compact"><h2>异常模板</h2></div>
        <div v-for="item in templates" :key="item.id" class="template-row">
          <strong>{{ item.exceptionType }} · {{ item.occurCount }}</strong>
          <p>{{ item.templateText }}</p>
        </div>
      </section>
    </div>

    <el-dialog v-model="eventDetailVisible" title="异常详情" width="780px">
      <div v-if="selectedEvent" class="event-detail">
        <dl>
          <dt>异常类型</dt><dd>{{ selectedEvent.exceptionType }}</dd>
          <dt>分类分数</dt><dd>{{ selectedEvent.classifyScore || 0 }}</dd>
          <dt>命中关键词</dt><dd>{{ selectedEvent.matchedKeywords || '-' }}</dd>
          <dt>TraceId</dt><dd>{{ selectedEvent.traceId || '-' }}</dd>
          <dt>接口</dt><dd>{{ selectedEvent.interfaceName || '-' }}</dd>
          <dt>推荐方案</dt><dd>{{ selectedEvent.recommendTitle || '未命中知识库' }}</dd>
        </dl>
        <h3>异常模板</h3>
        <pre>{{ selectedEvent.templateText }}</pre>
        <h3>异常栈</h3>
        <pre>{{ selectedEvent.stackTrace }}</pre>
        <div class="dialog-actions">
          <el-button plain :disabled="!selectedEvent.recommendId" @click="submitFeedback(1)">推荐有效</el-button>
          <el-button plain :disabled="!selectedEvent.recommendId" @click="submitFeedback(0)">推荐无效</el-button>
          <el-button type="primary" plain :disabled="!selectedEvent.traceId" @click="loadTraceFromDetail">查看 Trace 链路</el-button>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="knowledgeEditorVisible" title="知识库维护" width="720px">
      <div class="knowledge-form">
        <el-input v-model="knowledgeForm.title" placeholder="标题" />
        <el-select v-model="knowledgeForm.exceptionType" placeholder="异常类型">
          <el-option v-for="type in exceptionTypes" :key="type" :label="type" :value="type" />
        </el-select>
        <el-input v-model="knowledgeForm.keywords" placeholder="关键词，使用逗号分隔" />
        <el-input v-model="knowledgeForm.causeDesc" type="textarea" :rows="3" placeholder="原因说明" />
        <el-input v-model="knowledgeForm.solution" type="textarea" :rows="4" placeholder="处理方案" />
        <el-switch v-model="knowledgeEnabled" active-text="启用" inactive-text="停用" />
      </div>
      <template #footer>
        <el-button @click="knowledgeEditorVisible = false">取消</el-button>
        <el-button type="primary" @click="submitKnowledge">保存</el-button>
      </template>
    </el-dialog>
  </main>
</template>

<script setup>
import { nextTick, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { closeAlertEvent, getAlertEvents, getAlertRules, getDeployUnitRanking, getDeployUnits, getExceptionEvents, getKnowledge, getSummary, getTasks, getTopInterfaces, getTopTemplates, getTraceChain, getTrend, getTypeDistribution, saveAlertRule, saveFeedback, saveKnowledge, uploadLog } from './api'

const summary = ref({})
const events = ref([])
const knowledge = ref([])
const tasks = ref([])
const templates = ref([])
const topInterfaces = ref([])
const alertEvents = ref([])
const alertRules = ref([])
const deployUnits = ref([])
const deployUnitRanking = ref([])
const selectedDeployUnitId = ref(null)
const selectedLogType = ref('APPLICATION')
const exceptionTypes = ['COUPON', 'RPC', 'SQL', 'KAFKA', 'JOB', 'REDIS', 'PARAMETER', 'SYSTEM']
const eventFilters = ref({
  deployUnitId: null,
  taskId: null,
  type: ''
})
const taskFilters = ref({
  deployUnitId: null
})
const eventDetailVisible = ref(false)
const selectedEvent = ref(null)
const activeTaskFilter = ref(null)
const traceKeyword = ref('')
const traceChain = ref(null)
const alertRuleForm = ref({
  ruleName: '',
  deployUnitId: null,
  exceptionType: '',
  thresholdCount: 1,
  severity: 'MEDIUM',
  windowMinutes: 10,
  enabled: 1
})
const knowledgeEditorVisible = ref(false)
const knowledgeEnabled = ref(true)
const knowledgeForm = ref(newKnowledgeForm())
const trendRef = ref(null)
const typeRef = ref(null)
let trendChart
let typeChart

async function handleFileChange(uploadFile) {
  if (!selectedDeployUnitId.value) {
    ElMessage.warning('请先选择部署单元')
    return
  }
  const result = await uploadLog(uploadFile.raw, selectedDeployUnitId.value, selectedLogType.value)
  if (result.success) {
    ElMessage.success('日志分析完成')
    await loadAll()
  } else {
    ElMessage.error(result.message || '上传失败')
  }
}

async function loadAll() {
  deployUnits.value = await getDeployUnits({ enabled: 1 })
  if (!selectedDeployUnitId.value && deployUnits.value.length > 0) {
    selectedDeployUnitId.value = deployUnits.value[0].id
  }
  summary.value = await getSummary()
  await loadTasks()
  await loadEvents()
  await loadAlerts()
  knowledge.value = await getKnowledge()
  templates.value = await getTopTemplates()
  deployUnitRanking.value = await getDeployUnitRanking()
  const interfaces = await getTopInterfaces()
  topInterfaces.value = Object.entries(interfaces).map(([name, value]) => ({ name, value }))
  await renderCharts()
}

async function loadTasks() {
  const params = {}
  if (taskFilters.value.deployUnitId) {
    params.deployUnitId = taskFilters.value.deployUnitId
  }
  tasks.value = await getTasks(params)
}

async function filterByTask(task) {
  eventFilters.value.taskId = task.id
  eventFilters.value.deployUnitId = task.deployUnitId
  activeTaskFilter.value = task
  await loadEvents()
  ElMessage.success(`已筛选任务 #${task.id} 的异常事件`)
}

async function clearTaskFilter() {
  eventFilters.value.taskId = null
  activeTaskFilter.value = null
  await loadEvents()
}

async function queryEvents() {
  eventFilters.value.taskId = null
  activeTaskFilter.value = null
  await loadEvents()
}

async function loadAlerts() {
  alertRules.value = await getAlertRules()
  const alertPage = await getAlertEvents({ page: 1, size: 50 })
  alertEvents.value = alertPage.records || []
}

async function saveRule() {
  if (!alertRuleForm.value.ruleName) {
    ElMessage.warning('请输入规则名称')
    return
  }
  await saveAlertRule(alertRuleForm.value)
  ElMessage.success('告警规则已保存')
  alertRuleForm.value = {
    ruleName: '',
    deployUnitId: null,
    exceptionType: '',
    thresholdCount: 1,
    severity: 'MEDIUM',
    windowMinutes: 10,
    enabled: 1
  }
  await loadAlerts()
}

async function closeAlert(row) {
  await closeAlertEvent(row.id)
  ElMessage.success('告警已关闭')
  await loadAlerts()
  summary.value = await getSummary()
}

function formatRule(rule) {
  const unit = rule.deployUnitId ? getDeployUnitName(rule.deployUnitId) : '全部部署单元'
  const type = rule.exceptionType || '任意异常'
  const threshold = rule.thresholdCount || 1
  return `${unit}，单次分析任务内 ${type} 达到 ${threshold} 次触发`
}

function getDeployUnitName(id) {
  const unit = deployUnits.value.find(item => item.id === id)
  return unit ? `${unit.appCode} / ${unit.unitName}` : `部署单元 #${id}`
}

function severityTag(severity) {
  if (severity === 'HIGH') return 'danger'
  if (severity === 'MEDIUM') return 'warning'
  return 'info'
}

function severityText(severity) {
  if (severity === 'HIGH') return '高'
  if (severity === 'MEDIUM') return '中'
  if (severity === 'LOW') return '低'
  return severity || '-'
}

function newKnowledgeForm() {
  return {
    id: null,
    title: '',
    exceptionType: '',
    keywords: '',
    causeDesc: '',
    solution: '',
    enabled: 1
  }
}

function openKnowledgeEditor(row) {
  knowledgeForm.value = row ? { ...row } : newKnowledgeForm()
  knowledgeEnabled.value = knowledgeForm.value.enabled !== 0
  knowledgeEditorVisible.value = true
}

async function submitKnowledge() {
  if (!knowledgeForm.value.title || !knowledgeForm.value.exceptionType || !knowledgeForm.value.keywords) {
    ElMessage.warning('请填写标题、异常类型和关键词')
    return
  }
  knowledgeForm.value.enabled = knowledgeEnabled.value ? 1 : 0
  await saveKnowledge(knowledgeForm.value)
  ElMessage.success('知识库已保存')
  knowledgeEditorVisible.value = false
  knowledge.value = await getKnowledge()
}

async function submitFeedback(useful) {
  await saveFeedback({
    eventId: selectedEvent.value.id,
    knowledgeId: selectedEvent.value.recommendId,
    useful,
    feedbackText: useful ? '推荐方案有效' : '推荐方案无效，需要补充知识库'
  })
  ElMessage.success('反馈已记录')
  summary.value = await getSummary()
}

async function loadEvents() {
  const params = { page: 1, size: 50 }
  if (eventFilters.value.deployUnitId) {
    params.deployUnitId = eventFilters.value.deployUnitId
  }
  if (eventFilters.value.taskId) {
    params.taskId = eventFilters.value.taskId
  }
  if (eventFilters.value.type) {
    params.type = eventFilters.value.type
  }
  const eventPage = await getExceptionEvents(params)
  events.value = eventPage.records || []
}

function openEventDetail(event) {
  selectedEvent.value = event
  eventDetailVisible.value = true
}

async function loadTraceChain(traceId) {
  if (!traceId) {
    ElMessage.warning('请输入 traceId')
    return
  }
  traceChain.value = await getTraceChain(traceId)
  if (!traceChain.value.records || traceChain.value.records.length === 0) {
    ElMessage.warning('未查询到链路日志')
  }
}

async function loadTraceFromDetail() {
  traceKeyword.value = selectedEvent.value.traceId
  eventDetailVisible.value = false
  await loadTraceChain(traceKeyword.value)
}

async function renderCharts() {
  const trend = await getTrend()
  const types = await getTypeDistribution()
  await nextTick()
  trendChart = trendChart || echarts.init(trendRef.value)
  typeChart = typeChart || echarts.init(typeRef.value)
  trendChart.setOption({
    title: { text: '异常趋势', left: 8, top: 6, textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'axis' },
    grid: { left: 44, right: 20, top: 54, bottom: 34 },
    xAxis: { type: 'category', data: Object.keys(trend) },
    yAxis: { type: 'value' },
    series: [{ type: 'line', smooth: true, data: Object.values(trend), areaStyle: {} }]
  })
  typeChart.setOption({
    title: { text: '异常类型分布', left: 8, top: 6, textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'item' },
    series: [{ type: 'pie', radius: ['42%', '70%'], center: ['50%', '56%'], data: Object.entries(types).map(([name, value]) => ({ name, value })) }]
  })
}

onMounted(loadAll)
</script>
