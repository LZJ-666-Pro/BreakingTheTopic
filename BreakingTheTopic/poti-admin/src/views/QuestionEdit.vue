<template>
  <div class="page-card">
    <!-- 头部：返回 + 标题 -->
    <div class="header-row">
      <el-button :icon="ArrowLeft" @click="goBack">返回</el-button>
      <div class="header-title">{{ isEdit ? '编辑题目' : '添加题目' }}</div>
    </div>

    <el-form :model="form" :rules="rules" ref="formRef" label-width="80px" class="edit-form" v-loading="loading">
      <el-row>
        <el-col :span="8">
          <el-form-item label="分类" prop="categoryId">
            <el-select v-model="form.categoryId" placeholder="选择分类" filterable style="width: 100%">
              <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="难度" prop="difficulty">
            <el-select v-model="form.difficulty" placeholder="选择难度" style="width: 100%">
              <el-option label="简单" :value="1" />
              <el-option label="中等" :value="2" />
              <el-option label="困难" :value="3" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="状态" prop="status">
            <el-select v-model="form.status" placeholder="选择状态" style="width: 100%">
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="内容" prop="content">
        <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请输入题目内容" maxlength="500" show-word-limit />
      </el-form-item>
      <el-form-item label="选项" prop="options">
        <div class="options-wrapper">
          <div v-for="(opt, index) in form.options" :key="index" class="option-item">
            <span class="option-label">{{ String.fromCharCode(65 + index) }}</span>
            <el-input
              v-model="form.options[index]"
              type="textarea"
              :autosize="{ minRows: 1, maxRows: 4 }"
              :placeholder="`请输入选项${String.fromCharCode(65 + index)}`"
            />
            <el-button v-if="form.options.length > 2" type="danger" link @click="form.options.splice(index, 1)">
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
          <el-button v-if="form.options.length < 6" type="primary" link @click="form.options.push('')">
            <el-icon><Plus /></el-icon>
            添加选项
          </el-button>
        </div>
      </el-form-item>
      <el-form-item label="答案" prop="answer">
        <el-select v-model="form.answer" placeholder="选择正确答案" style="width: 100%; max-width: 420px">
          <el-option v-for="(opt, index) in form.options.filter(o => o)" :key="index" :label="`选项${String.fromCharCode(65 + index)}`" :value="String.fromCharCode(65 + index)" />
        </el-select>
      </el-form-item>
      <el-form-item label="解析" prop="analysis">
        <el-input v-model="form.analysis" type="textarea" :rows="6" placeholder="请输入答案解析（可选）" />
      </el-form-item>
      <el-form-item label="标签" prop="tags">
        <el-select
          v-model="formTagList"
          multiple
          filterable
          allow-create
          default-first-option
          placeholder="选择标签，或输入新标签后回车"
          style="width: 100%; max-width: 640px"
        >
          <el-option v-for="t in tagOptions" :key="t.name" :label="t.name" :value="t.name">
            <span>{{ t.name }}</span>
            <el-tag v-if="t.hot" type="danger" size="small" effect="plain" style="margin-left: 6px">热门</el-tag>
          </el-option>
        </el-select>
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
import { ArrowLeft, Close, Plus } from '@element-plus/icons-vue'
import api from '../utils/api'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)

const loading = ref(false)
const saving = ref(false)
const formRef = ref(null)
const categories = ref([])
const tagOptions = ref([])

const form = reactive({
  id: null,
  categoryId: null,
  content: '',
  difficulty: 2,
  status: 1,
  options: ['', '', '', ''],
  answer: '',
  analysis: '',
  tags: ''
})

const rules = {
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
  answer: [{ required: true, message: '请选择答案', trigger: 'change' }]
}

// form.tags（逗号分隔）↔ 多选数组 双向同步
const formTagList = computed({
  get: () => (form.tags ? form.tags.replace(/，/g, ',').split(',').map(s => s.trim()).filter(Boolean) : []),
  set: (val) => { form.tags = [...new Set(val)].join(',') }
})

const goBack = () => {
  if (window.history.length > 1) router.back()
  else router.push('/question/list')
}

const loadCategories = async () => {
  try {
    const res = await api.get('/admin/category/list')
    categories.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const loadTagOptions = async () => {
  try {
    const res = await api.get('/admin/tag/all')
    tagOptions.value = res.data || []
  } catch (error) {
    console.error('加载标签失败', error)
  }
}

// 与原编辑弹窗相同的回填方式：options 为 JSON 字符串时解析为数组
const loadDetail = async () => {
  loading.value = true
  try {
    const res = await api.get(`/admin/question/${route.params.id}`)
    const d = res.data || {}
    Object.assign(form, {
      id: d.id,
      categoryId: d.categoryId,
      content: d.content || '',
      difficulty: d.difficulty ?? 2,
      status: d.status ?? 1,
      options: d.options ? (typeof d.options === 'string' ? JSON.parse(d.options) : d.options) : ['', '', '', ''],
      answer: d.answer || '',
      analysis: d.analysis || '',
      tags: d.tags || ''
    })
  } catch (e) {
    ElMessage.error(e?.message || '加载题目详情失败')
  } finally {
    loading.value = false
  }
}

const save = () => {
  if (!formRef.value) return
  formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      const data = {
        ...form,
        options: JSON.stringify(form.options.filter(o => o))
      }
      if (isEdit.value) {
        await api.put('/admin/question', data)
        ElMessage.success('更新成功')
      } else {
        await api.post('/admin/question', data)
        ElMessage.success('添加成功')
      }
      goBack()
    } catch (e) {
      ElMessage.error(e?.message || '保存失败')
    } finally {
      saving.value = false
    }
  })
}

onMounted(() => {
  loadCategories()
  loadTagOptions()
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

.options-wrapper {
  width: 100%;
}

.option-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;
}

.option-label {
  width: 24px;
  height: 24px;
  background: #f0f0f0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  color: #666;
  flex-shrink: 0;
  margin-top: 4px;
}

.option-item .el-textarea {
  flex: 1;
}
</style>
