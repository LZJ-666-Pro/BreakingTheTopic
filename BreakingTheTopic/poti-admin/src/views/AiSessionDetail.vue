<template>
  <div class="page">
    <!-- 头部：返回 + 会话信息 -->
    <el-card shadow="never" class="header-card">
      <div class="header-row">
        <el-button :icon="ArrowLeft" @click="goBack">返回</el-button>
        <div class="header-info">
          <div class="header-title">{{ sessionTitle || `会话 #${sessionId}` }}</div>
          <div class="header-meta">
            <span>用户 #{{ userId }}</span>
            <span>会话 #{{ sessionId }}</span>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 对话区 -->
    <el-card shadow="never">
      <div v-loading="loading" class="msg-list">
        <div v-for="m in messages" :key="m.id" class="msg-row" :class="m.role === 'user' ? 'is-user' : 'is-ai'">
          <div class="role-badge" :class="m.role === 'user' ? 'badge-user' : 'badge-ai'">
            {{ m.role === 'user' ? '用户' : 'AI' }}
          </div>
          <div class="msg-bubble">
            <pre class="msg-content">{{ m.content }}</pre>
            <div class="msg-time">{{ fmtTime(m.createTime) }}</div>
          </div>
        </div>
        <el-empty v-if="!loading && messages.length === 0" description="该会话暂无消息" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import api from '../utils/api'

const route = useRoute()
const router = useRouter()

const sessionId = computed(() => route.params.id)
const userId = computed(() => route.query.userId || '—')
const sessionTitle = computed(() => route.query.title || '')

const loading = ref(false)
const messages = ref([])

const fmtTime = (t) => (t || '').replace('T', ' ').slice(0, 19)

const goBack = () => {
  if (window.history.length > 1) router.back()
  else router.push('/ai-monitor')
}

const loadMessages = async () => {
  loading.value = true
  try {
    const res = await api.get('/admin/agent/messages', { params: { sessionId: sessionId.value } })
    if (res.code === 200) messages.value = res.data || []
  } finally {
    loading.value = false
  }
}

onMounted(loadMessages)
</script>

<style scoped>
.header-card {
  margin-bottom: 16px;
}

.header-row {
  display: flex;
  align-items: center;
  gap: 14px;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.header-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #909399;
}

.header-meta span:first-child::after {
  content: '·';
  margin-left: 12px;
  color: #dcdfe6;
}

.msg-list {
  min-height: 300px;
  max-height: 68vh;
  overflow-y: auto;
  background: #fafbfc;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 16px;
}

.msg-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 14px;
}

.msg-row:last-child {
  margin-bottom: 0;
}

.msg-row.is-user {
  flex-direction: row-reverse;
}

.role-badge {
  flex-shrink: 0;
  width: 34px;
  height: 34px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

.badge-ai {
  background: #ffffff;
  border: 1px solid #dcdfe6;
  color: #606266;
  font-weight: 600;
}

.badge-user {
  background: #eaf2fe;
  border: 1px solid #d8e6fc;
  color: #4a6fa5;
}

.msg-bubble {
  max-width: 78%;
  border-radius: 8px;
  padding: 9px 12px;
}

.is-ai .msg-bubble {
  background: #ffffff;
  border: 1px solid #ebeef5;
}

.is-user .msg-bubble {
  background: #eaf2fe;
  border: 1px solid #d8e6fc;
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

.msg-time {
  font-size: 11px;
  color: #c0c4cc;
  margin-top: 4px;
  text-align: right;
}
</style>
