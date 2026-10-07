<template>
  <div class="stats-page">
    <!-- 用户数据 -->
    <div class="section-title">用户数据</div>
    <el-row :gutter="16">
      <el-col :span="3" v-for="item in userCards" :key="item.label">
        <el-card shadow="never" class="mini-card">
          <div class="mini-value">{{ item.value }}</div>
          <div class="mini-label">{{ item.label }}</div>
        </el-card>
      </el-col>
      <el-col :span="3">
        <el-card shadow="never" class="mini-card">
          <div class="mini-value">{{ retention.d1 ?? '--' }}%</div>
          <div class="mini-label">次日留存</div>
        </el-card>
      </el-col>
      <el-col :span="3">
        <el-card shadow="never" class="mini-card">
          <div class="mini-value">{{ retention.d7 ?? '--' }}%</div>
          <div class="mini-label">7日留存</div>
        </el-card>
      </el-col>
      <el-col :span="3">
        <el-card shadow="never" class="mini-card">
          <div class="mini-value">{{ retention.d30 ?? '--' }}%</div>
          <div class="mini-label">30日留存</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 刷题行为数据 -->
    <div class="section-title">刷题行为</div>
    <el-row :gutter="16">
      <el-col :span="4" v-for="item in practiceCards" :key="item.label">
        <el-card shadow="never" class="mini-card">
          <div class="mini-value">{{ item.value }}</div>
          <div class="mini-label">{{ item.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 趋势图 -->
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="never">
          <div class="chart-title">近30天用户增长</div>
          <div ref="userTrendChart" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <div class="chart-title">近14天刷题量</div>
          <div ref="practiceTrendChart" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 分布图 -->
    <el-row :gutter="16" class="mt16">
      <el-col :span="8">
        <el-card shadow="never">
          <div class="chart-title">各难度通过率</div>
          <div ref="difficultyChart" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div class="chart-title">分类刷题热度 TOP8</div>
          <div ref="categoryChart" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div class="chart-title">连续打卡天数分布</div>
          <div ref="streakChart" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 热门题目 + 题库/特训营概览 -->
    <el-row :gutter="16" class="mt16">
      <el-col :span="16">
        <el-card shadow="never">
          <div class="chart-title">热门题目 TOP10</div>
          <el-table :data="hotQuestions" size="small" stripe>
            <el-table-column type="index" label="#" width="50" />
            <el-table-column prop="content" label="题目" min-width="280" show-overflow-tooltip />
            <el-table-column label="难度" width="80">
              <template #default="{ row }">
                <span :class="['diff-tag', diffMap[row.difficulty]?.cls]">{{ diffMap[row.difficulty]?.name }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="submits" label="提交量" width="90" sortable />
            <el-table-column prop="correct" label="通过量" width="90" sortable />
            <el-table-column label="通过率" width="100">
              <template #default="{ row }">
                {{ row.submits ? Math.round((row.correct || 0) / row.submits * 100) : 0 }}%
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="mb16">
          <div class="chart-title">题库概览</div>
          <div class="kv-row"><span>题目总数</span><b>{{ questionStats.totalQuestions || 0 }}</b></div>
          <div class="kv-row"><span>已启用题目</span><b>{{ questionStats.enabledQuestions || 0 }}</b></div>
          <div class="kv-row"><span>累计浏览量</span><b>{{ questionStats.totalViews || 0 }}</b></div>
        </el-card>
        <el-card shadow="never">
          <div class="chart-title">特训营概览</div>
          <div class="kv-row"><span>报名总人数</span><b>{{ campStats.totalMembers || 0 }}</b></div>
          <div class="kv-row"><span>结营人数</span><b>{{ campStats.finishedMembers || 0 }}</b></div>
          <div class="kv-row"><span>累计打卡次数</span><b>{{ campStats.totalCheckins || 0 }}</b></div>
          <div class="kv-row"><span>今日打卡人数</span><b>{{ campStats.todayCheckinUsers || 0 }}</b></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import api from '../utils/api'
import * as echarts from 'echarts'

// ===== 数据 =====
const userStats = ref({})
const lostUsers = ref(0)
const retention = ref({})
const practiceStats = ref({})
const questionStats = ref({})
const campStats = ref({})
const hotQuestions = ref([])

const diffMap = {
  1: { name: '简单', cls: 'easy' },
  2: { name: '中等', cls: 'medium' },
  3: { name: '困难', cls: 'hard' }
}

const userCards = computed(() => [
  { label: '总用户数', value: userStats.value.totalUsers || 0 },
  { label: '今日新增', value: userStats.value.todayNew || 0 },
  { label: '7日新增', value: userStats.value.weekNew || 0 },
  { label: '30日新增', value: userStats.value.monthNew || 0 },
  { label: '日活跃 DAU', value: userStats.value.dau || 0 },
  { label: '周活跃 WAU', value: userStats.value.wau || 0 },
  { label: '月活跃 MAU', value: userStats.value.mau || 0 },
  { label: '流失用户(30天未登录)', value: lostUsers.value || 0 }
])

const practiceCards = computed(() => {
  const p = practiceStats.value
  const rate = p.totalSubmits ? Math.round((p.totalCorrect || 0) / p.totalSubmits * 100) : 0
  return [
    { label: '总提交次数', value: p.totalSubmits || 0 },
    { label: '总通过次数', value: p.totalCorrect || 0 },
    { label: '整体通过率', value: rate + '%' },
    { label: '今日刷题量', value: p.todaySubmits || 0 },
    { label: '本周刷题量', value: p.weekSubmits || 0 },
    { label: '人均提交次数', value: p.avgPerUser || 0 }
  ]
})

// ===== 图表 =====
const userTrendChart = ref(null)
const practiceTrendChart = ref(null)
const difficultyChart = ref(null)
const categoryChart = ref(null)
const streakChart = ref(null)
const instances = []

function renderChart(el, option) {
  if (!el) return
  const inst = echarts.init(el)
  inst.setOption(option)
  instances.push(inst)
}

function baseOption() {
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 20, top: 30, bottom: 30 }
  }
}

async function loadStats() {
  try {
    // api 响应拦截器已返回 {code, msg, data}，这里的 res.data 即统计数据
    const res = await api.get('/admin/stats/overview')
    const d = res?.data || {}
    userStats.value = d.user || {}
    lostUsers.value = d.lostUsers || 0
    retention.value = d.retention || {}
    practiceStats.value = d.practice || {}
    questionStats.value = d.question || {}
    campStats.value = d.camp || {}
    hotQuestions.value = d.hotQuestions || []

    await nextTick()

    // 用户增长趋势
    const ut = d.userTrend || []
    renderChart(userTrendChart.value, {
      ...baseOption(),
      xAxis: { type: 'category', data: ut.map(i => i.day) },
      yAxis: { type: 'value', minInterval: 1 },
      series: [{ name: '新增用户', type: 'line', smooth: true, data: ut.map(i => i.cnt), areaStyle: { opacity: 0.15 }, itemStyle: { color: '#409EFF' } }]
    })

    // 刷题量趋势
    const pt = d.practiceTrend || []
    renderChart(practiceTrendChart.value, {
      ...baseOption(),
      xAxis: { type: 'category', data: pt.map(i => i.day) },
      yAxis: { type: 'value', minInterval: 1 },
      series: [{ name: '刷题量', type: 'bar', data: pt.map(i => i.cnt), itemStyle: { color: '#67C23A', borderRadius: [3, 3, 0, 0] } }]
    })

    // 难度通过率
    const ds = d.difficultyStats || []
    const diffNames = { 1: '简单', 2: '中等', 3: '困难' }
    const diffColors = { 1: '#10B981', 2: '#1E40AF', 3: '#EF4444' }
    renderChart(difficultyChart.value, {
      ...baseOption(),
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      xAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' } },
      yAxis: { type: 'category', data: ds.map(i => diffNames[i.difficulty] || i.difficulty) },
      series: [{
        name: '通过率',
        type: 'bar',
        data: ds.map(i => ({
          value: i.submits ? Math.round((i.correct || 0) / i.submits * 100) : 0,
          itemStyle: { color: diffColors[i.difficulty] || '#409EFF', borderRadius: [0, 3, 3, 0] }
        })),
        label: { show: true, position: 'right', formatter: '{c}%' }
      }]
    })

    // 分类热度
    const ch = d.categoryHot || []
    renderChart(categoryChart.value, {
      ...baseOption(),
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      xAxis: { type: 'value', minInterval: 1 },
      yAxis: { type: 'category', data: ch.map(i => i.name).reverse() },
      series: [{
        name: '提交量',
        type: 'bar',
        data: ch.map(i => i.cnt).reverse(),
        itemStyle: { color: '#409EFF', borderRadius: [0, 3, 3, 0] }
      }]
    })

    // 连续打卡分布
    const sd = d.streakDist || {}
    renderChart(streakChart.value, {
      tooltip: { trigger: 'item' },
      legend: { bottom: 0, itemWidth: 12, itemHeight: 12 },
      series: [{
        type: 'pie',
        radius: ['40%', '65%'],
        center: ['50%', '45%'],
        label: { show: false },
        data: [
          { name: '1-3天', value: sd.s1to3 || 0, itemStyle: { color: '#94A3B8' } },
          { name: '4-7天', value: sd.s4to7 || 0, itemStyle: { color: '#409EFF' } },
          { name: '8-14天', value: sd.s8to14 || 0, itemStyle: { color: '#E6A23C' } },
          { name: '15天以上', value: sd.s15plus || 0, itemStyle: { color: '#10B981' } }
        ]
      }]
    })
  } catch (error) {
    console.error('加载统计数据失败', error)
  }
}

const handleResize = () => instances.forEach(i => i.resize())

onMounted(() => {
  loadStats()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  instances.forEach(i => i.dispose())
})
</script>

<style scoped>
.stats-page {
  padding-bottom: 20px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin: 4px 0 12px;
}

.mt16 {
  margin-top: 16px;
}

.mb16 {
  margin-bottom: 16px;
}

.mini-card {
  text-align: center;
}

.mini-card :deep(.el-card__body) {
  padding: 16px 8px;
}

.mini-value {
  font-size: 22px;
  font-weight: 700;
  color: #1E293B;
  margin-bottom: 4px;
}

.mini-label {
  font-size: 12px;
  color: #909399;
}

.chart-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.chart {
  height: 260px;
}

.kv-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 4px;
  border-bottom: 1px solid #f5f5f5;
  font-size: 13px;
  color: #606266;
}

.kv-row:last-child {
  border-bottom: none;
}

.kv-row b {
  color: #1E293B;
}

.diff-tag {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
}

.diff-tag.easy {
  color: #10B981;
  background: #ecfdf5;
}

.diff-tag.medium {
  color: #1E40AF;
  background: #eff6ff;
}

.diff-tag.hard {
  color: #EF4444;
  background: #fef2f2;
}
</style>
