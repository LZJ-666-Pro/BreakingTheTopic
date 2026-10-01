<template>
  <div class="dashboard">
    <div class="page-header">
      <div class="page-header-left">
        <h2>工作台</h2>
        <p class="page-desc">
          欢迎回来，{{ nickname }} ·
          今日新增 <b>{{ overview.todayQuestionCount || 0 }}</b> 道题目，<b>{{ overview.pendingFeedbackCount || 0 }}</b> 条反馈待处理
        </p>
      </div>
      <el-button type="primary" size="large" class="add-btn" @click="$router.push('/question')">
        <el-icon><Plus /></el-icon>
        添加题目
      </el-button>
    </div>

    <div class="stat-grid">
      <div class="stat-card" v-for="card in statCards" :key="card.label">
        <div class="stat-icon" :class="card.type">
          <el-icon :size="22"><component :is="card.icon" /></el-icon>
          <span v-if="card.badge > 0" class="stat-badge">{{ card.badge > 99 ? '99+' : card.badge }}</span>
        </div>
        <div class="stat-meta">
          <span class="stat-title">{{ card.label }}</span>
          <span class="stat-value">{{ card.value }}</span>
        </div>
      </div>
    </div>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="14">
        <div class="panel" v-loading="loading">
          <div class="panel-header">
            <span class="panel-title">数据概览</span>
          </div>
          <div class="panel-body">
            <div class="section-title">题目难度分布</div>
            <div class="difficulty-block">
              <template v-if="difficultyTotal > 0">
                <div ref="difficultyChart" class="pie-chart"></div>
                <div class="difficulty-legend">
                  <div class="legend-item" v-for="item in difficultyList" :key="item.name">
                    <span class="legend-dot" :style="{ background: item.color }"></span>
                    <span class="legend-name">{{ item.name }}</span>
                    <span class="legend-value">{{ item.percent }}% ({{ item.value }})</span>
                  </div>
                </div>
              </template>
              <div v-else class="empty-tip">暂无难度数据</div>
            </div>

            <div class="section-title">题目分类分布（Top 10）</div>
            <div class="category-bars">
              <div class="bar-row" v-for="(item, index) in topCategories" :key="index">
                <span class="bar-name" :title="item.categoryName">{{ item.categoryName }}</span>
                <div class="bar-track">
                  <div class="bar-fill" :style="{ width: item.percent + '%' }"></div>
                </div>
                <span class="bar-count">{{ item.questionCount }}</span>
              </div>
              <div v-if="topCategories.length === 0" class="empty-tip">暂无分类数据</div>
            </div>

            <div class="panel-footer">
              <el-link type="primary" :underline="false" @click="$router.push('/category')">
                查看全部分类
                <el-icon><ArrowRight /></el-icon>
              </el-link>
            </div>
          </div>
        </div>
      </el-col>

      <el-col :xs="24" :lg="10">
        <div class="panel" v-loading="loading">
          <div class="panel-header">
            <span class="panel-title">用户动态</span>
          </div>
          <div class="panel-body">
            <div class="section-title">最近活跃用户</div>
            <div class="user-list">
              <div class="user-item" v-for="item in recentUsers" :key="item.userId">
                <el-avatar :size="40" :src="item.avatarUrl" class="user-avatar">
                  {{ (item.nickname || '微').charAt(0) }}
                </el-avatar>
                <div class="user-info">
                  <span class="user-name">{{ item.nickname || '微信用户' }}</span>
                  <span class="user-sub">最近做题 {{ formatTimeAgo(item.lastPracticeTime) }}</span>
                </div>
                <span class="user-time"><i class="time-dot"></i>{{ shortTimeAgo(item.lastPracticeTime) }}</span>
              </div>
              <div v-if="recentUsers.length === 0 && !loading" class="empty-tip">暂无活跃用户</div>
            </div>

            <div class="activity-summary">
              <div class="summary-item">
                <span class="summary-label">今日活跃</span>
                <span class="summary-value">{{ userStats.todayActiveCount || 0 }}</span>
              </div>
              <div class="summary-item">
                <span class="summary-label">本周活跃</span>
                <span class="summary-value">{{ userStats.weekActiveCount || 0 }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useUserStore } from '../stores/user'
import api from '../utils/api'
import { Document, Folder, User, ChatDotRound, Plus, ArrowRight } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const userStore = useUserStore()
const difficultyChart = ref(null)
let difficultyChartInstance = null

const loading = ref(true)

const nickname = computed(() => userStore.userInfo?.nickname || '管理员')

const overview = ref({
  questionCount: 0,
  categoryCount: 0,
  userCount: 0,
  enabledCount: 0,
  todayQuestionCount: 0,
  pendingFeedbackCount: 0,
  weekActiveUserCount: 0
})

const animatedStats = ref({
  questionCount: 0,
  categoryCount: 0,
  weekActiveUserCount: 0,
  pendingFeedbackCount: 0
})

const questionStats = ref({
  easyCount: 0,
  mediumCount: 0,
  hardCount: 0
})

const userStats = ref({
  activeCount: 0,
  disabledCount: 0,
  todayActiveCount: 0,
  weekActiveCount: 0
})

const categoryStats = ref([])
const recentUsers = ref([])

const statCards = computed(() => [
  { label: '题目总数', value: animatedStats.value.questionCount, icon: Document, type: 'blue', badge: 0 },
  { label: '分类数量', value: animatedStats.value.categoryCount, icon: Folder, type: 'blue', badge: 0 },
  { label: '近7日活跃用户', value: animatedStats.value.weekActiveUserCount, icon: User, type: 'blue', badge: 0 },
  { label: '待处理反馈', value: animatedStats.value.pendingFeedbackCount, icon: ChatDotRound, type: 'red', badge: overview.value.pendingFeedbackCount || 0 }
])

const difficultyMeta = [
  { key: 'mediumCount', name: '中等', color: '#3b82f6' },
  { key: 'easyCount', name: '简单', color: '#22c55e' },
  { key: 'hardCount', name: '困难', color: '#ef4444' }
]

const difficultyTotal = computed(() =>
  difficultyMeta.reduce((sum, item) => sum + (questionStats.value[item.key] || 0), 0)
)

const difficultyList = computed(() =>
  difficultyMeta.map(item => {
    const value = questionStats.value[item.key] || 0
    return {
      name: item.name,
      color: item.color,
      value,
      percent: difficultyTotal.value > 0 ? Math.round((value / difficultyTotal.value) * 100) : 0
    }
  })
)

const topCategories = computed(() => {
  const list = categoryStats.value.slice(0, 10)
  const max = list.length > 0 ? Math.max(...list.map(item => Number(item.questionCount) || 0)) : 0
  return list.map(item => ({
    ...item,
    percent: max > 0 ? Math.round(((Number(item.questionCount) || 0) / max) * 100) : 0
  }))
})

const pad = (num) => String(num).padStart(2, '0')

const parseTime = (time) => {
  if (!time) return null
  const date = new Date(String(time).replace(' ', 'T'))
  return Number.isNaN(date.getTime()) ? null : date
}

const formatTimeAgo = (time) => {
  const date = parseTime(time)
  if (!date) return '暂无记录'
  const diffMinutes = Math.floor((Date.now() - date.getTime()) / 60000)
  if (diffMinutes < 1) return '刚刚'
  if (diffMinutes < 60) return `${diffMinutes}分钟前`
  const diffHours = Math.floor(diffMinutes / 60)
  if (diffHours < 24) return `${diffHours}小时前`
  const diffDays = Math.floor(diffHours / 24)
  if (diffDays === 1) return '昨天'
  if (diffDays < 30) return `${diffDays}天前`
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

const shortTimeAgo = (time) => {
  const date = parseTime(time)
  if (!date) return '--'
  const diffMinutes = Math.floor((Date.now() - date.getTime()) / 60000)
  if (diffMinutes < 1) return '刚刚'
  if (diffMinutes < 60) return `${diffMinutes}分前`
  const diffHours = Math.floor(diffMinutes / 60)
  if (diffHours < 24) return `${diffHours}时前`
  const diffDays = Math.floor(diffHours / 24)
  if (diffDays === 1) return '昨天'
  if (diffDays < 30) return `${diffDays}天前`
  return `${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

const initDifficultyChart = () => {
  if (!difficultyChart.value) return

  if (difficultyChartInstance) {
    difficultyChartInstance.dispose()
    difficultyChartInstance = null
  }

  difficultyChartInstance = echarts.init(difficultyChart.value)

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    series: [
      {
        name: '题目难度',
        type: 'pie',
        radius: '68%',
        center: ['50%', '50%'],
        data: difficultyList.value.map(item => ({
          name: item.name,
          value: item.value,
          itemStyle: { color: item.color }
        })),
        label: {
          show: true,
          position: 'inside',
          formatter: '{b}\n{d}%',
          color: '#fff',
          fontSize: 12,
          lineHeight: 18
        },
        labelLine: { show: false },
        emphasis: {
          itemStyle: {
            shadowBlur: 12,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.2)'
          }
        }
      }
    ]
  }

  difficultyChartInstance.setOption(option)
}

const handleResize = () => {
  if (difficultyChartInstance) {
    difficultyChartInstance.resize()
  }
}

const animateNumber = (target, key, endValue) => {
  const duration = 800
  const startTime = Date.now()
  const startValue = 0

  const animate = () => {
    const elapsed = Date.now() - startTime
    const progress = Math.min(elapsed / duration, 1)
    const easeProgress = 1 - Math.pow(1 - progress, 3)
    target[key] = Math.round(startValue + (endValue - startValue) * easeProgress)

    if (progress < 1) {
      requestAnimationFrame(animate)
    }
  }

  animate()
}

onMounted(async () => {
  window.addEventListener('resize', handleResize)

  const [overviewRes, questionRes, userRes, categoryRes, recentUsersRes] = await Promise.allSettled([
    api.get('/admin/dashboard/overview'),
    api.get('/admin/dashboard/question-stats'),
    api.get('/admin/dashboard/user-stats'),
    api.get('/admin/dashboard/category-stats'),
    api.get('/admin/dashboard/recent-users')
  ])

  if (overviewRes.status === 'fulfilled') {
    overview.value = { ...overview.value, ...overviewRes.value.data }
    animateNumber(animatedStats.value, 'questionCount', overview.value.questionCount || 0)
    animateNumber(animatedStats.value, 'categoryCount', overview.value.categoryCount || 0)
    animateNumber(animatedStats.value, 'weekActiveUserCount', overview.value.weekActiveUserCount || 0)
    animateNumber(animatedStats.value, 'pendingFeedbackCount', overview.value.pendingFeedbackCount || 0)
  }
  if (questionRes.status === 'fulfilled') {
    questionStats.value = { ...questionStats.value, ...questionRes.value.data }
  }
  if (userRes.status === 'fulfilled') {
    userStats.value = { ...userStats.value, ...userRes.value.data }
  }
  if (categoryRes.status === 'fulfilled') {
    categoryStats.value = categoryRes.value.data || []
  }
  if (recentUsersRes.status === 'fulfilled') {
    recentUsers.value = recentUsersRes.value.data || []
  }

  loading.value = false

  await nextTick()
  if (difficultyTotal.value > 0) {
    initDifficultyChart()
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (difficultyChartInstance) {
    difficultyChartInstance.dispose()
    difficultyChartInstance = null
  }
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.page-desc {
  font-size: 14px;
  color: #8a919f;
  margin: 0;
}

.page-desc b {
  color: #1890ff;
  font-weight: 600;
}

.add-btn {
  border-radius: 6px;
  font-weight: 500;
}

.add-btn .el-icon {
  margin-right: 4px;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

@media (max-width: 992px) {
  .stat-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .stat-grid {
    grid-template-columns: 1fr;
  }
}

.stat-card {
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
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

.stat-icon.blue {
  background: #e8f3ff;
  color: #1890ff;
}

.stat-icon.red {
  background: #ffece8;
  color: #f5483b;
}

.stat-badge {
  position: absolute;
  top: -6px;
  right: -6px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 9px;
  background: #f5483b;
  color: #fff;
  font-size: 11px;
  line-height: 18px;
  text-align: center;
  font-weight: 600;
}

.stat-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-title {
  font-size: 14px;
  color: #8a919f;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  line-height: 1;
}

.panel {
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  margin-bottom: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.panel-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.panel-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.panel-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 20px;
}

.section-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 16px;
}

.difficulty-block {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 24px;
}

.pie-chart {
  width: 220px;
  height: 200px;
  flex-shrink: 0;
}

.difficulty-legend {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.legend-name {
  color: #4e5969;
}

.legend-value {
  margin-left: auto;
  color: #86909c;
}

.category-bars {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 8px;
}

.bar-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.bar-name {
  width: 88px;
  font-size: 13px;
  color: #4e5969;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  text-align: right;
  flex-shrink: 0;
}

.bar-track {
  flex: 1;
  height: 10px;
  background: #f2f3f5;
  border-radius: 5px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  border-radius: 5px;
  background: #8cbdfb;
  transition: width 0.6s ease;
}

.bar-count {
  width: 32px;
  font-size: 13px;
  color: #4e5969;
  text-align: right;
  flex-shrink: 0;
}

.panel-footer {
  margin-top: auto;
  padding-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.panel-footer .el-link {
  font-size: 13px;
}

.panel-footer .el-icon {
  margin-left: 2px;
}

.user-list {
  display: flex;
  flex-direction: column;
  margin-bottom: 20px;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
}

.user-item + .user-item {
  border-top: 1px solid #f7f8fa;
}

.user-avatar {
  background: #e8f3ff;
  color: #1890ff;
  font-size: 15px;
  flex-shrink: 0;
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.user-name {
  font-size: 15px;
  font-weight: 600;
  color: #1d2129;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-sub {
  font-size: 13px;
  color: #a1a7b3;
}

.user-time {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #a1a7b3;
  flex-shrink: 0;
}

.time-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #d4d7de;
}

.activity-summary {
  margin-top: auto;
  display: grid;
  grid-template-columns: 1fr 1fr;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 16px 0;
}

.summary-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.summary-item + .summary-item {
  border-left: 1px solid #f0f0f0;
}

.summary-label {
  font-size: 13px;
  color: #8a919f;
}

.summary-value {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
  line-height: 1;
}

.empty-tip {
  padding: 24px 0;
  text-align: center;
  font-size: 13px;
  color: #a1a7b3;
}
</style>
