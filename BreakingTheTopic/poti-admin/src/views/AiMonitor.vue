<template>
  <div class="page">
    <!-- 概览指标 -->
    <div class="overview-grid" v-loading="overviewLoading">
      <el-card v-for="card in overviewCards" :key="card.label" shadow="never" class="overview-card">
        <div class="overview-label">{{ card.label }}</div>
        <div class="overview-value">{{ card.value }}</div>
        <div class="overview-sub">{{ card.sub }}</div>
      </el-card>
    </div>

    <el-card shadow="never">
      <el-tabs v-model="activeTab">
        <!-- 会话记录 -->
        <el-tab-pane label="会话记录" name="sessions">
          <div class="toolbar">
            <el-input v-model="sessionQuery.keyword" placeholder="按会话标题搜索" clearable style="width: 240px"
              @keyup.enter="searchSessions" @clear="searchSessions" />
            <el-button type="primary" @click="searchSessions">搜索</el-button>
          </div>

          <el-table :data="sessions" v-loading="sessionsLoading" stripe>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="userId" label="用户ID" width="80" />
            <el-table-column prop="title" label="会话标题" min-width="240" show-overflow-tooltip />
            <el-table-column prop="messageCount" label="消息数" width="80" />
            <el-table-column prop="createTime" label="创建时间" width="170">
              <template #default="{ row }">{{ fmtTime(row.createTime) }}</template>
            </el-table-column>
            <el-table-column prop="updateTime" label="最后活跃" width="170">
              <template #default="{ row }">{{ fmtTime(row.updateTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="90" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="showMessages(row)">查看对话</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-model:current-page="sessionQuery.pageNum"
            v-model:page-size="sessionQuery.pageSize"
            :total="sessionTotal"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            class="pagination"
            @size-change="searchSessions"
            @current-change="loadSessions"
          />
        </el-tab-pane>

        <!-- 工具调用日志 -->
        <el-tab-pane label="工具调用日志" name="toolLogs">
          <div class="toolbar">
            <el-input v-model="logQuery.toolName" placeholder="工具名（如 get_study_）" clearable style="width: 220px"
              @keyup.enter="searchLogs" @clear="searchLogs" />
            <el-select v-model="logQuery.success" placeholder="执行结果" clearable style="width: 120px" @change="searchLogs">
              <el-option label="成功" :value="1" />
              <el-option label="失败" :value="0" />
            </el-select>
            <el-button type="primary" @click="searchLogs">搜索</el-button>
          </div>

          <el-table :data="logs" v-loading="logsLoading" stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="userId" label="用户ID" width="80" />
            <el-table-column prop="sessionId" label="会话ID" width="80" />
            <el-table-column prop="toolName" label="工具名" min-width="200">
              <template #default="{ row }"><code class="tool-name">{{ row.toolName }}</code></template>
            </el-table-column>
            <el-table-column prop="roundNo" label="轮次" width="70" />
            <el-table-column label="耗时" width="90">
              <template #default="{ row }">{{ row.costMs }}ms</template>
            </el-table-column>
            <el-table-column label="结果" width="80">
              <template #default="{ row }">
                <el-tag :type="row.success === 1 ? 'success' : 'danger'" size="small">{{ row.success === 1 ? '成功' : '失败' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="调用时间" width="170">
              <template #default="{ row }">{{ fmtTime(row.createTime) }}</template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-model:current-page="logQuery.pageNum"
            v-model:page-size="logQuery.pageSize"
            :total="logTotal"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            class="pagination"
            @size-change="searchLogs"
            @current-change="loadLogs"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 对话详情弹窗 -->
    <el-dialog v-model="msgDialog" :title="`会话对话详情 #${currentSession?.id || ''}`" width="720px">
      <div v-loading="msgLoading" class="msg-list">
        <div v-for="m in messages" :key="m.id" class="msg-item">
          <div class="msg-head">
            <el-tag :type="m.role === 'user' ? 'primary' : 'success'" size="small">
              {{ m.role === 'user' ? '用户' : '助手' }}
            </el-tag>
            <span class="msg-time">{{ fmtTime(m.createTime) }}</span>
          </div>
          <pre class="msg-content">{{ m.content }}</pre>
        </div>
        <el-empty v-if="!msgLoading && messages.length === 0" description="该会话暂无消息" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import api from '../utils/api'
import { getDefaultPageSize } from '../utils/config'

const activeTab = ref('sessions')
const overviewLoading = ref(false)
const overview = ref({})

const sessionQuery = reactive({ pageNum: 1, pageSize: getDefaultPageSize(20), keyword: '' })
const sessions = ref([])
const sessionTotal = ref(0)
const sessionsLoading = ref(false)

const logQuery = reactive({ pageNum: 1, pageSize: getDefaultPageSize(20), toolName: '', success: null })
const logs = ref([])
const logTotal = ref(0)
const logsLoading = ref(false)

const msgDialog = ref(false)
const msgLoading = ref(false)
const messages = ref([])
const currentSession = ref(null)

const overviewCards = computed(() => {
  const o = overview.value || {}
  return [
    { label: '今日会话', value: o.todaySessions ?? '—', sub: `累计 ${o.totalSessions ?? '—'} 个` },
    { label: '今日提问', value: o.todayQuestions ?? '—', sub: `累计消息 ${o.totalMessages ?? '—'} 条` },
    { label: '今日工具调用', value: o.todayToolCalls ?? '—', sub: `近7天 ${o.toolCalls7d ?? '—'} 次` },
    { label: '今日活跃用户', value: o.todayActiveUsers ?? '—', sub: `累计使用 ${o.totalUsers ?? '—'} 人` },
    { label: '工具成功率（近7天）', value: o.toolSuccessRate7d != null ? `${o.toolSuccessRate7d}%` : '—', sub: '成功调用 / 总调用' },
    { label: '工具平均耗时（近7天）', value: o.avgToolCost7d != null ? `${Math.round(o.avgToolCost7d)}ms` : '—', sub: '成功调用平均耗时' }
  ]
})

const fmtTime = (t) => (t || '').replace('T', ' ').slice(0, 19)

const loadOverview = async () => {
  overviewLoading.value = true
  try {
    const res = await api.get('/admin/agent/overview')
    if (res.code === 200) overview.value = res.data || {}
  } finally {
    overviewLoading.value = false
  }
}

const loadSessions = async () => {
  sessionsLoading.value = true
  try {
    const res = await api.get('/admin/agent/sessions', { params: sessionQuery })
    if (res.code === 200) {
      sessions.value = res.data.records || []
      sessionTotal.value = res.data.total || 0
    }
  } finally {
    sessionsLoading.value = false
  }
}

const searchSessions = () => {
  sessionQuery.pageNum = 1
  loadSessions()
}

const loadLogs = async () => {
  logsLoading.value = true
  try {
    const res = await api.get('/admin/agent/toolLogs', { params: logQuery })
    if (res.code === 200) {
      logs.value = res.data.records || []
      logTotal.value = res.data.total || 0
    }
  } finally {
    logsLoading.value = false
  }
}

const searchLogs = () => {
  logQuery.pageNum = 1
  loadLogs()
}

const showMessages = async (row) => {
  currentSession.value = row
  msgDialog.value = true
  msgLoading.value = true
  messages.value = []
  try {
    const res = await api.get('/admin/agent/messages', { params: { sessionId: row.id } })
    if (res.code === 200) messages.value = res.data || []
  } finally {
    msgLoading.value = false
  }
}

onMounted(() => {
  loadOverview()
  loadSessions()
  loadLogs()
})
</script>

<style scoped>
.overview-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}

.overview-label {
  font-size: 13px;
  color: #909399;
  white-space: nowrap;
}

.overview-value {
  font-size: 26px;
  font-weight: 600;
  color: #303133;
  margin: 6px 0 4px;
  white-space: nowrap;
}

.overview-sub {
  font-size: 12px;
  color: #c0c4cc;
  white-space: nowrap;
}

.tool-name {
  background: #f5f7fa;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 2px 6px;
  font-size: 12px;
}

.msg-list {
  max-height: 60vh;
  overflow-y: auto;
}

.msg-item {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 10px 12px;
  margin-bottom: 10px;
}

.msg-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.msg-time {
  font-size: 12px;
  color: #909399;
}

.msg-content {
  margin: 0;
  font-family: inherit;
  font-size: 13px;
  line-height: 1.7;
  color: #303133;
  white-space: pre-wrap;
  word-break: break-word;
}

@media (max-width: 1200px) {
  .overview-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
