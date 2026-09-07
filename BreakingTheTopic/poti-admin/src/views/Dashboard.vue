<template>
  <div class="dashboard">
    <div class="page-header">
      <h2>工作台</h2>
      <p>欢迎回来，{{ userStore.userInfo?.nickname || '管理员' }}</p>
    </div>

    <el-row :gutter="16" class="stat-row">
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card">
          <div class="stat-header">
            <span class="stat-title">题目总数</span>
            <el-icon class="stat-icon" :size="20"><Document /></el-icon>
          </div>
          <div class="stat-value">{{ animatedStats.questionCount }}</div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card">
          <div class="stat-header">
            <span class="stat-title">分类数量</span>
            <el-icon class="stat-icon" :size="20"><Folder /></el-icon>
          </div>
          <div class="stat-value">{{ animatedStats.categoryCount }}</div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card">
          <div class="stat-header">
            <span class="stat-title">用户数量</span>
            <el-icon class="stat-icon" :size="20"><User /></el-icon>
          </div>
          <div class="stat-value">{{ animatedStats.userCount }}</div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card">
          <div class="stat-header">
            <span class="stat-title">启用题目</span>
            <el-icon class="stat-icon" :size="20"><CircleCheck /></el-icon>
          </div>
          <div class="stat-value">{{ animatedStats.enabledCount }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="12">
        <el-card class="content-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>数据统计</span>
            </div>
          </template>
          <div class="charts-wrapper">
            <div class="chart-section">
              <div class="chart-title">题目难度分布</div>
              <div ref="difficultyChart" class="chart"></div>
            </div>
            <div class="chart-section">
              <div class="chart-title">题目分类分布</div>
              <div ref="categoryChart" class="chart"></div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card class="content-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>用户状态分布</span>
            </div>
          </template>
          <div class="chart-content">
            <div class="status-item">
              <div class="status-dot active"></div>
              <div class="status-info">
                <span class="status-label">活跃用户</span>
                <span class="status-value">{{ userStats.activeCount }}</span>
              </div>
              <span class="status-percent">{{ getPercentage(userStats.activeCount, stats.userCount) }}%</span>
            </div>
            <div class="status-item">
              <div class="status-dot disabled"></div>
              <div class="status-info">
                <span class="status-label">禁用用户</span>
                <span class="status-value">{{ userStats.disabledCount }}</span>
              </div>
              <span class="status-percent">{{ getPercentage(userStats.disabledCount, stats.userCount) }}%</span>
            </div>
          </div>
          <div class="quick-actions">
            <el-button type="primary" @click="$router.push('/question')">
              <el-icon><Plus /></el-icon>
              添加题目
            </el-button>
            <el-button @click="$router.push('/user')">
              <el-icon><User /></el-icon>
              用户管理
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import { useUserStore } from '../stores/user'
import api from '../utils/api'
import { Document, Folder, User, CircleCheck, Plus } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const userStore = useUserStore()
const difficultyChart = ref(null)
const categoryChart = ref(null)
let difficultyChartInstance = null
let categoryChartInstance = null

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
  enabledCount: 0
})

const questionStats = ref({
  easyCount: 0,
  mediumCount: 0,
  hardCount: 0,
  enabledCount: 0,
  difficultyStats: {}
})

const userStats = ref({
  activeCount: 0,
  disabledCount: 0
})

const categoryStats = ref([])

const getPercentage = (value, total) => {
  if (total === 0) return 0
  return Math.round((value / total) * 100)
}

const initDifficultyChart = () => {
  if (!difficultyChart.value) return
  
  if (difficultyChartInstance) {
    difficultyChartInstance.dispose()
  }
  
  difficultyChartInstance = echarts.init(difficultyChart.value)
  
  const difficultyStats = questionStats.value.difficultyStats || {}
  console.log('difficultyStats:', difficultyStats)
  
  const chartData = []
  
  for (const [name, value] of Object.entries(difficultyStats)) {
    console.log(`难度: ${name}, 数量: ${value}`)
    chartData.push({
      name: name,
      value: value || 0
    })
  }
  
  console.log('chartData:', chartData)
  
  if (chartData.length === 0) {
    console.log('没有数据，跳过图表渲染')
    return
  }
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center'
    },
    series: [
      {
        name: '题目难度',
        type: 'pie',
        radius: '60%',
        center: ['40%', '50%'],
        data: chartData,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  
  difficultyChartInstance.setOption(option)
}

const initCategoryChart = () => {
  if (!categoryChart.value) return
  
  if (categoryChartInstance) {
    categoryChartInstance.dispose()
  }
  
  categoryChartInstance = echarts.init(categoryChart.value)
  
  const categories = categoryStats.value.map(item => item.categoryName)
  const counts = categoryStats.value.map(item => item.questionCount)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: categories,
      axisLabel: {
        interval: 0,
        rotate: 30
      }
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '题目数量',
        type: 'bar',
        data: counts,
        itemStyle: {
          color: '#409eff'
        }
      }
    ]
  }
  
  categoryChartInstance.setOption(option)
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
    
    animateNumber(animatedStats.value, 'questionCount', stats.value.questionCount)
    animateNumber(animatedStats.value, 'categoryCount', stats.value.categoryCount)
    animateNumber(animatedStats.value, 'userCount', stats.value.userCount)
    animateNumber(animatedStats.value, 'enabledCount', stats.value.enabledCount)
    
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
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.page-header p {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.stat-row {
  margin-bottom: 16px;
}

.stat-card {
  background: #fff;
  border-radius: 4px;
  padding: 20px;
  margin-bottom: 16px;
}

.stat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.stat-title {
  font-size: 14px;
  color: #666;
}

.stat-icon {
  color: #1890ff;
}

.stat-value {
  font-size: 32px;
  font-weight: 600;
  color: #1a1a1a;
  line-height: 1;
}

.content-card {
  border-radius: 4px;
  border: 1px solid #f0f0f0;
  margin-bottom: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.content-card :deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.content-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 20px;
}

.card-header {
  font-size: 15px;
  font-weight: 500;
  color: #1a1a1a;
}

.chart-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px 0;
}

.charts-wrapper {
  padding: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-evenly;
}

.chart-section {
  margin-bottom: 0;
}

.chart-section:last-child {
  margin-bottom: 0;
}

.chart-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 16px;
  padding-left: 12px;
  border-left: 3px solid #409eff;
}

.chart {
  width: 100%;
  height: 190px;
}

.chart-content {
  padding: 4px 0;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.chart-item {
  margin-bottom: 20px;
}

.chart-item:last-child {
  margin-bottom: 0;
}

.chart-label {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
  color: #666;
}

.chart-value {
  color: #1a1a1a;
  font-weight: 500;
}

.status-item {
  display: flex;
  align-items: center;
  padding: 24px 20px;
  background: #fafafa;
  border-radius: 4px;
  margin-bottom: 24px;
}

.status-item:last-child {
  margin-bottom: 0;
}

.status-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 16px;
}

.status-dot.active {
  background: #52c41a;
}

.status-dot.disabled {
  background: #ff4d4f;
}

.status-info {
  flex: 1;
}

.status-label {
  display: block;
  font-size: 14px;
  color: #666;
  margin-bottom: 6px;
}

.status-value {
  font-size: 26px;
  font-weight: 600;
  color: #1a1a1a;
}

.status-percent {
  font-size: 18px;
  font-weight: 500;
  color: #1890ff;
}

.quick-actions {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.quick-actions .el-button {
  width: 100%;
}
</style>
