import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

export async function uploadLog(file, deployUnitId, logType = 'APPLICATION') {
  const form = new FormData()
  form.append('file', file)
  form.append('deployUnitId', deployUnitId)
  form.append('logType', logType)
  const { data } = await request.post('/upload/log', form)
  return data
}

export async function getDeployUnits(params = {}) {
  const { data } = await request.get('/deploy-units', { params })
  return data.data
}

export async function getSummary() {
  const { data } = await request.get('/dashboard/summary')
  return data.data
}

export async function getTrend() {
  const { data } = await request.get('/dashboard/trend')
  return data.data
}

export async function getTypeDistribution() {
  const { data } = await request.get('/dashboard/type-distribution')
  return data.data
}

export async function getTopInterfaces() {
  const { data } = await request.get('/dashboard/top-interfaces')
  return data.data
}

export async function getTopTemplates() {
  const { data } = await request.get('/dashboard/top-templates')
  return data.data
}

export async function getDeployUnitRanking() {
  const { data } = await request.get('/dashboard/deploy-unit-ranking')
  return data.data
}

export async function getTasks(params = {}) {
  const { data } = await request.get('/logs/tasks', { params })
  return data.data
}

export async function getExceptionEvents(params = {}) {
  const { data } = await request.get('/exceptions/events', { params })
  return data.data
}

export async function getTraceChain(traceId) {
  const { data } = await request.get('/logs/trace', { params: { traceId } })
  return data.data
}

export async function getAlertRules(params = {}) {
  const { data } = await request.get('/alerts/rules', { params })
  return data.data
}

export async function saveAlertRule(payload) {
  const { data } = await request.post('/alerts/rules', payload)
  return data.data
}

export async function getAlertEvents(params = {}) {
  const { data } = await request.get('/alerts/events', { params })
  return data.data
}

export async function closeAlertEvent(id) {
  const { data } = await request.post(`/alerts/events/${id}/close`)
  return data.data
}

export async function getKnowledge() {
  const { data } = await request.get('/knowledge')
  return data.data
}

export async function saveKnowledge(payload) {
  const { data } = await request.post('/knowledge', payload)
  return data.data
}

export async function saveFeedback(payload) {
  const { data } = await request.post('/feedback', payload)
  return data.data
}
