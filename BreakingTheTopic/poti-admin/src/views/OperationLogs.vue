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
          <el-button type="danger" @click="clearLogs">清空日志</el-button>
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
        @size-change="load"
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
import { onMounted, ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../utils/api'

const modules = ['题目管理', '分类管理', '标签管理', '题库专题', '每日一题', '题解审核', '文章管理', '公告管理', '角色管理', '管理员账号', '用户管理', '反馈与举报', '系统配置', '登录注册', '其他']

const loading = ref(false)
const logs = ref([])
const total = ref(0)
const dateRange = ref(null)
const detailDialog = ref(false)
const detail = ref(null)

const query = reactive({
  pageNum: 1,
  pageSize: 20,
  adminName: '',
  module: '',
  result: null
})

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
  ElMessageBox.confirm('确定清空全部操作日志吗？此操作不可恢复！', '清空确认', { type: 'warning' })
    .then(async () => {
      await api.post('/admin/log/clear')
      ElMessage.success('已清空')
      search()
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
