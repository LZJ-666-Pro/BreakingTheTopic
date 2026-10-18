<template>
  <div class="page-card">
    <!-- 头部：返回 + 标题 -->
    <div class="header-row">
      <el-button :icon="ArrowLeft" @click="goBack">返回</el-button>
      <div class="header-title">{{ isEdit ? '编辑竞赛' : '新建竞赛' }}</div>
    </div>

    <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px" class="edit-form" v-loading="loading">
      <el-form-item label="竞赛名称" prop="title">
        <el-input v-model="form.title" placeholder="请输入竞赛名称" maxlength="100" show-word-limit style="max-width: 480px" />
      </el-form-item>
      <el-form-item label="竞赛说明" prop="description">
        <el-input v-model="form.description" type="textarea" :rows="3" placeholder="选填，向用户展示的竞赛介绍" maxlength="500" show-word-limit style="max-width: 640px" />
      </el-form-item>
      <el-form-item label="题库分类" prop="categoryId">
        <el-select v-model="form.categoryId" placeholder="选择题库分类（抽题范围）" filterable style="max-width: 480px; width: 100%">
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
          style="max-width: 480px; width: 100%"
        />
      </el-form-item>
      <el-form-item label="结束时间" prop="endTime">
        <el-date-picker
          v-model="form.endTime"
          type="datetime"
          placeholder="选择结束时间"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="max-width: 480px; width: 100%"
        />
      </el-form-item>
      <el-form-item label="每题分值" prop="scorePerQuestion">
        <el-input-number v-model="form.scorePerQuestion" :min="1" :max="100" />
        <span class="form-tip">总分 = 题目数 × 每题分值</span>
      </el-form-item>
      <el-form-item>
        <el-button @click="goBack">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import api from '../utils/api'
import { formatDate } from '../utils/formatDate'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)

const loading = ref(false)
const saving = ref(false)
const formRef = ref(null)

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

/** 统一时间显示：兼容 ISO（含T）与空格两种格式 */
const formatTime = t => (t ? formatDate(String(t).replace('T', ' ')) : '-')

const goBack = () => {
  if (window.history.length > 1) router.back()
  else router.push('/competition')
}

/** 分类下拉（抽题范围） */
const categoryOptions = ref([])
const loadCategories = async () => {
  if (categoryOptions.value.length > 0) return
  try {
    const res = await api.get('/admin/category/list')
    categoryOptions.value = res.data || []
  } catch (e) {
    ElMessage.error(e?.message || '加载分类失败')
  }
}

async function loadDetail() {
  loading.value = true
  try {
    // 后端无独立详情接口：复用分页接口按 id 定位，回填口径与原弹窗一致
    const id = Number(route.params.id)
    let pageNum = 1
    let row = null
    while (!row) {
      const res = await api.post('/admin/competition/page', { pageNum, pageSize: 100 })
      const list = res.data.list || []
      row = list.find(c => Number(c.id) === id) || null
      if (row || list.length < 100) break
      pageNum++
    }
    if (!row) {
      ElMessage.error('竞赛不存在或已被删除')
      goBack()
      return
    }
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
  } catch (e) {
    ElMessage.error(e?.message || '加载竞赛详情失败')
  } finally {
    loading.value = false
  }
}

async function save() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  saving.value = true
  try {
    await api.post('/admin/competition/save', {
      id: isEdit.value ? route.params.id : undefined,
      title: form.title.trim(),
      description: form.description || '',
      categoryId: form.categoryId,
      questionCount: form.questionCount,
      durationMinutes: form.durationMinutes,
      startTime: form.startTime,
      endTime: form.endTime,
      scorePerQuestion: form.scorePerQuestion
    })
    ElMessage.success(isEdit.value ? '竞赛已保存' : '竞赛草稿已创建')
    goBack()
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadCategories()
  if (isEdit.value) loadDetail()
})
</script>

<style scoped>
.page-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.header-row {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 20px;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.edit-form {
  max-width: 860px;
}

.form-tip {
  margin-left: 12px;
  font-size: 12px;
  color: #999;
}
</style>
