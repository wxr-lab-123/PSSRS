<template>
  <div class="dashboard-container">
    <div class="welcome-banner">
      <div class="welcome-text">
        <h2>工作台</h2>
        <p>欢迎回来，祝您工作愉快！今天是 {{ currentDate }}</p>
      </div>
      <div class="welcome-actions">
        <el-button type="primary" :icon="Refresh" @click="load" :loading="loading" plain>刷新数据</el-button>
        <el-button 
          @click="autoRefresh = !autoRefresh" 
          :type="autoRefresh ? 'success' : 'default'"
          :icon="autoRefresh ? VideoPlay : VideoPause"
          plain
        >
          {{ autoRefresh ? '自动刷新 (10s)' : '开启自动刷新' }}
        </el-button>
      </div>
    </div>

    <el-row :gutter="24" class="kpi-row">
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card class="kpi-card purple" shadow="hover">
          <div class="kpi-icon">
            <el-icon><Tickets /></el-icon>
          </div>
          <div class="kpi-content">
            <div class="kpi-label">今日挂号</div>
            <div class="kpi-value">{{ data.today_registrations }}</div>
          </div>
          <div class="kpi-trend">
            <span>较昨日</span>
            <span class="trend-up">+5% <el-icon><Top /></el-icon></span>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card class="kpi-card blue" shadow="hover">
          <div class="kpi-icon">
            <el-icon><Timer /></el-icon>
          </div>
          <div class="kpi-content">
            <div class="kpi-label">待就诊</div>
            <div class="kpi-value">{{ data.today_pending_visits }}</div>
          </div>
          <div class="kpi-trend">
            <span>实时数据</span>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card class="kpi-card green" shadow="hover">
          <div class="kpi-icon">
            <el-icon><FirstAidKit /></el-icon>
          </div>
          <div class="kpi-content">
            <div class="kpi-label">医生在线</div>
            <div class="kpi-value">{{ data.online_doctors }}</div>
          </div>
          <div class="kpi-trend">
            <span>在岗率 95%</span>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card class="kpi-card orange" shadow="hover">
          <div class="kpi-icon">
            <el-icon><Money /></el-icon>
          </div>
          <div class="kpi-content">
            <div class="kpi-label">退款申请</div>
            <div class="kpi-value">{{ data.refund_requests }}</div>
          </div>
          <div class="kpi-trend">
            <span>待处理</span>
            <span class="trend-warning" v-if="data.refund_requests > 0">需关注</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="24" class="chart-row">
      <el-col :span="16">
        <el-card class="chart-card" header="挂号趋势 (近7天)">
          <div ref="trendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="chart-card" header="科室挂号占比">
          <div ref="pieChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed, nextTick, watch } from 'vue'
import axios from 'axios'
import { Refresh, VideoPlay, VideoPause, Tickets, Timer, FirstAidKit, Money, Top } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import * as echarts from 'echarts'

const data = ref({ today_registrations: 0, today_pending_visits: 0, online_doctors: 0, refund_requests: 0 })
const loading = ref(false)
const autoRefresh = ref(false)
let timer = null

const trendChartRef = ref(null)
const pieChartRef = ref(null)
let trendChart = null
let pieChart = null

const currentDate = computed(() => dayjs().format('YYYY年MM月DD日 dddd'))

function getBaseURL(){
  const t = (typeof sessionStorage !== 'undefined' ? sessionStorage.getItem('auth_token') : null) || localStorage.getItem('auth_token')
  const base = (window.__API_BASE__ || '') || ''
  return { token: t, base: base }
}

function initCharts() {
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value)
    trendChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', boundaryGap: false, data: [] },
      yAxis: { type: 'value' },
      series: [{ name: '挂号量', type: 'line', smooth: true, areaStyle: {}, data: [], itemStyle: { color: '#8b5cf6' }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(139, 92, 246, 0.5)' }, { offset: 1, color: 'rgba(139, 92, 246, 0.01)' }]) } }]
    })
  }
  
  if (pieChartRef.value) {
    pieChart = echarts.init(pieChartRef.value)
    pieChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: '0%', left: 'center' },
      series: [{
        name: '科室挂号',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
        label: { show: false, position: 'center' },
        emphasis: { label: { show: true, fontSize: 20, fontWeight: 'bold' } },
        labelLine: { show: false },
        data: []
      }]
    })
  }
}

function updateCharts(chartData) {
  if (trendChart && chartData.trend) {
    trendChart.setOption({
      xAxis: { data: chartData.trend.dates || [] },
      series: [{ data: chartData.trend.values || [] }]
    })
  }
  
  if (pieChart && chartData.departmentProportion) {
    pieChart.setOption({
      series: [{ data: chartData.departmentProportion || [] }]
    })
  }
}

async function load(){
  loading.value = true
  try {
    const { token } = getBaseURL()
    
    // 获取KPI数据
    const res = await axios.get('/api/admin/dashboard/summary', { headers: { Authorization: token } })
    if (res?.data?.code === 0 || res?.data?.code === 200) {
      data.value = { ...data.value, ...(res.data.data || {}) }
    }
    
    // 获取图表数据 (模拟或真实接口)
    try {
      const chartRes = await axios.get('/api/admin/dashboard/charts', { headers: { Authorization: token } })
      if (chartRes?.data?.code === 0 || chartRes?.data?.code === 200) {
        updateCharts(chartRes.data.data)
      } else {
        // Fallback mock data if API not ready
        mockChartData()
      }
    } catch (e) {
      // Fallback mock data on error
      mockChartData()
    }
    
  } catch (e) {
    console.warn('加载仪表盘失败', e)
  } finally {
    loading.value = false
  }
}

function mockChartData() {
  const dates = []
  const values = []
  for (let i = 6; i >= 0; i--) {
    dates.push(dayjs().subtract(i, 'day').format('MM-DD'))
    values.push(Math.floor(Math.random() * 50) + 10)
  }
  
  const depts = [
    { value: 1048, name: '内科' },
    { value: 735, name: '外科' },
    { value: 580, name: '儿科' },
    { value: 484, name: '妇产科' },
    { value: 300, name: '其他' }
  ]
  
  updateCharts({
    trend: { dates, values },
    departmentProportion: depts
  })
}

function handleResize() {
  trendChart?.resize()
  pieChart?.resize()
}

onMounted(() => { 
  nextTick(() => {
    initCharts()
    load()
  })
  window.addEventListener('resize', handleResize)
  timer = setInterval(()=>{ if (autoRefresh.value) load() }, 10000) 
})

onBeforeUnmount(() => { 
  if (timer) clearInterval(timer)
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  pieChart?.dispose()
})
</script>

<style scoped>
.dashboard-container {
  min-height: calc(100vh - 100px);
}

.welcome-banner {
  background: linear-gradient(135deg, #fff 0%, #f8fafc 100%);
  padding: 24px 32px;
  border-radius: 16px;
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-border);
}

.welcome-text h2 {
  margin: 0 0 8px;
  color: var(--color-text-primary);
  font-size: 24px;
}

.welcome-text p {
  margin: 0;
  color: var(--color-text-secondary);
}

.welcome-actions {
  display: flex;
  gap: 12px;
}

.kpi-row {
  margin-bottom: 24px;
}

.kpi-card {
  height: 140px;
  border: none;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
}

.kpi-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
}

.kpi-card :deep(.el-card__body) {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 20px !important;
  z-index: 1;
  position: relative;
}

.kpi-icon {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  opacity: 0.15;
}

.kpi-label {
  font-size: 14px;
  color: var(--color-text-secondary);
  font-weight: 500;
}

.kpi-value {
  font-size: 32px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin-top: 4px;
  font-family: 'Inter', sans-serif;
}

.kpi-trend {
  font-size: 12px;
  color: var(--color-text-secondary);
  display: flex;
  align-items: center;
  gap: 8px;
}

/* Color Variants */
.kpi-card.purple .kpi-icon { background: #8b5cf6; color: #8b5cf6; }
.kpi-card.purple .kpi-value { color: #7c3aed; }
.kpi-card.purple::before {
  content: '';
  position: absolute;
  right: -20px;
  top: -20px;
  width: 100px;
  height: 100px;
  background: radial-gradient(circle, rgba(139, 92, 246, 0.1) 0%, transparent 70%);
  border-radius: 50%;
}

.kpi-card.blue .kpi-icon { background: #3b82f6; color: #3b82f6; }
.kpi-card.blue .kpi-value { color: #2563eb; }

.kpi-card.green .kpi-icon { background: #10b981; color: #10b981; }
.kpi-card.green .kpi-value { color: #059669; }

.kpi-card.orange .kpi-icon { background: #f59e0b; color: #f59e0b; }
.kpi-card.orange .kpi-value { color: #d97706; }

.trend-up { color: #10b981; display: flex; align-items: center; }
.trend-warning { color: #f59e0b; font-weight: 600; }

.chart-row {
  margin-bottom: 24px;
}

.chart-card {
  min-height: 400px;
}

.chart-container {
  height: 320px;
  width: 100%;
}

.chart-placeholder {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>


