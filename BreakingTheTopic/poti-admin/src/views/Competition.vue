<template>
  <div class="page-container">
    <div class="page-header">
      <h2>竞赛活动</h2>
      <div class="header-actions">
        <el-button type="primary" @click="openCreate">
          <el-icon><Plus /></el-icon>
          新建竞赛
        </el-button>
        <el-button @click="loadList" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <el-card class="content-card" shadow="never">
      <!-- 筛选栏：名称 + 动态状态（前端过滤当前页） -->
      <div class="filter-bar">
        <el-input
          v-model="query.title"
          placeholder="按竞赛名称搜索"
          clearable
          style="width: 220px"
          @keyup.enter="searchList"
          @clear="searchList"
        />
        <el-select v-model="query.state" placeholder="状态" clearable style="width: 130px" @change="searchList">
          <el-option label="全部" value="" />
          <el-option v-for="(v, k) in stateMap" :key="k" :label="v.label" :value="k" />
        </el-select>
        <el-button type="primary" @click="searchList">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </div>

      <el-table :data="filteredList" v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="title" label="名称" min-width="150" show-overflow-tooltip />
        <el-table-column label="分类" width="120" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ categoryName(row.categoryId) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="questionCount" label="题目数" width="75" align="center" />
        <el-table-column label="时长" width="80" align="center">
          <template #default="{ row }">{{ row.durationMinutes }}分钟</template>
        </el-table-column>
        <el-table-column label="时间窗" width="320">
          <template #default="{ row }">
            <div class="time-cell">
              <div>{{ formatTime(row.startTime) }}</div>
              <div class="text-muted">至 {{ formatTime(row.endTime) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="stateMap[row.state]?.type || 'info'" size="small">
              {{ stateMap[row.state]?.label || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="signupCount" label="报名数" width="75" align="center" />
        <el-table-column prop="submitCount" label="交卷数" width="75" align="center" />
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.state === 'draft'" type="primary" link size="small" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="row.state === 'draft'" type="primary" link size="small" @click="handlePublish(row)">发布</el-button>
            <el-button
              v-if="row.state !== 'draft'"
              type="primary"
              link
              size="small"
              @click="openRecords(row)"
            >
              查看成绩
            </el-button>
            <el-button
              v-if="row.state === 'draft' || row.state === 'ended'"
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

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadList"
          @current-change="loadList"
        />
      </div>
    </el-card>

    <!-- 新建 / 编辑竞赛弹窗 -->
    <el-dialog v-model="formVisible" :title="editId ? '编辑竞赛' : '新建竞赛'" width="560px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="竞赛名称" prop="title">
          <el-input v-model="form.title" placeholder="请输入竞赛名称" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="竞赛说明" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="选填，向用户展示的竞赛介绍" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="题库分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="选择题库分类（抽题范围）" filterable style="width: 100%">
            <el-option v-for="c in categoryOptions" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="题目数量" prop="questionCount">
          <el-input-number v-model="form.questionCount" :min="1" :max="50" />
          <span class="form-tip">1-50 题，开赛时按分类随机抽题</span>
        </el-form-item>
        <el-form-item label="答题时长" prop="durationMinutes">
          <el-input-number v-model="form.durationMinutes" :min="1" :max="180" />
          <span class="form-tip">单位：分钟，1-180</span>
        </el-form-item>
        <el-form-item label="开赛时间" prop="startTime">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            placeholder="选择开赛时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            placeholder="选择结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="每题分值" prop="scorePerQuestion">
          <el-input-number v-model="form.scorePerQuestion" :min="1" :max="100" />
          <span class="form-tip">总分 = 题目数 × 每题分值</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <!-- 成绩改为独立页面：/competition/records/:id -->
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import api from '../utils/api'
import { getDefaultPageSize } from '../utils/config'
import { formatDate } from '../utils/formatDate'

const loading = ref(false)
const router = useRouter()
const list = ref([])
const currentPage = ref(1)
// 每页条数持久化：用户选择优先，其次系统配置默认值
const pageSize = ref(Number(localStorage.getItem('competition_page_size')) || getDefaultPageSize())
watch(pageSize, v => localStorage.setItem('competition_page_size', String(v)))
const total = ref(0)

// 筛选条件：名称走后端，状态按返回 state 在前端过滤当前页
const query = reactive({ title: '', state: '' })

// 动态状态映射（后端推导返回）
const stateMap = {
  draft: { label: '草稿', type: 'info' },
  signup: { label: '报名中', type: 'warning' },
  ongoing: { label: '进行中', type: 'success' },
  ended: { label: '已结束', type: 'danger' }
}

const filteredList = computed(() => {
  if (!query.state) return list.value
  return list.value.filter(item => item.state === query.state)
})

/** 分类下拉与名称回显 */
const categoryOptions = ref([])
const categoryMap = computed(() => {
  const map = {}
  categoryOptions.value.forEach(c => { map[c.id] = c.name })
  return map
})
const categoryName = id => categoryMap.value[id] || '未分类'

/** 统一时间显示：兼容 ISO（含T）与空格两种格式 */
const formatTime = t => (t ? formatDate(String(t).replace('T', ' ')) : '-')

/** 加载竞赛分页列表 */
const loadList = async () => {
  loading.value = true
  try {
    const res = await api.post('/admin/competition/page', {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      title: query.title.trim() || undefined
    })
    list.value = res.data.list || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('加载竞赛列表失败', error)
  } finally {
    loading.value = false
  }
}

const searchList = () => {
  currentPage.value = 1
  loadList()
}

const resetQuery = () => {
  query.title = ''
  query.state = ''
  searchList()
}

/** 加载分类下拉（用于表单选择与表格回显） */
const loadCategories = async () => {
  if (categoryOptions.value.length > 0) return
  try {
    const res = await api.get('/admin/category/list')
    if (res.code === 200) {
      categoryOptions.value = res.data || []
    }
  } catch (error) {
    console.error('加载分类失败', error)
  }
}

// ===== 新建 / 编辑 =====
const formVisible = ref(false)
const saving = ref(false)
const formRef = ref(null)
const editId = ref(null)
const form = reactive({
  title: '',
  description: '',
  categoryId: null,
  questionCount: 10,
  durationMinutes: 30,
  startTime: '',
  endTime: '',
  scorePerQuestion: 10
})

// 结束时间晚于开赛时间校验
const validateEndTime = (rule, value, callback) => {
  if (!value) return callback(new Error('请选择结束时间'))
  if (form.startTime && new Date(value.replace(' ', 'T')) <= new Date(form.startTime.replace(' ', 'T'))) {
    return callback(new Error('结束时间必须晚于开赛时间'))
  }
  callback()
}

const formRules = {
  title: [
    { required: true, message: '请输入竞赛名称', trigger: 'blur' },
    { max: 100, message: '竞赛名称不能超过100字', trigger: 'blur' }
  ],
  categoryId: [{ required: true, message: '请选择题库分类', trigger: 'change' }],
  questionCount: [{ required: true, message: '请填写题目数量', trigger: 'blur' }],
  durationMinutes: [{ required: true, message: '请填写答题时长', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开赛时间', trigger: 'change' }],
  endTime: [{ required: true, validator: validateEndTime, trigger: 'change' }],
  scorePerQuestion: [{ required: true, message: '请填写每题分值', trigger: 'blur' }]
}

const openCreate = async () => {
  editId.value = null
  Object.assign(form, {
    title: '',
    description: '',
    categoryId: null,
    questionCount: 10,
    durationMinutes: 30,
    startTime: '',
    endTime: '',
    scorePerQuestion: 10
  })
  formVisible.value = true
  await loadCategories()
}

const openEdit = async (row) => {
  editId.value = row.id
  Object.assign(form, {
    title: row.title,
    description: row.description || '',
    categoryId: row.categoryId,
    questionCount: row.questionCount,
    durationMinutes: row.durationMinutes,
    startTime: formatTime(row.startTime),
    endTime: formatTime(row.endTime),
    scorePerQuestion: row.scorePerQuestion
  })
  formVisible.value = true
  await loadCategories()
}

const submitForm = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  saving.value = true
  try {
    const res = await api.post('/admin/competition/save', {
      id: editId.value || undefined,
      title: form.title.trim(),
      description: form.description || '',
      categoryId: form.categoryId,
      questionCount: form.questionCount,
      durationMinutes: form.durationMinutes,
      startTime: form.startTime,
      endTime: form.endTime,
      scorePerQuestion: form.scorePerQuestion
    })
    if (res.code === 200) {
      ElMessage.success(editId.value ? '竞赛已保存' : '竞赛草稿已创建')
      formVisible.value = false
      searchList()
    }
  } catch (error) {
    console.error('保存竞赛失败', error)
  } finally {
    saving.value = false
  }
}

// ===== 发布 / 删除 =====
const handlePublish = async (row) => {
  try {
    await ElMessageBox.confirm(`确定发布竞赛「${row.title}」吗？发布后不可再编辑。`, '发布确认', { type: 'warning' })
  } catch {
    return
  }
  try {
    const res = await api.post(`/admin/competition/publish/${row.id}`)
    if (res.code === 200) {
      ElMessage.success('已发布')
      loadList()
    }
  } catch (error) {
    console.error('发布失败', error)
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除竞赛「${row.title}」吗？删除后不可恢复。`, '删除确认', { type: 'warning' })
  } catch {
    return
  }
  try {
    const res = await api.delete(`/admin/competition/${row.id}`)
    if (res.code === 200) {
      ElMessage.success('已删除')
      loadList()
    }
  } catch (error) {
    console.error('删除失败', error)
  }
}

// ===== 查看成绩：跳转独立成绩页 =====
const openRecords = (row) => {
  router.push({ path: `/competition/records/${row.id}`, query: { title: row.title } })
}

onMounted(() => {
  loadList()
  loadCategories()
})
</script>

<style scoped>
.page-container {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.form-tip {
  margin-left: 12px;
  font-size: 12px;
  color: #999;
}

.time-cell {
  font-size: 13px;
  line-height: 1.5;
}

.rank-top {
  font-weight: 700;
  color: #e6a23c;
}

.text-muted {
  color: #999;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}
</style>
