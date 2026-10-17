<template>
  <div class="page-container">
    <div class="page-header">
      <h2>AI生成任务</h2>
      <div class="header-actions">
        <el-button type="primary" @click="openCreate">
          <el-icon><Plus /></el-icon>
          新建AI出题任务
        </el-button>
        <el-button @click="refreshTasks" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
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
        <el-table-column label="操作" width="150" align="center" fixed="right">
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
              type="warning"
              link
              size="small"
              @click="retryCreate(row)"
            >
              重新创建
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

    <!-- 新建 AI 出题任务弹窗 -->
    <el-dialog v-model="createVisible" title="新建AI出题任务" width="520px" :close-on-click-modal="false">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="90px">
        <el-form-item label="所属分类" prop="categoryId">
          <el-select v-model="createForm.categoryId" placeholder="选择出题分类" filterable style="width: 100%">
            <el-option v-for="c in categoryOptions" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度" prop="difficulty">
          <el-radio-group v-model="createForm.difficulty">
            <el-radio :value="1">简单</el-radio>
            <el-radio :value="2">中等</el-radio>
            <el-radio :value="3">困难</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="出题数量" prop="count">
          <el-input-number v-model="createForm.count" :min="1" :max="50" />
          <span class="form-tip">单次建议不超过 20 题，生成过快可能触发 AI 限流</span>
        </el-form-item>
        <el-form-item label="出题主题" prop="topic">
          <el-input v-model="createForm.topic" placeholder="选填，如：HashMap 扩容机制、Spring 事务失效场景" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="补充要求" prop="additionalRequirements">
          <el-input v-model="createForm.additionalRequirements" type="textarea" :rows="3" placeholder="选填，对题目内容的额外要求，如：偏重实际代码场景、包含追问链" maxlength="300" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="submitCreate">创建任务</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Loading, Plus } from '@element-plus/icons-vue'
import api from '../utils/api'
import { getDefaultPageSize } from '../utils/config'
import { formatDate } from '../utils/formatDate'

const loading = ref(false)
const tasks = ref([])
const currentPage = ref(1)
// 每页条数持久化：用户选择优先，其次系统配置默认值
const pageSize = ref(Number(localStorage.getItem('aitask_page_size')) || getDefaultPageSize())
watch(pageSize, v => localStorage.setItem('aitask_page_size', String(v)))
const total = ref(0)
let pollingTimer = null

// 创建任务弹窗状态
const createVisible = ref(false)
const creating = ref(false)
const createFormRef = ref(null)
const categoryOptions = ref([])
const createForm = reactive({
  categoryId: null,
  difficulty: 1,
  count: 5,
  topic: '',
  additionalRequirements: ''
})
const createRules = {
  categoryId: [{ required: true, message: '请选择出题分类', trigger: 'change' }],
  count: [{ required: true, message: '请填写出题数量', trigger: 'blur' }]
}

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

/** 加载分类下拉（全量分类接口） */
const loadCategories = async () => {
  if (categoryOptions.value.length > 0) return
  try {
    const res = await api.get('/admin/category/list')
    if (res.code === 200) {
      categoryOptions.value = res.data || []
    }
  } catch (error) {
    console.error('加载分类失败', error)
  }
}

/** 打开创建弹窗（空表单） */
const openCreate = async () => {
  createForm.categoryId = null
  createForm.difficulty = 1
  createForm.count = 5
  createForm.topic = ''
  createForm.additionalRequirements = ''
  createVisible.value = true
  await loadCategories()
}

/** 失败任务重试：按原任务配置预填创建弹窗 */
const retryCreate = async (task) => {
  await loadCategories()
  const diffMap = { '简单': 1, '中等': 2, '困难': 3 }
  createForm.categoryId = task.categoryId || null
  createForm.difficulty = diffMap[task.difficulty] || 1
  createForm.count = task.totalCount || 5
  createForm.topic = task.topic || ''
  createForm.additionalRequirements = task.additionalRequirements || ''
  createVisible.value = true
}

/** 提交创建：响应 taskId 字段为任务ID */
const submitCreate = async () => {
  try {
    await createFormRef.value.validate()
  } catch {
    return
  }
  creating.value = true
  try {
    const res = await api.post('/admin/question/ai/task/create', {
      categoryId: createForm.categoryId,
      categoryName: categoryOptions.value.find(c => c.id === createForm.categoryId)?.name,
      count: createForm.count,
      difficulty: createForm.difficulty,
      topic: createForm.topic || null,
      additionalRequirements: createForm.additionalRequirements || null
    })
    if (res.code === 200 && res.data && res.data.taskId) {
      ElMessage.success(`任务 #${res.data.taskId} 已创建，正在后台生成`)
      createVisible.value = false
      currentPage.value = 1
      loadTasks()
    } else {
      ElMessage.error(res.msg || '创建任务失败')
    }
  } catch (error) {
    ElMessage.error('创建任务失败')
  } finally {
    creating.value = false
  }
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

.header-actions {
  display: flex;
  align-items: center;
}

.form-tip {
  margin-left: 12px;
  font-size: 12px;
  color: #999;
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
