<template>
  <div class="page-card">
    <!-- 头部：返回 + 标题 -->
    <div class="header-row">
      <el-button :icon="ArrowLeft" @click="goBack">返回</el-button>
      <div class="header-title">{{ isEdit ? '编辑专题' : '新增专题' }}</div>
    </div>

    <el-form label-width="90px" class="edit-form" v-loading="loading">
      <el-form-item label="专题名称" required>
        <el-input v-model="form.name" placeholder="如：动态规划专题" maxlength="50" style="max-width: 420px" />
      </el-form-item>
      <el-form-item label="简介">
        <el-input v-model="form.description" type="textarea" :rows="3" placeholder="一句话介绍这个专题" maxlength="200" style="max-width: 640px" />
      </el-form-item>
      <el-row>
        <el-col :span="8">
          <el-form-item label="难度">
            <el-select v-model="form.difficulty">
              <el-option label="简单" :value="1" />
              <el-option label="中等" :value="2" />
              <el-option label="困难" :value="3" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="预计天数">
            <el-input-number v-model="form.estimatedDays" :min="1" :max="365" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="8">
          <el-form-item label="解锁规则">
            <el-select v-model="form.unlockType">
              <el-option label="自由刷" :value="0" />
              <el-option label="按顺序解锁" :value="1" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="推荐权重">
            <el-input-number v-model="form.sort" :min="0" :max="9999" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="是否上架">
        <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
      </el-form-item>
      <el-form-item label="选择题目" required>
        <div class="question-block">
          <el-select
            v-model="form.questionIds"
            multiple filterable remote
            :remote-method="searchQuestions"
            :loading="qLoading"
            placeholder="输入关键字搜索题目，勾选顺序即题目顺序"
            style="width: 100%"
          >
            <el-option v-for="q in qOptions" :key="q.id" :label="q.id + '. ' + q.content" :value="q.id" />
          </el-select>
          <div class="question-tip">已选 {{ form.questionIds.length }} 题 · 勾选顺序即学习顺序，可在输入框搜索并按需勾选</div>
        </div>
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

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)

const loading = ref(false)
const saving = ref(false)
const qLoading = ref(false)
const qOptions = ref([])

const form = reactive({ name: '', description: '', difficulty: 2, estimatedDays: 7, unlockType: 0, sort: 0, status: 1, questionIds: [] })

const goBack = () => {
  if (window.history.length > 1) router.back()
  else router.push('/special/manage')
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

async function loadDetail() {
  loading.value = true
  try {
    // 先拉详情（含题目列表），把已有题目回填进选项
    const res = await api.get(`/admin/special/${route.params.id}`)
    const d = res.data || {}
    form.name = d.name || ''
    form.description = d.description || ''
    form.difficulty = d.difficulty ?? 2
    form.estimatedDays = d.estimatedDays ?? 7
    form.unlockType = d.unlockType ?? 0
    form.sort = d.sort ?? 0
    form.status = d.status ?? 1
    const qs = d.questions || []
    qOptions.value = qs.map((q) => ({ id: q.id, content: q.title || q.content }))
    form.questionIds = qs.map((q) => q.id)
  } catch (e) {
    ElMessage.error(e?.message || '加载专题详情失败')
  } finally {
    loading.value = false
  }
}

async function save() {
  const f = form
  if (!f.name || !f.name.trim()) return ElMessage.warning('请填写专题名称')
  if (!f.questionIds.length) return ElMessage.warning('请至少选择一道题目')
  saving.value = true
  try {
    if (isEdit.value) {
      await api.put('/admin/special', { id: route.params.id, ...f })
    } else {
      await api.post('/admin/special', f)
    }
    ElMessage.success('保存成功')
    goBack()
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
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

.question-block {
  width: 100%;
  max-width: 640px;
}

.question-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 6px;
}
</style>
