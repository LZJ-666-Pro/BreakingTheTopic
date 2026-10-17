<template>
  <div class="page">
    <el-card shadow="never">
      <div class="toolbar">
        <el-input v-model="query.adminName" placeholder="操作人" clearable style="width: 140px" @keyup.enter="search" @clear="search" />
        <el-select v-model="query.module" placeholder="模块" clearable style="width: 140px" @change="search">
          <el-option v-for="m in modules" :key="m" :label="m" :value="m" />
        </el-select>
        <el-select v-model="query.result" placeholder="结果" clearable style="width: 110px" @change="search">
          <el-option label="成功" :value="1" />
          <el-option label="失败" :value="0" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          style="width: 240px"
          @change="search"
        />
        <el-button type="primary" @click="search">搜索</el-button>
        <div class="toolbar-right">
          <el-button @click="exportLogs">导出 CSV</el-button>
          <el-button v-if="isSuperAdmin" type="danger" @click="clearLogs">清空日志</el-button>
        </div>
      </div>

      <el-table :data="logs" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="adminName" label="操作人" width="120" />
        <el-table-column prop="module" label="模块" width="120" />
        <el-table-column prop="action" label="动作" width="80" />
        <el-table-column prop="target" label="操作对象" min-width="180" show-overflow-tooltip />
        <el-table-column label="结果" width="80">
          <template #default="{ row }">
            <el-tag :type="row.result === 1 ? 'success' : 'danger'" size="small">{{ row.result === 1 ? '成功' : '失败' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP" width="130" />
        <el-table-column label="耗时" width="90">
          <template #default="{ row }">{{ row.costMs }}ms</template>
        </el-table-column>
        <el-table-column prop="createTime" label="操作时间" width="170">
          <template #default="{ row }">{{ (row.createTime || '').replace('T', ' ') }}</template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="showDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination"
        @size-change="handleSizeChange"
        @current-change="load"
      />
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailDialog" title="操作日志详情" width="640px">
      <el-descriptions :column="2" border v-if="detail">
        <el-descriptions-item label="操作人">{{ detail.adminName }}</el-descriptions-item>
        <el-descriptions-item label="模块">{{ detail.module }}</el-descriptions-item>
        <el-descriptions-item label="动作">{{ detail.action }}</el-descriptions-item>
        <el-descriptions-item label="结果">
          <el-tag :type="detail.result === 1 ? 'success' : 'danger'" size="small">{{ detail.result === 1 ? '成功' : '失败' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="IP">{{ detail.ip || '—' }}</el-descriptions-item>
        <el-descriptions-item label="耗时">{{ detail.costMs }}ms</el-descriptions-item>
        <el-descriptions-item label="操作对象" :span="2">{{ detail.target }}</el-descriptions-item>
        <el-descriptions-item label="操作时间" :span="2">{{ (detail.createTime || '').replace('T', ' ') }}</el-descriptions-item>
        <el-descriptions-item label="参数摘要" :span="2">
          <pre class="detail-json">{{ formatDetail(detail.detail) }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../utils/api'
import { getDefaultPageSize } from '../utils/config'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()
// 清空日志为超管专属：role=2 或权限为全部（super 角色）
const isSuperAdmin = computed(() => {
  const info = userStore.userInfo || {}
  return info.role === 2 || info.permissions === '*'
})

const modules = ['题目管理', '题库设置', '标签管理', '题库专题', '每日一题', '题解审核', '文章管理', '公告管理', '角色管理', '管理员账号', '用户管理', '反馈与举报', '系统配置', '登录注册', '其他']

const loading = ref(false)
const logs = ref([])
const total = ref(0)
const dateRange = ref(null)
const detailDialog = ref(false)
const detail = ref(null)

const query = reactive({
  pageNum: 1,
  // 每页条数持久化到 localStorage，刷新后保持用户选择
  pageSize: Number(localStorage.getItem('oplog_page_size')) || getDefaultPageSize(),
  adminName: '',
  module: '',
  result: null
})

const handleSizeChange = () => {
  // 切换每页条数时重置页码，避免超出范围显示空白
  query.pageNum = 1
  localStorage.setItem('oplog_page_size', String(query.pageSize))
  load()
}

const buildParams = () => {
  const params = {
    pageNum: query.pageNum,
    pageSize: query.pageSize,
    adminName: query.adminName || undefined,
    module: query.module || undefined,
    result: query.result ?? undefined
  }
  if (dateRange.value && dateRange.value.length === 2) {
    params.startDate = dateRange.value[0]
    params.endDate = dateRange.value[1]
  }
  return params
}

const load = async () => {
  loading.value = true
  try {
    const res = await api.get('/admin/log/list', { params: buildParams() })
    logs.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const search = () => {
  query.pageNum = 1
  load()
}

const showDetail = (row) => {
  detail.value = row
  detailDialog.value = true
}

// 参数摘要美化：尝试格式化 JSON
const formatDetail = (detailStr) => {
  if (!detailStr) return '—'
  try {
    return JSON.stringify(JSON.parse(detailStr), null, 2)
  } catch (e) {
    return detailStr
  }
}

const exportLogs = () => {
  const params = buildParams()
  delete params.pageNum
  delete params.pageSize
  // responseType blob：api 拦截器会透传完整 response
  api.get('/admin/log/export', { params, responseType: 'blob' }).then((res) => {
    const url = window.URL.createObjectURL(new Blob([res.data], { type: 'text/csv;charset=UTF-8' }))
    const link = document.createElement('a')
    link.href = url
    link.download = `操作日志_${new Date().toISOString().slice(0, 10)}.csv`
    link.click()
    window.URL.revokeObjectURL(url)
  })
}

const clearLogs = () => {
  ElMessageBox.prompt(
    '此操作将永久删除全部操作日志且不可恢复！请输入「清空」两字以确认执行',
    '高危操作确认',
    {
      type: 'warning',
      confirmButtonText: '确认清空',
      cancelButtonText: '取消',
      inputPlaceholder: '输入：清空',
      inputValidator: (v) => (v === '清空' ? true : '请输入「清空」两字以确认')
    }
  )
    .then(async () => {
      try {
        await api.post('/admin/log/clear')
        ElMessage.success('已清空')
        search()
      } catch (e) {
        ElMessage.error(e?.response?.data?.msg || e?.msg || '清空失败，可能无权限')
      }
    })
    .catch(() => {})
}

onMounted(load)
</script>

<style scoped>
.page {
  padding: 20px;
}
.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}
.toolbar-right {
  margin-left: auto;
}
.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
.detail-json {
  margin: 0;
  font-size: 12px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 240px;
  overflow: auto;
}
</style>
