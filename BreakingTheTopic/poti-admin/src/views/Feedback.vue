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
      <el-table :data="feedbackList" v-loading="loading" stripe>
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const loading = ref(false)
const feedbackList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filterStatus = ref(null)
const filterType = ref('')

const replyDialogVisible = ref(false)
const currentFeedback = ref(null)
const replyForm = ref({ reply: '' })
const submitting = ref(false)

const userUrl = import.meta.env.VITE_USER_URL || 'http://localhost:8200'

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
    
    const res = await axios.get(`${userUrl}/feedback/page`, { params })
    if (res.data.code === 200) {
      let list = res.data.data.records || []
      if (filterType.value) {
        list = list.filter(item => item.type === filterType.value)
      }
      feedbackList.value = list
      total.value = res.data.data.total || 0
    }
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
    const res = await axios.post(`${userUrl}/feedback/reply/${currentFeedback.value.id}`, {
      reply: replyForm.value.reply
    })
    if (res.data.code === 200) {
      ElMessage.success('回复成功')
      replyDialogVisible.value = false
      loadFeedbackList()
    } else {
      ElMessage.error(res.data.msg || '回复失败')
    }
  } catch (error) {
    ElMessage.error('回复失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除这条反馈吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await axios.delete(`${userUrl}/feedback/delete/${row.id}`, {
        headers: { 'X-User-Id': 0 }
      })
      if (res.data.code === 200) {
        ElMessage.success('删除成功')
        loadFeedbackList()
      } else {
        ElMessage.error(res.data.msg || '删除失败')
      }
    } catch (error) {
      ElMessage.error('删除失败')
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
