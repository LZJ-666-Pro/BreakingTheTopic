<template>
  <div class="feedback-page">
    <div class="page-header">
      <h2>反馈管理</h2>
    </div>

    <div class="filter-card">
      <el-form :inline="true">
        <el-form-item label="状态">
          <el-select v-model="filterStatus" placeholder="全部" clearable style="width: 120px" @change="loadFeedbackList">
            <el-option label="待处理" :value="0" />
            <el-option label="已回复" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="filterType" placeholder="全部" clearable style="width: 120px" @change="loadFeedbackList">
            <el-option label="问题反馈" value="bug" />
            <el-option label="功能建议" value="feature" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-card">
      <div class="batch-bar">
        <el-button
          type="primary"
          :disabled="selectedRows.length === 0"
          :loading="batchResolving"
          @click="handleBatchResolve"
        >
          批量标记已处理
        </el-button>
        <span class="batch-tip" v-if="selectedRows.length > 0">已选 {{ selectedRows.length }} 条，仅待处理记录会被处理</span>
      </div>
      <el-table :data="feedbackList" v-loading="loading" stripe @selection-change="onSelectionChange">
        <el-table-column type="selection" width="45" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="nickname" label="用户" width="120">
          <template #default="{ row }">
            {{ row.nickname || '匿名用户' }}
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 'bug' ? 'danger' : row.type === 'feature' ? 'success' : 'info'" size="small">
              {{ row.type === 'bug' ? '问题反馈' : row.type === 'feature' ? '功能建议' : '其他' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" min-width="200">
          <template #default="{ row }">
            <el-tooltip :content="row.content" placement="top" :disabled="row.content.length < 50">
              <span class="content-text">{{ row.content }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="contact" label="联系方式" width="140">
          <template #default="{ row }">
            {{ row.contact || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'" size="small">
              {{ row.status === 1 ? '已回复' : '待处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openReplyDialog(row)">
              {{ row.status === 1 ? '查看' : '回复' }}
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100, 200, 500]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadFeedbackList"
          @current-change="loadFeedbackList"
        />
      </div>
    </div>

    <el-dialog v-model="replyDialogVisible" :title="currentFeedback?.status === 1 ? '查看反馈' : '回复反馈'" width="500px">
      <div class="feedback-detail">
        <div class="detail-item">
          <span class="label">用户：</span>
          <span>{{ currentFeedback?.nickname || '匿名用户' }}</span>
        </div>
        <div class="detail-item">
          <span class="label">类型：</span>
          <span>{{ currentFeedback?.type === 'bug' ? '问题反馈' : currentFeedback?.type === 'feature' ? '功能建议' : '其他' }}</span>
        </div>
        <div class="detail-item">
          <span class="label">内容：</span>
          <span>{{ currentFeedback?.content }}</span>
        </div>
        <div class="detail-item" v-if="currentFeedback?.contact">
          <span class="label">联系方式：</span>
          <span>{{ currentFeedback?.contact }}</span>
        </div>
        <div class="detail-item">
          <span class="label">提交时间：</span>
          <span>{{ formatTime(currentFeedback?.createTime) }}</span>
        </div>
      </div>
      <el-divider />
      <el-form :model="replyForm" label-width="80px">
        <el-form-item label="回复内容">
          <el-input
            v-model="replyForm.reply"
            type="textarea"
            :rows="4"
            placeholder="请输入回复内容"
            :disabled="currentFeedback?.status === 1"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReply" :loading="submitting" v-if="currentFeedback?.status !== 1">
          提交回复
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../utils/api'
import { getDefaultPageSize } from '../utils/config'

const loading = ref(false)
const feedbackList = ref([])
const pageNum = ref(1)
// 每页条数持久化：用户选择优先，其次系统配置默认值
const pageSize = ref(Number(localStorage.getItem('feedback_page_size')) || getDefaultPageSize())
watch(pageSize, v => localStorage.setItem('feedback_page_size', String(v)))
const total = ref(0)
const filterStatus = ref(null)
const filterType = ref('')

const replyDialogVisible = ref(false)
const currentFeedback = ref(null)
const replyForm = ref({ reply: '' })
const submitting = ref(false)

// 批量标记已处理
const selectedRows = ref([])
const batchResolving = ref(false)

const onSelectionChange = (rows) => {
  selectedRows.value = rows
}

const handleBatchResolve = () => {
  const ids = selectedRows.value.map(r => r.id)
  ElMessageBox.prompt(
    `将选中的 ${ids.length} 条反馈标记为已处理，用户端可见。可填写统一回复话术（留空使用默认话术）`,
    '批量标记已处理',
    {
      confirmButtonText: '确认处理',
      cancelButtonText: '取消',
      inputPlaceholder: '默认：已处理，感谢您的反馈！',
      inputType: 'textarea',
      inputValidator: (v) => !v || v.length <= 200 || '话术不能超过 200 字'
    }
  ).then(async ({ value }) => {
    batchResolving.value = true
    try {
      const res = await api.post('/feedback/batch/resolve', {
        ids,
        reply: value || null
      })
      ElMessage.success(res.data || '处理成功')
      loadFeedbackList()
    } catch (error) {
      // 统一拦截器已提示错误
    } finally {
      batchResolving.value = false
    }
  }).catch(() => {})
}

const loadFeedbackList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
    if (filterStatus.value !== null) {
      params.status = filterStatus.value
    }
    
    const res = await api.get('/feedback/page', { params })
    let list = res.data.records || []
    if (filterType.value) {
      list = list.filter(item => item.type === filterType.value)
    }
    feedbackList.value = list
    total.value = res.data.total || 0
  } catch (error) {
    console.error('加载反馈列表失败', error)
  } finally {
    loading.value = false
  }
}

const openReplyDialog = (row) => {
  currentFeedback.value = row
  replyForm.value.reply = row.reply || ''
  replyDialogVisible.value = true
}

const submitReply = async () => {
  if (!replyForm.value.reply.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  
  submitting.value = true
  try {
    await api.post(`/feedback/reply/${currentFeedback.value.id}`, {
      reply: replyForm.value.reply
    })
    ElMessage.success('回复成功')
    replyDialogVisible.value = false
    loadFeedbackList()
  } catch (error) {
    // 统一拦截器已提示错误
  } finally {
    submitting.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除这条反馈吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await api.delete(`/feedback/delete/${row.id}`)
      ElMessage.success('删除成功')
      loadFeedbackList()
    } catch (error) {
      // 统一拦截器已提示错误
    }
  }).catch(() => {})
}

const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  loadFeedbackList()
})
</script>

<style scoped>
.feedback-page {
  
}

.page-header {
  margin-bottom: 16px;
}

.page-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.filter-card {
  background: #fff;
  padding: 16px 20px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.table-card {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
}

.batch-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.batch-tip {
  font-size: 12px;
  color: #999;
}

.content-text {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.feedback-detail {
  
}

.detail-item {
  margin-bottom: 12px;
  display: flex;
}

.detail-item .label {
  width: 80px;
  color: #666;
  flex-shrink: 0;
}
</style>
