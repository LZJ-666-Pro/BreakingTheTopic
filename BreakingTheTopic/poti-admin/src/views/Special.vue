<template>
  <div class="page-card">
    <!-- 顶部操作 -->
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="搜索专题名称" clearable style="width: 220px" @keyup.enter="search" @clear="search" />
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px" @change="search">
        <el-option label="已上架" :value="1" />
        <el-option label="已下架" :value="0" />
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button type="success" @click="openDialog()">+ 新增专题</el-button>
    </div>

    <!-- 列表 -->
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="专题名称" min-width="180" show-overflow-tooltip />
      <el-table-column label="难度" width="80">
        <template #default="{ row }">
          <el-tag :type="difficultyType(row.difficulty)" size="small">{{ difficultyLabel(row.difficulty) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="questionCount" label="题目数" width="80" />
      <el-table-column prop="estimatedDays" label="预计天数" width="90" />
      <el-table-column label="解锁规则" width="100">
        <template #default="{ row }">{{ row.unlockType === 1 ? '按顺序解锁' : '自由刷' }}</template>
      </el-table-column>
      <el-table-column prop="sort" label="权重" width="70" />
      <el-table-column label="上架" width="80">
        <template #default="{ row }">
          <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="toggleStatus(row)" />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170">
        <template #default="{ row }">{{ fmtTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      class="pager"
      v-model:current-page="query.pageNum"
      v-model:page-size="query.pageSize"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      :total="total"
      @size-change="search"
      @current-change="loadList"
    />

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialog.visible" :title="dialog.id ? '编辑专题' : '新增专题'" width="640px">
      <el-form label-width="90px">
        <el-form-item label="专题名称" required>
          <el-input v-model="dialog.form.name" placeholder="如：动态规划专题" maxlength="50" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="dialog.form.description" type="textarea" :rows="2" placeholder="一句话介绍这个专题" maxlength="200" />
        </el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="难度">
              <el-select v-model="dialog.form.difficulty">
                <el-option label="简单" :value="1" />
                <el-option label="中等" :value="2" />
                <el-option label="困难" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预计天数">
              <el-input-number v-model="dialog.form.estimatedDays" :min="1" :max="365" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="解锁规则">
              <el-select v-model="dialog.form.unlockType">
                <el-option label="自由刷" :value="0" />
                <el-option label="按顺序解锁" :value="1" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="推荐权重">
              <el-input-number v-model="dialog.form.sort" :min="0" :max="9999" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="是否上架">
          <el-switch v-model="dialog.form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="选择题目" required>
          <el-select
            v-model="dialog.form.questionIds"
            multiple filterable remote
            :remote-method="searchQuestions"
            :loading="qLoading"
            placeholder="输入关键字搜索题目，勾选顺序即题目顺序"
            style="width: 100%"
          >
            <el-option v-for="q in qOptions" :key="q.id" :label="q.id + '. ' + q.content" :value="q.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../utils/api'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', status: null })

const dialog = reactive({
  visible: false,
  id: null,
  form: { name: '', description: '', difficulty: 2, estimatedDays: 7, unlockType: 0, sort: 0, status: 1, questionIds: [] }
})

const qOptions = ref([])
const qLoading = ref(false)
const saving = ref(false)

const difficultyLabel = (d) => ({ 1: '简单', 2: '中等', 3: '困难' }[d] || '中等')
const difficultyType = (d) => ({ 1: 'success', 2: 'warning', 3: 'danger' }[d] || 'info')
const fmtTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '')

function search() {
  query.pageNum = 1
  loadList()
}

async function loadList() {
  loading.value = true
  try {
    const res = await api.get('/admin/special/list', { params: { ...query } })
    list.value = res.data.list || []
    total.value = res.data.total || 0
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  dialog.id = row ? row.id : null
  dialog.form = row
    ? { name: row.name, description: row.description, difficulty: row.difficulty, estimatedDays: row.estimatedDays, unlockType: row.unlockType, sort: row.sort, status: row.status, questionIds: [] }
    : { name: '', description: '', difficulty: 2, estimatedDays: 7, unlockType: 0, sort: 0, status: 1, questionIds: [] }
  qOptions.value = []
  if (row) {
    // 编辑时先拉详情（含题目列表），把已有题目回填进选项
    api.get(`/admin/special/${row.id}`).then((res) => {
      const qs = res.data.questions || []
      qOptions.value = qs.map((q) => ({ id: q.id, content: q.title || q.content }))
      dialog.form.questionIds = qs.map((q) => q.id)
    })
  }
  dialog.visible = true
}

async function searchQuestions(keyword) {
  if (!keyword || !keyword.trim()) return
  qLoading.value = true
  try {
    const res = await api.get('/admin/question/page', { params: { page: 1, size: 20, keyword: keyword.trim() } })
    const records = res.data.records || []
    const known = new Set(qOptions.value.map((o) => o.id))
    qOptions.value = [...qOptions.value, ...records.filter((q) => !known.has(q.id)).map((q) => ({ id: q.id, content: q.title || q.content }))]
  } finally {
    qLoading.value = false
  }
}

async function toggleStatus(row) {
  try {
    await api.put('/admin/special', { id: row.id, status: row.status, name: row.name })
    ElMessage.success(row.status === 1 ? '已上架' : '已下架')
  } catch (e) {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error(e?.message || '操作失败')
  }
}

async function save() {
  const f = dialog.form
  if (!f.name || !f.name.trim()) return ElMessage.warning('请填写专题名称')
  if (!f.questionIds.length) return ElMessage.warning('请至少选择一道题目')
  saving.value = true
  try {
    if (dialog.id) {
      await api.put('/admin/special', { id: dialog.id, ...f })
    } else {
      await api.post('/admin/special', f)
    }
    ElMessage.success('保存成功')
    dialog.visible = false
    loadList()
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

function remove(row) {
  ElMessageBox.confirm(`确定删除专题「${row.name}」？`, '提示', { type: 'warning' }).then(async () => {
    try {
      await api.delete(`/admin/special/${row.id}`)
      ElMessage.success('删除成功')
      loadList()
    } catch (e) {
      ElMessage.error(e?.message || '删除失败')
    }
  }).catch(() => {})
}

onMounted(loadList)
</script>

<style scoped>
.page-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
.toolbar .el-button {
  margin-left: 0;
}
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
