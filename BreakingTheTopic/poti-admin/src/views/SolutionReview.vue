<template>
  <div class="page">
    <el-card shadow="never">
      <div class="toolbar">
        <el-tabs v-model="query.status" class="status-tabs" @tab-change="search">
          <el-tab-pane label="待审核" name="0" />
          <el-tab-pane label="已通过" name="1" />
          <el-tab-pane label="已拒绝" name="2" />
          <el-tab-pane label="全部" name="-1" />
        </el-tabs>
        <div class="toolbar-right">
          <el-checkbox v-model="onlyReported" border @change="search">只看被举报</el-checkbox>
          <el-input
            v-model="query.keyword"
            placeholder="搜索标题 / 作者"
            clearable
            style="width: 200px"
            @keyup.enter="search"
            @clear="search"
          />
          <el-button type="primary" :icon="Search" @click="search">搜索</el-button>
        </div>
      </div>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="64" />
        <el-table-column prop="title" label="题解标题" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <el-link type="primary" @click="openDetail(row)">{{ row.title }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="questionId" label="题目ID" width="80" />
        <el-table-column prop="authorName" label="作者" width="120" show-overflow-tooltip />
        <el-table-column label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isOfficial === 1 ? 'danger' : 'info'" size="small">
              {{ row.isOfficial === 1 ? '官方' : '用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="数据" width="170">
          <template #default="{ row }">
            <span class="metric">👁 {{ row.viewCount }}</span>
            <span class="metric">👍 {{ row.likeCount }}</span>
            <span class="metric">⭐ {{ row.starCount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="举报" width="70">
          <template #default="{ row }">
            <el-badge :value="row.reportCount" :hidden="!row.reportCount" type="danger">
              <span>{{ row.reportCount }}</span>
            </el-badge>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button link type="primary" @click="review(row, 1)">通过</el-button>
              <el-button link type="danger" @click="review(row, 2)">拒绝</el-button>
            </template>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-dropdown trigger="click" class="more-dropdown" @command="(cmd) => handleCommand(cmd, row)">
              <el-button link type="primary">更多<el-icon class="more-icon"><arrow-down /></el-icon></el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="quality">{{ row.quality === 1 ? '取消优质' : '设为优质' }}</el-dropdown-item>
                  <el-dropdown-item command="pinned">{{ row.pinned === 1 ? '取消置顶' : '置顶题解' }}</el-dropdown-item>
                  <el-dropdown-item command="clearReport" :disabled="!row.reportCount">清零举报</el-dropdown-item>
                  <el-dropdown-item command="delete" divided style="color: var(--el-color-danger)">删除题解</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
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

    <!-- 拒绝原因弹窗 -->
    <el-dialog v-model="rejectDialog.visible" title="拒绝题解" width="420px">
      <el-input
        v-model="rejectDialog.reason"
        type="textarea"
        :rows="3"
        maxlength="200"
        show-word-limit
        placeholder="请填写拒绝原因（会展示给用户）"
      />
      <template #footer>
        <el-button @click="rejectDialog.visible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认拒绝</el-button>
      </template>
    </el-dialog>

    <!-- 题解详情弹窗 -->
    <el-dialog v-model="detailDialog.visible" :title="detailDialog.row?.title" width="680px">
      <div v-if="detailDialog.row" class="detail-body">
        <div class="detail-meta">
          <el-tag :type="statusType(detailDialog.row.status)" size="small">{{ statusText(detailDialog.row.status) }}</el-tag>
          <span>题目ID：{{ detailDialog.row.questionId }}</span>
          <span>作者：{{ detailDialog.row.authorName }}</span>
          <span v-if="detailDialog.row.rejectReason" class="reject-reason">拒绝原因：{{ detailDialog.row.rejectReason }}</span>
        </div>
        <pre class="md-preview">{{ detailDialog.row.content }}</pre>
      </div>
    </el-dialog>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editDialog.visible" title="编辑题解" width="680px">
      <el-form label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="editDialog.form.title" maxlength="200" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input
            v-model="editDialog.form.content"
            type="textarea"
            :rows="14"
            placeholder="支持 Markdown 语法"
          />
        </el-form-item>
        <el-form-item label="优质">
          <el-switch v-model="editDialog.form.quality" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="置顶">
          <el-switch v-model="editDialog.form.pinned" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="editDialog.saving" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, ArrowDown } from '@element-plus/icons-vue'
import api from '../utils/api'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const onlyReported = ref(false)

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  status: '0',
  keyword: ''
})

const rejectDialog = reactive({ visible: false, id: null, reason: '' })
const detailDialog = reactive({ visible: false, row: null })
const editDialog = reactive({ visible: false, saving: false, id: null, form: { title: '', content: '', quality: 0, pinned: 0 } })

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res = await api.get('/admin/solution/list', {
      params: {
        pageNum: query.pageNum,
        pageSize: query.pageSize,
        status: query.status === '-1' ? undefined : Number(query.status),
        keyword: query.keyword || undefined,
        reported: onlyReported.value ? 1 : undefined
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

function review(row, status) {
  if (status === 1) {
    ElMessageBox.confirm(`确认通过题解「${row.title}」？通过后将在题目题解 Tab 中展示。`, '审核通过', { type: 'success' })
      .then(async () => {
        await api.put(`/admin/solution/review/${row.id}`, { status: 1 })
        ElMessage.success('已通过')
        load()
      })
      .catch(() => {})
  } else {
    rejectDialog.id = row.id
    rejectDialog.reason = ''
    rejectDialog.visible = true
  }
}

async function confirmReject() {
  await api.put(`/admin/solution/review/${rejectDialog.id}`, {
    status: 2,
    rejectReason: rejectDialog.reason || '内容质量不符合要求'
  })
  ElMessage.success('已拒绝')
  rejectDialog.visible = false
  load()
}

function openEdit(row) {
  editDialog.id = row.id
  editDialog.form = {
    title: row.title,
    content: row.content,
    quality: row.quality,
    pinned: row.pinned
  }
  editDialog.visible = true
}

async function saveEdit() {
  if (!editDialog.form.title.trim()) {
    ElMessage.warning('标题不能为空')
    return
  }
  editDialog.saving = true
  try {
    await api.put('/admin/solution', { id: editDialog.id, ...editDialog.form })
    ElMessage.success('保存成功')
    editDialog.visible = false
    load()
  } finally {
    editDialog.saving = false
  }
}

function handleCommand(cmd, row) {
  if (cmd === 'quality') {
    toggleFlag(row, 'quality', row.quality === 1 ? 0 : 1)
  } else if (cmd === 'pinned') {
    toggleFlag(row, 'pinned', row.pinned === 1 ? 0 : 1)
  } else if (cmd === 'clearReport') {
    api.put(`/admin/solution/report/clear/${row.id}`).then(() => {
      ElMessage.success('举报已清零')
      load()
    })
  } else if (cmd === 'delete') {
    ElMessageBox.confirm(`确认删除题解「${row.title}」？删除后不可恢复。`, '删除题解', { type: 'warning' })
      .then(async () => {
        await api.delete(`/admin/solution/${row.id}`)
        ElMessage.success('已删除')
        load()
      })
      .catch(() => {})
  }
}

async function toggleFlag(row, field, value) {
  await api.put('/admin/solution', { id: row.id, [field]: value })
  ElMessage.success(value === 1 ? '已设置' : '已取消')
  load()
}

function openDetail(row) {
  detailDialog.row = row
  detailDialog.visible = true
}

function statusText(s) {
  return { 0: '待审核', 1: '已通过', 2: '已拒绝' }[s] || '未知'
}

function statusType(s) {
  return { 0: 'warning', 1: 'success', 2: 'danger' }[s] || 'info'
}

function formatTime(t) {
  if (!t) return '-'
  return String(t).replace('T', ' ').slice(0, 19)
}
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
  padding-top: 4px;
}

/* 表格内按钮/下拉统一垂直居中，避免基线错位 */
:deep(.el-table .el-button) {
  vertical-align: middle;
}

:deep(.el-table .el-dropdown) {
  vertical-align: middle;
}

.metric {
  margin-right: 8px;
  font-size: 12px;
  color: #666;
  vertical-align: middle;
  white-space: nowrap;
}

.more-dropdown {
  margin-left: 12px;
  vertical-align: middle;
}

.more-icon {
  margin-left: 2px;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 12px;
  font-size: 13px;
  color: #666;
  flex-wrap: wrap;
}

.reject-reason {
  color: #f56c6c;
}

.md-preview {
  background: #f7f8fa;
  border-radius: 8px;
  padding: 16px;
  max-height: 420px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: inherit;
  font-size: 14px;
  line-height: 1.7;
  margin: 0;
}
</style>
