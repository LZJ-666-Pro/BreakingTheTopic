<template>
  <div class="page-container">
    <div class="page-header">
      <h2>题目管理</h2>
      <div class="header-actions">
        <el-button type="warning" @click="showAiDialog">
          <el-icon><MagicStick /></el-icon>
          AI生成题目
        </el-button>
        <el-upload
          ref="uploadRef"
          :action="uploadUrl"
          :headers="uploadHeaders"
          :show-file-list="false"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
          :on-progress="handleUploadProgress"
          :before-upload="beforeUpload"
          accept=".xlsx,.xls,.csv"
          :disabled="uploading"
        >
          <el-button type="success" :loading="uploading">
            <el-icon v-if="!uploading"><Upload /></el-icon>
            {{ uploading ? '导入中...' : '批量导入' }}
          </el-button>
        </el-upload>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          添加题目
        </el-button>
      </div>
    </div>

    <el-card class="content-card" shadow="never">
      <div class="search-bar">
        <el-select v-model="searchForm.categoryId" placeholder="选择分类" clearable filterable style="width: 140px">
          <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
        </el-select>
        <el-select v-model="searchForm.difficulty" placeholder="选择难度" clearable style="width: 120px">
          <el-option label="简单" :value="1" />
          <el-option label="中等" :value="2" />
          <el-option label="困难" :value="3" />
        </el-select>
        <el-select v-model="searchForm.status" placeholder="选择状态" clearable style="width: 120px">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-input 
          v-model="searchForm.keyword" 
          placeholder="搜索题目内容" 
          clearable 
          style="width: 200px"
          :prefix-icon="Search"
        />
        <el-button type="primary" @click="loadQuestions">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>
      
      <el-table :data="questions" v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="content" label="题目内容" min-width="250" show-overflow-tooltip />
        <el-table-column prop="categoryId" label="分类" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ getCategoryName(row.categoryId) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="difficulty" label="难度" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="difficultyMap[row.difficulty]?.type" size="small">
              {{ difficultyMap[row.difficulty]?.label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              @change="(val) => handleStatusChange(row, val)"
              size="small"
            />
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览" width="80" align="center">
          <template #default="{ row }">
            <span class="text-muted">{{ row.viewCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" align="center">
          <template #default="{ row }">
            <span class="text-muted">{{ formatDate(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100, 200, 500]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="loadQuestions"
          @current-change="loadQuestions"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑题目' : '添加题目'" width="720px" top="5vh" destroy-on-close>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-row :gutter="20">
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
          <el-input v-model="form.content" type="textarea" :rows="3" placeholder="请输入题目内容" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="选项" prop="options">
          <div class="options-wrapper">
            <div v-for="(opt, index) in form.options" :key="index" class="option-item">
              <span class="option-label">{{ String.fromCharCode(65 + index) }}</span>
              <el-input v-model="form.options[index]" :placeholder="`请输入选项${String.fromCharCode(65 + index)}`" />
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
          <el-select v-model="form.answer" placeholder="选择正确答案" style="width: 100%">
            <el-option v-for="(opt, index) in form.options.filter(o => o)" :key="index" :label="`选项${String.fromCharCode(65 + index)}`" :value="String.fromCharCode(65 + index)" />
          </el-select>
        </el-form-item>
        <el-form-item label="解析" prop="analysis">
          <el-input v-model="form.analysis" type="textarea" :rows="3" placeholder="请输入答案解析（可选）" />
        </el-form-item>
        <el-form-item label="标签" prop="tags">
          <el-input v-model="form.tags" placeholder="多个标签用逗号分隔（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="aiDialogVisible" title="AI生成题目" width="600px" destroy-on-close>
      <el-form :model="aiForm" :rules="aiRules" ref="aiFormRef" label-width="100px">
        <el-form-item label="题目分类" prop="categoryId">
          <el-select v-model="aiForm.categoryId" placeholder="输入关键字搜索分类" filterable style="width: 100%">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="生成数量" prop="count">
          <el-input-number v-model="aiForm.count" :min="1" :max="1000" style="width: 100%" />
          <div class="form-tip">生成任务将在后台运行，可在【AI任务】页面查看进度</div>
        </el-form-item>
        <el-form-item label="难度等级" prop="difficulty">
          <el-select v-model="aiForm.difficulty" placeholder="选择难度" style="width: 100%">
            <el-option label="简单" :value="1" />
            <el-option label="中等" :value="2" />
            <el-option label="困难" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="主题方向">
          <el-input v-model="aiForm.topic" placeholder="例如：多线程、集合、Spring IOC等（可选）" />
        </el-form-item>
        <el-form-item label="额外要求">
          <el-input v-model="aiForm.additionalRequirements" type="textarea" :rows="2" placeholder="例如：侧重实战场景、包含代码示例等（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="aiDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAiGenerate" :loading="aiGenerating">
          <el-icon v-if="!aiGenerating"><MagicStick /></el-icon>
          {{ aiGenerating ? '生成中...' : '开始生成' }}
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="taskDialogVisible" title="后台任务进度" width="500px" destroy-on-close>
      <div class="task-progress">
        <div class="task-info">
          <span>分类：{{ taskInfo.categoryName }}</span>
          <span>难度：{{ taskInfo.difficulty }}</span>
        </div>
        <el-progress 
          :percentage="taskProgress" 
          :status="taskStatus === 2 ? 'success' : taskStatus === 3 ? 'exception' : ''"
          :stroke-width="20"
        />
        <div class="task-count">
          {{ taskInfo.completedCount || 0 }} / {{ taskInfo.totalCount || 0 }} 道
        </div>
        <div v-if="taskStatus === 1" class="task-status">
          <el-icon class="is-loading"><Loading /></el-icon>
          正在生成中，请稍候...
        </div>
        <div v-else-if="taskStatus === 2" class="task-status success">
          <el-icon><CircleCheck /></el-icon>
          生成完成！
        </div>
        <div v-else-if="taskStatus === 3" class="task-status error">
          <el-icon><CircleClose /></el-icon>
          生成失败：{{ taskInfo.errorMessage }}
        </div>
      </div>
      <template #footer>
        <el-button v-if="taskStatus === 1" @click="handleCancelTask">取消任务</el-button>
        <el-button v-if="taskStatus === 2" type="primary" @click="taskDialogVisible = false">完成</el-button>
        <el-button v-if="taskStatus === 3" type="primary" @click="taskDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="aiResultVisible" title="AI生成结果" width="800px" top="5vh" destroy-on-close>
      <div class="ai-result-header">
        <span>共生成 <strong>{{ aiQuestions.length }}</strong> 道题目</span>
        <el-button type="primary" @click="handleSaveAiQuestions" :loading="aiSaving">
          <el-icon><Check /></el-icon>
          全部保存
        </el-button>
      </div>
      <div class="ai-questions-list">
        <div v-for="(q, index) in aiQuestions" :key="index" class="ai-question-item">
          <div class="ai-question-header">
            <span class="ai-question-index">题目 {{ index + 1 }}</span>
            <el-tag :type="difficultyMap[q.difficulty]?.type" size="small">
              {{ difficultyMap[q.difficulty]?.label }}
            </el-tag>
          </div>
          <div class="ai-question-content">{{ q.content }}</div>
          <div class="ai-question-options">
            <div v-for="(opt, optIndex) in q.options" :key="optIndex" class="ai-option" :class="{ 'is-answer': opt.startsWith(q.answer + '.') }">
              {{ opt }}
            </div>
          </div>
          <div class="ai-question-analysis">
            <strong>解析：</strong>{{ q.analysis }}
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="aiResultVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveAiQuestions" :loading="aiSaving">
          确认保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Close, Upload, MagicStick, Check, Loading, CircleCheck, CircleClose } from '@element-plus/icons-vue'
import api from '../utils/api'
import { formatDate } from '../utils/formatDate'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const questions = ref([])
const categories = ref([])
const formRef = ref(null)
const uploadRef = ref(null)
const uploading = ref(false)
const aiFormRef = ref(null)
const aiDialogVisible = ref(false)
const aiResultVisible = ref(false)
const aiGenerating = ref(false)
const aiSaving = ref(false)
const aiQuestions = ref([])

const uploadUrl = computed(() => {
  return '/api/admin/question/import'
})

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('admin_token')
  return token ? { Authorization: `Bearer ${token}` } : {}
})

const searchForm = reactive({
  categoryId: null,
  difficulty: null,
  status: null,
  keyword: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

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

const aiForm = reactive({
  categoryId: null,
  count: 5,
  difficulty: 2,
  topic: '',
  additionalRequirements: ''
})

const aiRules = {
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  count: [{ required: true, message: '请输入数量', trigger: 'blur' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }]
}

const taskDialogVisible = ref(false)
const taskInfo = ref({})
const taskStatus = ref(0)
const taskProgress = computed(() => {
  if (!taskInfo.value.totalCount) return 0
  return Math.round((taskInfo.value.completedCount || 0) / taskInfo.value.totalCount * 100)
})
let taskPollingTimer = null

const difficultyMap = {
  1: { label: '简单', type: 'success' },
  2: { label: '中等', type: 'warning' },
  3: { label: '困难', type: 'danger' }
}

const getCategoryName = (categoryId) => {
  const cat = categories.value.find(c => c.id === categoryId)
  return cat ? cat.name : '-'
}

const resetSearch = () => {
  searchForm.categoryId = null
  searchForm.difficulty = null
  searchForm.status = null
  searchForm.keyword = ''
  pagination.page = 1
  loadQuestions()
}

const loadCategories = async () => {
  try {
    const res = await api.get('/admin/category/list')
    categories.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const loadQuestions = async () => {
  loading.value = true
  try {
    const res = await api.get('/admin/question/page', {
      params: {
        page: pagination.page,
        size: pagination.size,
        categoryId: searchForm.categoryId,
        difficulty: searchForm.difficulty,
        status: searchForm.status,
        keyword: searchForm.keyword
      }
    })
    
    if (res.data && res.data.records) {
      questions.value = res.data.records
      pagination.total = res.data.total || 0
    } else if (Array.isArray(res.data)) {
      questions.value = res.data
      pagination.total = res.data.length
    } else {
      questions.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, {
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
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, {
    ...row,
    options: row.options ? (typeof row.options === 'string' ? JSON.parse(row.options) : row.options) : ['', '', '', '']
  })
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该题目吗？', '删除确认', { 
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await api.delete(`/admin/question/${row.id}`)
    ElMessage.success('删除成功')
    loadQuestions()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const handleStatusChange = async (row, val) => {
  try {
    await api.put(`/admin/question/${row.id}/status`, null, {
      params: { status: val ? 1 : 0 }
    })
    ElMessage.success('状态更新成功')
    loadQuestions()
  } catch (error) {
    console.error(error)
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitLoading.value = true
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
      dialogVisible.value = false
      loadQuestions()
    } catch (error) {
      console.error(error)
    } finally {
      submitLoading.value = false
    }
  })
}

const beforeUpload = (file) => {
  const fileName = file.name.toLowerCase()
  const isExcel = fileName.endsWith('.xlsx') || fileName.endsWith('.xls') || fileName.endsWith('.csv')
  if (!isExcel) {
    ElMessage.error('只能上传Excel文件（.xlsx、.xls或.csv格式）')
    return false
  }
  const isLt50M = file.size / 1024 / 1024 < 50
  if (!isLt50M) {
    ElMessage.error('文件大小不能超过50MB')
    return false
  }
  uploading.value = true
  return true
}

const handleUploadProgress = (event) => {
  console.log('上传进度：', event.percent)
}

const handleUploadSuccess = (response) => {
  uploading.value = false
  if (response.code === 200) {
    const { total, success, fail, errors, duration, message } = response.data
    
    const successRate = total > 0 ? ((success / total) * 100).toFixed(1) : 0
    
    const resultHTML = `
      <div style="padding: 12px 0;">
        <div style="margin-bottom: 16px; width: 100%;">
          <div style="display: flex; width: 100%; gap: 6px;">
            <div style="flex: 1; text-align: center; padding: 12px 8px; background: #f5f5f5; border-radius: 4px;">
              <div style="font-size: 24px; font-weight: bold; color: #333;">${total}</div>
              <div style="font-size: 12px; color: #999; margin-top: 4px;">总计</div>
            </div>
            <div style="flex: 1; text-align: center; padding: 12px 8px; background: #f5f5f5; border-radius: 4px;">
              <div style="font-size: 24px; font-weight: bold; color: #52c41a;">${success}</div>
              <div style="font-size: 12px; color: #999; margin-top: 4px;">成功</div>
            </div>
            <div style="flex: 1; text-align: center; padding: 12px 8px; background: #f5f5f5; border-radius: 4px;">
              <div style="font-size: 24px; font-weight: bold; color: ${fail > 0 ? '#ff4d4f' : '#333'};">${fail}</div>
              <div style="font-size: 12px; color: #999; margin-top: 4px;">失败</div>
            </div>
          </div>
        </div>
        <div style="background: #f5f5f5; padding: 10px 12px; border-radius: 4px; margin-bottom: 12px;">
          <div style="display: flex; justify-content: space-between; margin-bottom: 4px;">
            <span style="color: #666; font-size: 13px;">成功率</span>
            <span style="color: #333; font-weight: 500; font-size: 13px;">${successRate}%</span>
          </div>
          <div style="display: flex; justify-content: space-between;">
            <span style="color: #666; font-size: 13px;">耗时</span>
            <span style="color: #333; font-weight: 500; font-size: 13px;">${duration}</span>
          </div>
        </div>
        ${errors && errors.length > 0 ? `
          <div style="border-top: 1px solid #eee; padding-top: 12px;">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
              <span style="font-weight: 500; color: #333; font-size: 13px;">错误信息</span>
              <span style="background: #fff1f0; color: #ff4d4f; padding: 2px 8px; border-radius: 10px; font-size: 12px;">${errors.length}条</span>
            </div>
            <div style="max-height: 100px; overflow-y: auto; background: #fafafa; padding: 8px; border-radius: 4px;">
              ${errors.slice(0, 5).map(err => `<div style="font-size: 12px; color: #666; padding: 3px 0; border-bottom: 1px dashed #e8e8e8;">${err}</div>`).join('')}
            </div>
            ${errors.length > 5 ? `<div style="text-align: center; color: #999; font-size: 12px; margin-top: 6px;">还有 ${errors.length - 5} 条错误</div>` : ''}
          </div>
        ` : ''}
      </div>
    `
    
    ElMessageBox.alert(resultHTML, success === total ? '导入成功' : '导入完成', {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '确定',
      customClass: 'import-result-dialog'
    })
    
    loadQuestions()
  } else {
    ElMessage.error(response.msg || '导入失败')
  }
}

const handleUploadError = (error) => {
  uploading.value = false
  console.error('上传错误：', error)
  ElMessage.error('文件上传失败，请检查网络连接或联系管理员')
}

const showAiDialog = () => {
  aiForm.categoryId = null
  aiForm.count = 5
  aiForm.difficulty = 2
  aiForm.topic = ''
  aiForm.additionalRequirements = ''
  aiDialogVisible.value = true
}

const handleAiGenerate = async () => {
  if (!aiFormRef.value) return
  
  await aiFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    aiGenerating.value = true
    const count = aiForm.count
    
    try {
      const res = await api.post('/admin/question/ai/task/create', aiForm)
      if (res.code === 200 && res.data) {
        aiDialogVisible.value = false
        ElMessage.success(`任务已创建！正在后台生成 ${count} 道题目，可前往【AI任务】查看进度`)
      } else {
        ElMessage.error(res.msg || '创建任务失败')
      }
    } catch (error) {
      console.error(error)
      ElMessage.error('创建任务失败')
    } finally {
      aiGenerating.value = false
    }
  })
}

const startTaskPolling = (taskId) => {
  if (taskPollingTimer) {
    clearInterval(taskPollingTimer)
  }
  
  taskPollingTimer = setInterval(async () => {
    try {
      const res = await api.get(`/admin/question/ai/task/${taskId}`)
      if (res.code === 200 && res.data) {
        taskInfo.value = res.data
        taskStatus.value = res.data.status
        
        if (res.data.status === 2 || res.data.status === 3) {
          clearInterval(taskPollingTimer)
          taskPollingTimer = null
          if (res.data.status === 2) {
            loadQuestions()
          }
        }
      }
    } catch (error) {
      console.error('轮询任务状态失败', error)
    }
  }, 3000)
}

const handleCancelTask = async () => {
  if (!taskInfo.value.id) return
  
  try {
    const res = await api.post(`/admin/question/ai/task/${taskInfo.value.id}/cancel`)
    if (res.code === 200) {
      ElMessage.success('任务已取消')
      if (taskPollingTimer) {
        clearInterval(taskPollingTimer)
        taskPollingTimer = null
      }
      taskDialogVisible.value = false
    }
  } catch (error) {
    ElMessage.error('取消失败')
  }
}

const handleSaveAiQuestions = async () => {
  if (aiQuestions.value.length === 0) {
    ElMessage.warning('没有可保存的题目')
    return
  }
  
  aiSaving.value = true
  try {
    const res = await api.post('/admin/question/ai/save', aiQuestions.value, {
      params: { categoryId: aiForm.categoryId }
    })
    if (res.code === 200) {
      ElMessage.success('题目保存成功')
      aiResultVisible.value = false
      loadQuestions()
    } else {
      ElMessage.error(res.msg || '保存失败')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('保存失败')
  } finally {
    aiSaving.value = false
  }
}

onMounted(() => {
  loadCategories()
  loadQuestions()
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
  gap: 12px;
}

.content-card {
  border-radius: 4px;
  border: 1px solid #f0f0f0;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
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

.options-wrapper {
  width: 100%;
}

.option-item {
  display: flex;
  align-items: center;
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
}

.option-item .el-input {
  flex: 1;
}

.ai-result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.ai-result-header strong {
  color: #1890ff;
  font-size: 18px;
}

.ai-questions-list {
  max-height: 500px;
  overflow-y: auto;
}

.ai-question-item {
  background: #fafafa;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
}

.ai-question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.ai-question-index {
  font-weight: 600;
  color: #333;
}

.ai-question-content {
  font-size: 15px;
  color: #333;
  line-height: 1.6;
  margin-bottom: 12px;
}

.ai-question-options {
  margin-bottom: 12px;
}

.ai-option {
  padding: 8px 12px;
  margin-bottom: 6px;
  background: #fff;
  border-radius: 4px;
  border: 1px solid #e8e8e8;
  font-size: 14px;
}

.ai-option.is-answer {
  background: #f6ffed;
  border-color: #b7eb8f;
  color: #52c41a;
}

.ai-question-analysis {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  padding: 10px;
  background: #fff;
  border-radius: 4px;
  border-left: 3px solid #1890ff;
}

.ai-question-analysis strong {
  color: #1890ff;
}

.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.task-progress {
  padding: 20px;
  text-align: center;
}

.task-info {
  display: flex;
  justify-content: space-around;
  margin-bottom: 20px;
  color: #666;
}

.task-count {
  margin-top: 10px;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.task-status {
  margin-top: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #1890ff;
}

.task-status.success {
  color: #52c41a;
}

.task-status.error {
  color: #ff4d4f;
}
</style>

<style>
.import-result-dialog {
  width: 380px !important;
  max-width: 90vw !important;
  border-radius: 8px !important;
}

.import-result-dialog .el-message-box__header {
  padding: 16px 20px !important;
  border-bottom: 1px solid #f0f0f0 !important;
}

.import-result-dialog .el-message-box__title {
  font-size: 16px !important;
  font-weight: 600 !important;
}

.import-result-dialog .el-message-box__content {
  padding: 0 20px !important;
  width: 100% !important;
  box-sizing: border-box !important;
}

.import-result-dialog .el-message-box__message {
  width: 100% !important;
}

.import-result-dialog .el-message-box__message p {
  width: 100% !important;
  margin: 0 !important;
}

.import-result-dialog .el-message-box__btns {
  padding: 12px 20px !important;
  border-top: 1px solid #f0f0f0 !important;
}

.import-result-dialog .el-button--primary {
  background: #1890ff !important;
  border-color: #1890ff !important;
}
</style>
