<template>
  <div class="dashboard">
    <div class="page-header">
      <h2>工作台</h2>
      <p>
        欢迎回来，{{ userStore.userInfo?.nickname || '管理员' }}
        <template v-if="pendingFeedback > 0">
          <span class="divider">·</span>
          <span class="highlight">{{ pendingFeedback }}</span> 条反馈待处理
        </template>
      </p>
    </div>

    <!-- 指标卡 -->
    <div class="stat-row">
      <div class="stat-card">
        <div class="stat-icon blue">
          <el-icon :size="22"><Document /></el-icon>
        </div>
        <div class="stat-meta">
          <span class="stat-title">题目总数</span>
          <span class="stat-value">{{ animatedStats.questionCount }}</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon cyan">
          <el-icon :size="22"><Folder /></el-icon>
        </div>
        <div class="stat-meta">
          <span class="stat-title">分类数量</span>
          <span class="stat-value">{{ animatedStats.categoryCount }}</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon green">
          <el-icon :size="22"><User /></el-icon>
        </div>
        <div class="stat-meta">
          <span class="stat-title">用户数量</span>
          <span class="stat-value">{{ animatedStats.userCount }}</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon red">
          <el-icon :size="22"><ChatDotRound /></el-icon>
          <span v-if="pendingFeedback > 0" class="stat-badge">{{ pendingFeedback }}</span>
        </div>
        <div class="stat-meta">
          <span class="stat-title">待处理反馈</span>
          <span class="stat-value">{{ animatedStats.pendingFeedback }}</span>
        </div>
      </div>
    </div>

    <!-- 主体两栏 -->
    <el-row :gutter="16">
      <el-col :xs="24" :lg="14">
        <el-card class="content-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>数据概览</span>
            </div>
          </template>
          <div class="charts-wrapper">
            <div class="chart-section">
              <div class="chart-title">题目难度分布</div>
              <div ref="difficultyChart" class="chart chart-pie"></div>
            </div>
            <div class="chart-section">
              <div class="chart-title">题目分类分布（Top 10）</div>
              <div ref="categoryChart" class="chart chart-bar"></div>
              <div class="chart-footer" @click="$router.push('/question/category')">
                查看全部分类 →
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="10">
        <el-card class="content-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>用户概览</span>
            </div>
          </template>
          <div class="user-panel">
            <div class="user-stat-item">
              <div class="user-stat-head">
                <span class="dot active"></span>
                <span class="user-stat-label">活跃用户</span>
                <span class="user-stat-value">{{ userStats.activeCount }}</span>
              </div>
              <el-progress
                :percentage="getPercentage(userStats.activeCount, stats.userCount)"
                :stroke-width="8"
                color="#10B981"
                :show-text="false"
              />
            </div>
            <div class="user-stat-item">
              <div class="user-stat-head">
                <span class="dot disabled"></span>
                <span class="user-stat-label">禁用用户</span>
                <span class="user-stat-value">{{ userStats.disabledCount }}</span>
              </div>
              <el-progress
                :percentage="getPercentage(userStats.disabledCount, stats.userCount)"
                :stroke-width="8"
                color="#EF4444"
                :show-text="false"
              />
            </div>
            <div class="user-stat-summary">
              <div class="summary-item">
                <span class="summary-label">正常状态</span>
                <span class="summary-value">{{ getPercentage(userStats.activeCount, stats.userCount) }}%</span>
              </div>
              <div class="summary-item">
                <span class="summary-label">启用题目</span>
                <span class="summary-value">{{ animatedStats.enabledCount }}</span>
              </div>
            </div>
          </div>
          <div class="quick-actions">
            <el-button type="primary" @click="$router.push('/question/add')">
              <el-icon><Plus /></el-icon>
              添加题目
            </el-button>
            <el-button @click="$router.push('/user/list')">
              <el-icon><User /></el-icon>
              用户管理
            </el-button>
            <el-button @click="$router.push('/user/feedback')">
              <el-icon><ChatDotRound /></el-icon>
              反馈管理
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useUserStore } from '../stores/user'
import api from '../utils/api'
import axios from 'axios'
import { Document, Folder, User, ChatDotRound, Plus } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const userStore = useUserStore()
const difficultyChart = ref(null)
const categoryChart = ref(null)
let difficultyChartInstance = null
let categoryChartInstance = null

const userUrl = import.meta.env.VITE_USER_URL || 'http://localhost:8200'

const stats = ref({
  questionCount: 0,
  categoryCount: 0,
  userCount: 0,
  enabledCount: 0
})

const animatedStats = ref({
  questionCount: 0,
  categoryCount: 0,
  userCount: 0,
  enabledCount: 0,
  pendingFeedback: 0
})

const pendingFeedback = ref(0)

const questionStats = ref({
  easyCount: 0,
  mediumCount: 0,
  hardCount: 0,
  difficultyStats: {}
})

const userStats = ref({
  activeCount: 0,
  disabledCount: 0
})

const categoryStats = ref([])

const getPercentage = (value, total) => {
  if (!total) return 0
  return Math.round((value / total) * 100)
}

const initDifficultyChart = () => {
  if (!difficultyChart.value) return

  if (difficultyChartInstance) {
    difficultyChartInstance.dispose()
  }

  difficultyChartInstance = echarts.init(difficultyChart.value)

  const difficultyStats = questionStats.value.difficultyStats || {}
  const colorMap = { '简单': '#10B981', '中等': '#1E40AF', '困难': '#EF4444' }

  const chartData = Object.entries(difficultyStats).map(([name, value]) => ({
    name,
    value: value || 0,
    itemStyle: { color: colorMap[name] || '#94A3B8' }
  }))

  if (chartData.length === 0) return

  difficultyChartInstance.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} 题 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '8%',
      top: 'center',
      itemWidth: 10,
      itemHeight: 10,
      textStyle: { color: '#64748B', fontSize: 13 }
    },
    series: [
      {
        name: '题目难度',
        type: 'pie',
        radius: ['0%', '62%'],
        center: ['35%', '50%'],
        data: chartData,
        label: { show: false },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(15, 23, 42, 0.2)'
          }
        }
      }
    ]
  })
}

const initCategoryChart = () => {
  if (!categoryChart.value) return

  if (categoryChartInstance) {
    categoryChartInstance.dispose()
  }

  categoryChartInstance = echarts.init(categoryChart.value)

  const top10 = categoryStats.value.slice(0, 10)
  const names = top10.map(item => item.categoryName)
  const counts = top10.map(item => item.questionCount)

  categoryChartInstance.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: '{b}: {c} 题'
    },
    grid: {
      left: '3%',
      right: '12%',
      top: '3%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      axisLabel: { show: false },
      splitLine: { show: false },
      axisLine: { show: false },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'category',
      data: names,
      inverse: true,
      axisLabel: { color: '#0F172A', fontSize: 13 },
      axisLine: { show: false },
      axisTick: { show: false }
    },
    series: [
      {
        name: '题目数量',
        type: 'bar',
        data: counts,
        barWidth: 14,
        itemStyle: {
          borderRadius: [0, 7, 7, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#1E40AF' },
            { offset: 1, color: '#3B82F6' }
          ])
        },
        label: {
          show: true,
          position: 'right',
          color: '#64748B',
          fontSize: 12
        }
      }
    ]
  })
}

const animateNumber = (target, key, endValue) => {
  const duration = 800
  const startTime = Date.now()

  const animate = () => {
    const elapsed = Date.now() - startTime
    const progress = Math.min(elapsed / duration, 1)
    const easeProgress = 1 - Math.pow(1 - progress, 3)
    target[key] = Math.round(endValue * easeProgress)

    if (progress < 1) {
      requestAnimationFrame(animate)
    }
  }

  animate()
}

onMounted(async () => {
  try {
    const [overviewRes, questionRes, userRes, categoryRes] = await Promise.all([
      api.get('/admin/dashboard/overview'),
      api.get('/admin/dashboard/question-stats'),
      api.get('/admin/dashboard/user-stats'),
      api.get('/admin/dashboard/category-stats')
    ])

    stats.value = overviewRes.data
    stats.value.enabledCount = overviewRes.data.enabledCount || 0
    questionStats.value = questionRes.data
    userStats.value = userRes.data
    categoryStats.value = categoryRes.data || []

    // 待处理反馈数（直连用户服务，需携带后台登录令牌）
    try {
      const fbRes = await axios.get(`${userUrl}/feedback/page`, {
        params: { pageNum: 1, pageSize: 1, status: 0 },
        headers: { Authorization: `Bearer ${localStorage.getItem('admin_token') || ''}` }
      })
      pendingFeedback.value = fbRes.data?.data?.total || 0
    } catch (e) {
      pendingFeedback.value = 0
    }

    animateNumber(animatedStats.value, 'questionCount', stats.value.questionCount)
    animateNumber(animatedStats.value, 'categoryCount', stats.value.categoryCount)
    animateNumber(animatedStats.value, 'userCount', stats.value.userCount)
    animateNumber(animatedStats.value, 'enabledCount', stats.value.enabledCount)
    animateNumber(animatedStats.value, 'pendingFeedback', pendingFeedback.value)

    await nextTick()
    initDifficultyChart()
    initCategoryChart()
  } catch (error) {
    console.error(error)
  }
})

window.addEventListener('resize', () => {
  if (difficultyChartInstance) {
    difficultyChartInstance.resize()
  }
  if (categoryChartInstance) {
    categoryChartInstance.resize()
  }
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 600;
  line-height: 1.3;
  color: var(--color-text);
  margin: 0 0 8px 0;
}

.page-header p {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin: 0;
}

.page-header .divider {
  margin: 0 6px;
  color: var(--color-border);
}

.page-header .highlight {
  color: var(--color-error);
  font-weight: 500;
}

/* ===== 指标卡 ===== */
.stat-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

@media (max-width: 992px) {
  .stat-row {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 576px) {
  .stat-row {
    grid-template-columns: 1fr;
  }
}

.stat-card {
  background: var(--color-card);
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-light);
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: box-shadow 0.2s ease, transform 0.2s ease;
}

.stat-card:hover {
  box-shadow: var(--shadow-medium);
  transform: translateY(-2px);
}

.stat-icon {
  position: relative;
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon.blue { background: rgba(30, 64, 175, 0.08); color: #1E40AF; }
.stat-icon.cyan { background: rgba(14, 165, 233, 0.08); color: #0EA5E9; }
.stat-icon.green { background: rgba(16, 185, 129, 0.08); color: #10B981; }
.stat-icon.red { background: rgba(239, 68, 68, 0.08); color: #EF4444; }

.stat-badge {
  position: absolute;
  top: -6px;
  right: -6px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 9px;
  background: var(--color-error);
  color: #fff;
  font-size: 11px;
  line-height: 18px;
  text-align: center;
}

.stat-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-title {
  font-size: 14px;
  color: var(--color-text-secondary);
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: var(--color-text);
  line-height: 1.2;
}

/* ===== 内容卡 ===== */
.content-card {
  border-radius: var(--radius-card);
  margin-bottom: 16px;
  height: calc(100% - 16px);
  display: flex;
  flex-direction: column;
}

.content-card :deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid var(--color-border);
}

.content-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 20px;
}

.card-header {
  font-size: 16px;
  font-weight: 500;
  color: var(--color-text);
}

.charts-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chart-section {
  margin-bottom: 20px;
}

.chart-section:last-child {
  margin-bottom: 0;
}

.chart-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text);
  margin-bottom: 12px;
}

.chart {
  width: 100%;
}

.chart-pie {
  height: 200px;
}

.chart-bar {
  height: 260px;
}

.chart-footer {
  text-align: right;
  font-size: 13px;
  color: var(--color-accent);
  cursor: pointer;
  margin-top: 8px;
  transition: opacity 0.2s;
}

.chart-footer:hover {
  opacity: 0.75;
}

/* ===== 用户概览 ===== */
.user-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding: 8px 0;
}

.user-stat-item {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.user-stat-head {
  display: flex;
  align-items: center;
  gap: 10px;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.dot.active { background: var(--color-success); }
.dot.disabled { background: var(--color-error); }

.user-stat-label {
  flex: 1;
  font-size: 14px;
  color: var(--color-text-secondary);
}

.user-stat-value {
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text);
}

.user-stat-summary {
  display: flex;
  border-top: 1px solid var(--color-border);
  padding-top: 20px;
  margin-top: 4px;
}

.summary-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.summary-label {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.summary-value {
  font-size: 22px;
  font-weight: 600;
  color: var(--color-primary);
}

.quick-actions {
  padding-top: 20px;
  border-top: 1px solid var(--color-border);
  display: flex;
  gap: 12px;
}

.quick-actions .el-button {
  flex: 1;
}
</style>
