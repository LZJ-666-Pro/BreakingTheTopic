<template>
  <div class="page-card">
    <!-- 头部：返回 + 标题 -->
    <div class="header-row">
      <el-button :icon="ArrowLeft" @click="goBack">返回</el-button>
      <div class="header-title">编辑题解</div>
    </div>

    <el-form label-width="80px" class="edit-form" v-loading="loading">
      <el-form-item label="标题" required>
        <el-input v-model="form.title" maxlength="200" placeholder="题解标题" />
      </el-form-item>
      <el-form-item label="内容">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="20"
          placeholder="支持 Markdown 语法"
          class="content-input"
        />
      </el-form-item>
      <el-form-item label="优质">
        <el-switch v-model="form.quality" :active-value="1" :inactive-value="0" />
      </el-form-item>
      <el-form-item label="置顶">
        <el-switch v-model="form.pinned" :active-value="1" :inactive-value="0" />
      </el-form-item>
      <el-form-item>
        <el-button @click="goBack">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import api from '../utils/api'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const saving = ref(false)

const form = reactive({ title: '', content: '', quality: 0, pinned: 0 })

const goBack = () => {
  if (window.history.length > 1) router.back()
  else router.push('/content/solution-review')
}

async function loadDetail() {
  loading.value = true
  try {
    const res = await api.get(`/admin/solution/${route.params.id}`)
    const d = res.data || {}
    form.title = d.title || ''
    form.content = d.content || ''
    form.quality = d.quality ?? 0
    form.pinned = d.pinned ?? 0
  } catch (e) {
    ElMessage.error(e?.message || '加载题解详情失败')
  } finally {
    loading.value = false
  }
}

async function save() {
  if (!form.title || !form.title.trim()) {
    ElMessage.warning('标题不能为空')
    return
  }
  saving.value = true
  try {
    await api.put('/admin/solution', { id: route.params.id, ...form })
    ElMessage.success('保存成功')
    goBack()
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  if (!route.params.id) {
    ElMessage.error('缺少题解 ID')
    goBack()
    return
  }
  loadDetail()
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
  max-width: 960px;
}

.content-input :deep(.el-textarea__inner) {
  resize: vertical;
}
</style>
