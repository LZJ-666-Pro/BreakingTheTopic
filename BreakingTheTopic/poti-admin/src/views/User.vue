<template>
  <div class="user-page">
    <div class="page-header">
      <h2>用户管理</h2>
      <div class="header-actions">
        <el-input 
          v-model="searchForm.nickname" 
          placeholder="搜索用户" 
          clearable 
          style="width: 200px"
          :prefix-icon="Search"
          @keyup.enter="loadUsers"
        />
        <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 100px">
          <el-option label="正常" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-button type="primary" @click="loadUsers">查询</el-button>
      </div>
    </div>

    <el-card class="table-card" shadow="never">
      <el-table :data="users" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="用户" min-width="200">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :src="row.avatarUrl" :size="32">
                {{ row.nickname?.charAt(0) }}
              </el-avatar>
              <div class="user-text">
                <span class="name">{{ row.nickname }}</span>
                <span class="contact">{{ row.phone || row.email || '未绑定' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="性别" width="60" align="center">
          <template #default="{ row }">
            <span>{{ row.gender === 1 ? '男' : row.gender === 2 ? '女' : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <span :class="['status', row.status === 1 ? 'active' : 'disabled']">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="160" align="center">
          <template #default="{ row }">
            <span class="time">{{ formatDate(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="210" align="center">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              size="small"
              @click="handleStatusChange(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button
              type="warning"
              link
              size="small"
              @click="handleResetData(row)"
            >
              重置数据
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
      
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100, 200, 500]"
          layout="total, sizes, prev, pager, next"
          background
          @size-change="loadUsers"
          @current-change="loadUsers"
        />
      </div>
    </el-card>

    <!-- 重置用户数据弹窗 -->
    <el-dialog
      v-model="resetDialog.visible"
      title="重置用户数据"
      width="480px"
      :close-on-click-modal="false"
    >
      <div class="reset-target" v-if="resetDialog.user">
        目标用户：<b>{{ resetDialog.user.nickname || resetDialog.user.id }}</b>
        <span class="reset-target-id">ID: {{ resetDialog.user.id }}</span>
      </div>
      <div class="reset-tip">
        勾选的数据将被<b>物理删除且不可恢复</b>；勾选「刷题记录」或「签到记录」会同步将学习统计（累计/今日/连续天数/积分）归零。
      </div>
      <el-checkbox-group v-model="resetDialog.checks">
        <div class="reset-item">
          <el-checkbox label="practice">刷题记录（练习明细 + 学习统计归零）</el-checkbox>
        </div>
        <div class="reset-item">
          <el-checkbox label="wrongbook">错题本</el-checkbox>
        </div>
        <div class="reset-item">
          <el-checkbox label="favorite">收藏</el-checkbox>
        </div>
        <div class="reset-item">
          <el-checkbox label="checkin">签到记录（含连续天数归零）</el-checkbox>
        </div>
        <div class="reset-item">
          <el-checkbox label="discussion">讨论记录（评论与点赞）</el-checkbox>
        </div>
      </el-checkbox-group>
      <div class="reset-confirm">
        <div class="reset-confirm-label">请输入该用户昵称以确认操作：</div>
        <el-input
          v-model="resetDialog.confirmText"
          :placeholder="resetDialog.user?.nickname || String(resetDialog.user?.id || '')"
          clearable
        />
      </div>
      <template #footer>
        <el-button @click="resetDialog.visible = false">取消</el-button>
        <el-button
          type="danger"
          :loading="resetDialog.loading"
          :disabled="!resetDialog.checks.length"
          @click="confirmReset"
        >
          确认重置
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import api from '../utils/api'
import { formatDate } from '../utils/formatDate'

const loading = ref(false)
const users = ref([])

const searchForm = reactive({
  nickname: '',
  status: null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const resetSearch = () => {
  searchForm.nickname = ''
  searchForm.status = null
  pagination.page = 1
  loadUsers()
}

const loadUsers = async () => {
  loading.value = true
  try {
    const res = await api.get('/admin/user/page', {
      params: {
        page: pagination.page,
        size: pagination.size,
        nickname: searchForm.nickname,
        status: searchForm.status
      }
    })
    
    if (res.data && res.data.records) {
      users.value = res.data.records
      pagination.total = res.data.total || 0
    } else if (Array.isArray(res.data)) {
      users.value = res.data
      pagination.total = res.data.length
    } else {
      users.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleStatusChange = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '禁用' : '启用'
  
  try {
    await ElMessageBox.confirm(`确定要${action}该用户吗？`, '操作确认', { 
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await api.put(`/admin/user/${row.id}/status`, null, {
      params: { status: newStatus }
    })
    ElMessage.success(`${action}成功`)
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户「${row.nickname || row.id}」吗？删除后该用户将无法登录，且数据不可恢复。`,
      '删除确认',
      {
        type: 'warning',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger'
      }
    )
    await api.delete(`/admin/user/${row.id}`)
    ElMessage.success('删除成功')
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

// ===== 重置用户数据 =====
const resetDialog = reactive({
  visible: false,
  user: null,
  checks: [],
  confirmText: '',
  loading: false
})

const handleResetData = (row) => {
  resetDialog.user = row
  resetDialog.checks = []
  resetDialog.confirmText = ''
  resetDialog.visible = true
}

const confirmReset = async () => {
  const user = resetDialog.user
  if (!user) return
  const expected = String(user.nickname || user.id)
  if ((resetDialog.confirmText || '').trim() !== expected) {
    ElMessage.warning(`请输入用户昵称「${expected}」以确认`)
    return
  }
  if (!resetDialog.checks.length) {
    ElMessage.warning('请至少勾选一个要重置的数据维度')
    return
  }
  resetDialog.loading = true
  try {
    const body = {}
    resetDialog.checks.forEach(k => { body[k] = true })
    const res = await api.post(`/admin/user/${user.id}/reset-data`, body)
    const data = res.data || {}
    const lines = []
    if (data.practice !== undefined) lines.push(`刷题记录：已删除 ${data.practice} 条`)
    if (data.wrongbook !== undefined) lines.push(`错题本：已删除 ${data.wrongbook} 条`)
    if (data.favorite !== undefined) lines.push(`收藏：已删除 ${data.favorite} 条`)
    if (data.checkin !== undefined) lines.push(`签到记录：已删除 ${data.checkin} 条`)
    if (data.discussion !== undefined) lines.push(`讨论评论：已删除 ${data.discussion} 条`)
    if (data.discussionLike !== undefined) lines.push(`讨论点赞：已删除 ${data.discussionLike} 条`)
    if (data.stats !== undefined) lines.push('学习统计：已归零')
    resetDialog.visible = false
    ElMessageBox.alert(lines.join('<br/>'), '重置完成', {
      confirmButtonText: '知道了',
      dangerouslyUseHTMLString: true,
      type: 'success'
    })
  } catch (error) {
    // 业务错误已由 api 响应拦截器统一提示，401 会自动跳登录
    console.error(error)
  } finally {
    resetDialog.loading = false
  }
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.user-page {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-header h2 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.table-card {
  border: none;
  box-shadow: none;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.name {
  font-size: 14px;
  color: #333;
}

.contact {
  font-size: 12px;
  color: #999;
}

.status {
  font-size: 13px;
}

.status.active {
  color: #52c41a;
}

.status.disabled {
  color: #999;
}

.time {
  font-size: 13px;
  color: #666;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding-top: 16px;
}

/* 重置数据弹窗 */
.reset-target {
  font-size: 14px;
  color: #333;
  margin-bottom: 12px;
}

.reset-target-id {
  color: #999;
  margin-left: 8px;
  font-size: 12px;
}

.reset-tip {
  font-size: 12px;
  color: #e6a23c;
  line-height: 1.6;
  background: #fdf6ec;
  border-radius: 4px;
  padding: 8px 12px;
  margin-bottom: 16px;
}

.reset-item {
  display: block;
  margin-bottom: 8px;
}

.reset-confirm {
  margin-top: 16px;
}

.reset-confirm-label {
  font-size: 13px;
  color: #333;
  margin-bottom: 8px;
}
</style>
