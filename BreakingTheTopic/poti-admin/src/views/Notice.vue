<template>
  <div class="page">
    <el-card shadow="never">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px" @change="search">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
          <el-select v-model="query.displayType" placeholder="展示方式" clearable style="width: 130px" @change="search">
            <el-option label="弹窗" :value="0" />
            <el-option label="顶部横幅" :value="1" />
            <el-option label="消息中心" :value="2" />
          </el-select>
          <el-input
            v-model="query.keyword"
            placeholder="搜索公告标题"
            clearable
            style="width: 200px"
            @keyup.enter="search"
            @clear="search"
          />
          <el-button type="primary" :icon="Search" @click="search">搜索</el-button>
        </div>
        <el-button type="primary" :icon="Plus" @click="openCreate">发布公告</el-button>
      </div>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="64" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column label="展示方式" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ displayText(row.displayType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="目标用户" width="110">
          <template #default="{ row }">
            <span>{{ targetText(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="生效时间" width="240">
          <template #default="{ row }">
            <span class="time-range">{{ timeRange(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="标记" width="130">
          <template #default="{ row }">
            <el-tag v-if="row.forceRead === 1" type="danger" size="small" class="mini-tag">强制阅读</el-tag>
            <el-tag v-if="row.status === 0" type="info" size="small" class="mini-tag">已停用</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="数据" width="130">
          <template #default="{ row }">
            <span class="metric">📖 {{ row.readCount }}</span>
            <span class="metric">👆 {{ row.clickCount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 1" link type="danger" @click="changeStatus(row, 0)">停用</el-button>
            <el-button v-else link type="primary" @click="changeStatus(row, 1)">启用</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="load"
          @size-change="search"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialog.visible" :title="dialog.id ? '编辑公告' : '发布公告'" width="620px">
      <el-form label-width="90px">
        <el-form-item label="标题" required>
          <el-input v-model="dialog.form.title" maxlength="200" placeholder="公告标题" />
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input v-model="dialog.form.content" type="textarea" :rows="5" maxlength="2000" show-word-limit />
        </el-form-item>
        <el-form-item label="展示方式">
          <el-radio-group v-model="dialog.form.displayType">
            <el-radio :value="0">弹窗</el-radio>
            <el-radio :value="1">顶部横幅</el-radio>
            <el-radio :value="2">消息中心</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="目标用户">
          <el-radio-group v-model="dialog.form.targetType">
            <el-radio :value="0">全部用户</el-radio>
            <el-radio :value="1">会员用户</el-radio>
            <el-radio :value="2">指定用户</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="dialog.form.targetType === 2" label="用户ID" required>
          <el-input v-model="dialog.form.targetUserIds" placeholder="多个用户ID用英文逗号分隔，如：1,2,3" />
        </el-form-item>
        <el-form-item label="生效时间">
          <el-date-picker
            v-model="dialog.form.timeRange"
            type="datetimerange"
            start-placeholder="开始时间（可选）"
            end-placeholder="结束时间（可选）"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="强制阅读">
          <el-switch v-model="dialog.form.forceRead" :active-value="1" :inactive-value="0" />
          <span class="form-tip">开启后用户无法关闭弹窗</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="dialog.saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import api from '../utils/api'

const loading = ref(false)
const list = ref([])
const total = ref(0)

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  status: '',
  displayType: '',
  keyword: ''
})

const dialog = reactive({
  visible: false,
  saving: false,
  id: null,
  form: emptyForm()
})

function emptyForm() {
  return {
    title: '',
    content: '',
    displayType: 0,
    targetType: 0,
    targetUserIds: '',
    forceRead: 0,
    timeRange: null
  }
}

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res = await api.get('/admin/notice/list', {
      params: {
        pageNum: query.pageNum,
        pageSize: query.pageSize,
        status: query.status === '' ? undefined : query.status,
        displayType: query.displayType === '' ? undefined : query.displayType,
        keyword: query.keyword || undefined
      }
    })
    list.value = res.data.list || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function search() {
  query.pageNum = 1
  load()
}

function openCreate() {
  dialog.id = null
  dialog.form = emptyForm()
  dialog.visible = true
}

function openEdit(row) {
  dialog.id = row.id
  dialog.form = {
    title: row.title,
    content: row.content,
    displayType: row.displayType,
    targetType: row.targetType,
    targetUserIds: row.targetUserIds,
    forceRead: row.forceRead,
    timeRange: (row.startTime && row.endTime) ? [row.startTime, row.endTime] : null
  }
  dialog.visible = true
}

async function save() {
  if (!dialog.form.title.trim()) {
    ElMessage.warning('请填写公告标题')
    return
  }
  if (!dialog.form.content.trim()) {
    ElMessage.warning('请填写公告内容')
    return
  }
  if (dialog.form.targetType === 2 && !dialog.form.targetUserIds.trim()) {
    ElMessage.warning('指定用户模式下请填写用户ID')
    return
  }
  dialog.saving = true
  try {
    const payload = {
      title: dialog.form.title,
      content: dialog.form.content,
      displayType: dialog.form.displayType,
      targetType: dialog.form.targetType,
      targetUserIds: dialog.form.targetUserIds,
      forceRead: dialog.form.forceRead,
      startTime: dialog.form.timeRange?.[0] || null,
      endTime: dialog.form.timeRange?.[1] || null
    }
    if (dialog.id) {
      await api.put('/admin/notice', { id: dialog.id, ...payload })
    } else {
      await api.post('/admin/notice', payload)
    }
    ElMessage.success('保存成功')
    dialog.visible = false
    load()
  } finally {
    dialog.saving = false
  }
}

function changeStatus(row, status) {
  api.put(`/admin/notice/status/${row.id}?status=${status}`).then(() => {
    ElMessage.success(status === 1 ? '已启用' : '已停用')
    load()
  })
}

function remove(row) {
  ElMessageBox.confirm(`确认删除公告「${row.title}」？删除后不可恢复。`, '删除公告', { type: 'warning' })
    .then(async () => {
      await api.delete(`/admin/notice/${row.id}`)
      ElMessage.success('已删除')
      load()
    })
    .catch(() => {})
}

function displayText(t) {
  return { 0: '弹窗', 1: '顶部横幅', 2: '消息中心' }[t] || '未知'
}

function targetText(row) {
  if (row.targetType === 1) return '会员用户'
  if (row.targetType === 2) return `指定(${(row.targetUserIds || '').split(',').length}人)`
  return '全部用户'
}

function timeRange(row) {
  const start = row.startTime ? String(row.startTime).replace('T', ' ').slice(0, 16) : '立即'
  const end = row.endTime ? String(row.endTime).replace('T', ' ').slice(0, 16) : '永久'
  return `${start} ~ ${end}`
}
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.mini-tag {
  margin-right: 4px;
}

/* 表格内按钮统一垂直居中，避免基线错位 */
:deep(.el-table .el-button) {
  vertical-align: middle;
}

.metric {
  margin-right: 8px;
  font-size: 12px;
  color: #666;
  vertical-align: middle;
  white-space: nowrap;
}

.time-range {
  font-size: 12px;
  color: #666;
}

.form-tip {
  margin-left: 10px;
  font-size: 12px;
  color: #999;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
</style>
