<template>
  <div class="page-card">
    <!-- 今日状态 -->
    <el-alert
      v-if="todayInfo"
      :type="todayInfo.isToday ? 'success' : 'warning'"
      :title="todayInfo.isToday
        ? `今日一题已配置：${todayInfo.content}`
        : '今天（' + today + '）还没配置每日一题，用户端将看不到今日一题'"
      show-icon
      :closable="false"
      style="margin-bottom: 16px"
    />

    <!-- 顶部操作 -->
    <div class="toolbar">
      <el-button type="success" @click="openDialog()">+ 配置每日一题</el-button>
    </div>

    <!-- 列表 -->
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="date" label="日期" width="120" />
      <el-table-column label="题目" min-width="240" show-overflow-tooltip>
        <template #default="{ row }">
          <span :class="{ deleted: row.content === '（题目已删除）' }">{{ row.content }}</span>
        </template>
      </el-table-column>
      <el-table-column label="难度" width="80">
        <template #default="{ row }">
          <el-tag v-if="row.difficulty" :type="difficultyType(row.difficulty)" size="small">{{ difficultyLabel(row.difficulty) }}</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="reason" label="推荐理由" min-width="160" show-overflow-tooltip />
      <el-table-column prop="hint" label="解题提示" min-width="160" show-overflow-tooltip />
      <el-table-column prop="doneCount" label="完成人数" width="90" />
      <el-table-column label="正确率" width="90">
        <template #default="{ row }">{{ row.accuracy == null ? '-' : row.accuracy + '%' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="130" fixed="right">
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
    <el-dialog v-model="dialog.visible" :title="dialog.id ? '编辑每日一题' : '配置每日一题'" width="520px">
      <el-form label-width="90px">
        <el-form-item label="日期" required>
          <el-date-picker v-model="dialog.form.date" type="date" value-format="YYYY-MM-DD" :clearable="false" style="width: 100%" />
        </el-form-item>
        <el-form-item label="选择题目" required>
          <el-select
            v-model="dialog.form.questionId"
            filterable remote
            :remote-method="searchQuestions"
            :loading="qLoading"
            placeholder="输入关键字搜索题目"
            style="width: 100%"
          >
            <el-option v-for="q in qOptions" :key="q.id" :label="q.id + '. ' + q.content" :value="q.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="推荐理由">
          <el-input v-model="dialog.form.reason" placeholder="如：经典高频题，热热身" maxlength="100" />
        </el-form-item>
        <el-form-item label="解题提示">
          <el-input v-model="dialog.form.hint" placeholder="可选，展示在题目页" maxlength="100" />
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
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../utils/api'

const today = new Date().toISOString().slice(0, 10)
const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10 })

const dialog = reactive({
  visible: false,
  id: null,
  form: { date: today, questionId: null, reason: '', hint: '' }
})

const qOptions = ref([])
const qLoading = ref(false)
const saving = ref(false)

const difficultyLabel = (d) => ({ 1: '简单', 2: '中等', 3: '困难' }[d] || '中等')
const difficultyType = (d) => ({ 1: 'success', 2: 'warning', 3: 'danger' }[d] || 'info')

/** 第一条若为今天则给出状态提示 */
const todayInfo = computed(() => {
  const first = list.value[0]
  if (!first) return null
  return { isToday: first.date === today, content: first.content, date: first.date }
})

function search() {
  query.pageNum = 1
  loadList()
}

async function loadList() {
  loading.value = true
  try {
    const res = await api.get('/admin/daily/list', { params: { ...query } })
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
    ? { date: row.date, questionId: row.questionId, reason: row.reason || '', hint: row.hint || '' }
    : { date: today, questionId: null, reason: '', hint: '' }
  qOptions.value = []
  if (row && row.questionId && row.content) {
    qOptions.value = [{ id: row.questionId, content: row.content }]
  }
  dialog.visible = true
}

async function searchQuestions(keyword) {
  if (!keyword || !keyword.trim()) return
  qLoading.value = true
  try {
    const res = await api.get('/admin/question/page', { params: { page: 1, size: 20, keyword: keyword.trim() } })
    qOptions.value = (res.data.records || []).map((q) => ({ id: q.id, content: q.content }))
  } finally {
    qLoading.value = false
  }
}

async function save() {
  const f = dialog.form
  if (!f.questionId) return ElMessage.warning('请选择题目')
  if (!f.date) return ElMessage.warning('请选择日期')
  saving.value = true
  try {
    if (dialog.id) {
      await api.put('/admin/daily', { id: dialog.id, ...f })
    } else {
      await api.post('/admin/daily', f)
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
  ElMessageBox.confirm(`确定删除 ${row.date} 的每日一题？`, '提示', { type: 'warning' }).then(async () => {
    try {
      await api.delete(`/admin/daily/${row.id}`)
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
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
.deleted {
  color: #bbb;
  text-decoration: line-through;
}
</style>
