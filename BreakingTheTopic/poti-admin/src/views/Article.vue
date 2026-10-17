<template>
  <div class="page">
    <el-card shadow="never">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-select v-model="query.status" placeholder="发布状态" clearable style="width: 130px" @change="search">
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="已下架" :value="2" />
          </el-select>
          <el-select v-model="query.category" placeholder="文章分类" clearable style="width: 140px" @change="search">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
          <el-input
            v-model="query.keyword"
            placeholder="搜索标题 / 标签"
            clearable
            style="width: 200px"
            @keyup.enter="search"
            @clear="search"
          />
          <el-button type="primary" :icon="Search" @click="search">搜索</el-button>
        </div>
        <el-button type="primary" :icon="Plus" @click="openCreate">新增文章</el-button>
      </div>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="64" />
        <el-table-column label="标题" min-width="240">
          <template #default="{ row }">
            <div class="title-cell">
              <el-tag v-if="row.pinned === 1" type="danger" size="small" class="mini-tag">置顶</el-tag>
              <el-tag v-if="row.recommended === 1" type="warning" size="small" class="mini-tag">荐</el-tag>
              <span>{{ row.title }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="110" />
        <el-table-column prop="authorName" label="作者" width="100" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="数据" width="190">
          <template #default="{ row }">
            <span class="metric">👁 {{ row.viewCount }}</span>
            <span class="metric">👍 {{ row.likeCount }}</span>
            <span class="metric">💬 {{ row.commentCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="170">
          <template #default="{ row }">{{ formatTime(row.publishTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status !== 1" link type="primary" @click="changeStatus(row, 1)">发布</el-button>
            <el-button v-if="row.status === 1" link type="danger" @click="changeStatus(row, 2)">下架</el-button>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
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
    <el-dialog v-model="dialog.visible" :title="dialog.id ? '编辑文章' : '新增文章'" width="760px" top="4vh">
      <el-form label-width="90px">
        <el-form-item label="标题" required>
          <el-input v-model="dialog.form.title" maxlength="200" placeholder="文章标题" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="dialog.form.category" placeholder="选择分类" style="width: 200px">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="dialog.form.tags" placeholder="多个标签用英文逗号分隔，如：Java,面试" />
        </el-form-item>
        <el-form-item label="封面URL">
          <el-input v-model="dialog.form.cover" placeholder="封面图地址（可选）" />
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="dialog.form.summary" type="textarea" :rows="2" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="正文" required>
          <el-input
            v-model="dialog.form.content"
            type="textarea"
            :rows="16"
            placeholder="支持 Markdown 语法"
          />
        </el-form-item>
        <el-form-item label="作者">
          <el-input v-model="dialog.form.authorName" style="width: 200px" />
        </el-form-item>
        <el-form-item label="发布状态">
          <el-radio-group v-model="dialog.form.status">
            <el-radio :value="0">草稿</el-radio>
            <el-radio :value="1">发布</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="置顶">
          <el-switch v-model="dialog.form.pinned" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="推荐">
          <el-switch v-model="dialog.form.recommended" :active-value="1" :inactive-value="0" />
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
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import api from '../utils/api'
import { getDefaultPageSize } from '../utils/config'

const categories = ['学习路线', '面试经验', '刷题技巧', '面经', '技术科普']

const loading = ref(false)
const list = ref([])
const total = ref(0)

const query = reactive({
  pageNum: 1,
  // 每页条数持久化：用户选择优先，其次系统配置默认值
  pageSize: Number(localStorage.getItem('article_page_size')) || getDefaultPageSize(),
  status: '',
  category: '',
  keyword: ''
})
watch(() => query.pageSize, v => localStorage.setItem('article_page_size', String(v)))

const dialog = reactive({
  visible: false,
  saving: false,
  id: null,
  form: emptyForm()
})

function emptyForm() {
  return {
    title: '',
    category: '',
    cover: '',
    tags: '',
    summary: '',
    content: '',
    authorName: '官方',
    pinned: 0,
    recommended: 0,
    status: 0
  }
}

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res = await api.get('/admin/article/list', {
      params: {
        pageNum: query.pageNum,
        pageSize: query.pageSize,
        status: query.status === '' ? undefined : query.status,
        category: query.category || undefined,
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
    category: row.category,
    cover: row.cover,
    tags: row.tags,
    summary: row.summary,
    content: row.content,
    authorName: row.authorName,
    pinned: row.pinned,
    recommended: row.recommended,
    status: row.status === 2 ? 0 : row.status
  }
  dialog.visible = true
}

async function save() {
  if (!dialog.form.title.trim()) {
    ElMessage.warning('请填写文章标题')
    return
  }
  if (!dialog.form.content.trim()) {
    ElMessage.warning('请填写文章正文')
    return
  }
  dialog.saving = true
  try {
    if (dialog.id) {
      await api.put('/admin/article', { id: dialog.id, ...dialog.form })
    } else {
      await api.post('/admin/article', dialog.form)
    }
    ElMessage.success('保存成功')
    dialog.visible = false
    load()
  } finally {
    dialog.saving = false
  }
}

function changeStatus(row, status) {
  const text = status === 1 ? '发布' : '下架'
  ElMessageBox.confirm(`确认${text}文章「${row.title}」？`, `${text}文章`, { type: status === 1 ? 'success' : 'warning' })
    .then(async () => {
      await api.put(`/admin/article/status/${row.id}?status=${status}`)
      ElMessage.success(`已${text}`)
      load()
    })
    .catch(() => {})
}

function remove(row) {
  ElMessageBox.confirm(`确认删除文章「${row.title}」？删除后不可恢复。`, '删除文章', { type: 'warning' })
    .then(async () => {
      await api.delete(`/admin/article/${row.id}`)
      ElMessage.success('已删除')
      load()
    })
    .catch(() => {})
}

function statusText(s) {
  return { 0: '草稿', 1: '已发布', 2: '已下架' }[s] || '未知'
}

function statusType(s) {
  return { 0: 'info', 1: 'success', 2: 'warning' }[s] || 'info'
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
  align-items: center;
  margin-bottom: 12px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-cell {
  display: flex;
  align-items: center;
  gap: 6px;
}

.mini-tag {
  flex-shrink: 0;
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

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
</style>
