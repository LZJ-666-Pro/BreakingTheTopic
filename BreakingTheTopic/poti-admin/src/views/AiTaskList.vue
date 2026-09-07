<template>
  <div class="page-container">
    <div class="page-header">
      <h2>AI生成任务</h2>
      <el-button type="primary" @click="refreshTasks" :loading="loading">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <el-card class="content-card" shadow="never">
      <el-table :data="tasks" v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="categoryName" label="分类" width="120" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.categoryName || '未分类' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="进度" min-width="220">
          <template #default="{ row }">
            <div class="progress-cell">
              <div class="progress-header">
                <span class="progress-count">{{ row.completedCount || 0 }} / {{ row.totalCount }} 题</span>
                <span class="progress-percent">{{ getProgress(row) }}%</span>
              </div>
              <el-progress 
                :percentage="getProgress(row)" 
                :status="getProgressStatus(row.status)"
                :stroke-width="8"
                :show-text="false"
              />
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="difficulty" label="难度" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="difficultyMap[row.difficulty]?.type" size="small">
              {{ row.difficulty }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="topic" label="主题" width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="text-muted">{{ row.topic || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type" size="small">
              <el-icon v-if="row.status === 1" class="is-loading"><Loading /></el-icon>
              {{ statusMap[row.status]?.label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" align="center">
          <template #default="{ row }">
            <span class="text-muted">{{ formatDate(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="row.status === 1" 
              type="danger" 
              link 
              size="small" 
              @click="handleCancel(row)"
            >
              取消
            </el-button>
            <el-button 
              v-if="row.status === 3" 
              type="primary" 
              link 
              size="small" 
              @click="showError(row)"
            >
              查看错误
            </el-button>
            <el-button 
              type="danger" 
              link 
              size="small" 
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100, 200, 500]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadTasks"
          @current-change="loadTasks"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Loading } from '@element-plus/icons-vue'
import api from '../utils/api'
import { formatDate } from '../utils/formatDate'

const loading = ref(false)
const tasks = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
let pollingTimer = null

const statusMap = {
  0: { label: '待处理', type: 'info' },
  1: { label: '处理中', type: 'warning' },
  2: { label: '已完成', type: 'success' },
  3: { label: '失败', type: 'danger' },
  4: { label: '已取消', type: 'info' }
}

const difficultyMap = {
  '简单': { type: 'success' },
  '中等': { type: 'warning' },
  '困难': { type: 'danger' }
}

const getProgress = (task) => {
  if (!task.totalCount) return 0
  return Math.round((task.completedCount || 0) / task.totalCount * 100)
}

const getProgressStatus = (status) => {
  if (status === 2) return 'success'
  if (status === 3) return 'exception'
  return ''
}

const loadTasks = async () => {
  loading.value = true
  try {
    const res = await api.get('/admin/question/ai/task/list', {
      params: { page: currentPage.value, size: pageSize.value }
    })
    if (res.code === 200) {
      tasks.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('加载任务列表失败', error)
  } finally {
    loading.value = false
  }
}

const refreshTasks = () => {
  loadTasks()
}

const handleCancel = async (task) => {
  try {
    await ElMessageBox.confirm('确定要取消该任务吗？', '提示', { type: 'warning' })
    const res = await api.post(`/admin/question/ai/task/${task.id}/cancel`)
    if (res.code === 200) {
      ElMessage.success('任务已取消')
      loadTasks()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消失败')
    }
  }
}

const showError = (task) => {
  ElMessageBox.alert(task.errorMessage || '未知错误', '错误详情', { type: 'error' })
}

const handleDelete = async (task) => {
  try {
    await ElMessageBox.confirm('确定要删除该任务吗？', '提示', { type: 'warning' })
    const res = await api.delete(`/admin/question/ai/task/${task.id}`)
    if (res.code === 200) {
      ElMessage.success('任务已删除')
      loadTasks()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const startPolling = () => {
  pollingTimer = setInterval(() => {
    const hasRunning = tasks.value.some(t => t.status === 1)
    if (hasRunning) {
      loadTasks()
    }
  }, 5000)
}

onMounted(() => {
  loadTasks()
  startPolling()
})

onUnmounted(() => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
  }
})
</script>

<style scoped>
.page-container {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.content-card {
  border-radius: 4px;
  border: 1px solid #f0f0f0;
}

.progress-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.progress-count {
  font-size: 13px;
  color: #606266;
}

.progress-percent {
  font-size: 13px;
  font-weight: 600;
  color: #409eff;
}

.progress-cell :deep(.el-progress-bar__outer) {
  background-color: #ebeef5;
  border-radius: 4px;
}

.progress-cell :deep(.el-progress-bar__inner) {
  border-radius: 4px;
}

.text-muted {
  color: #999;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}
</style>
