<template>
  <div class="page-card">
    <!-- 头部：返回 + 标题 -->
    <div class="header-row">
      <el-button :icon="ArrowLeft" @click="goBack">返回</el-button>
      <div class="header-title">{{ isEdit ? '编辑文章' : '新增文章' }}</div>
    </div>

    <el-form label-width="90px" class="edit-form" v-loading="loading">
      <el-form-item label="标题" required>
        <el-input v-model="form.title" maxlength="200" placeholder="文章标题" />
      </el-form-item>
      <el-row>
        <el-col :span="8">
          <el-form-item label="分类">
            <el-select v-model="form.category" placeholder="选择分类">
              <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="作者">
            <el-input v-model="form.authorName" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="标签">
        <el-input v-model="form.tags" placeholder="多个标签用英文逗号分隔，如：Java,面试" style="max-width: 640px" />
      </el-form-item>
      <el-form-item label="封面URL">
        <el-input v-model="form.cover" placeholder="封面图地址（可选）" style="max-width: 640px" />
      </el-form-item>
      <el-form-item label="摘要">
        <el-input v-model="form.summary" type="textarea" :rows="3" maxlength="500" show-word-limit style="max-width: 640px" />
      </el-form-item>
      <el-form-item label="正文" required>
        <el-input v-model="form.content" type="textarea" :rows="20" placeholder="支持 Markdown 语法" />
      </el-form-item>
      <el-form-item label="发布状态">
        <el-radio-group v-model="form.status">
          <el-radio :value="0">草稿</el-radio>
          <el-radio :value="1">发布</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-row>
        <el-col :span="8">
          <el-form-item label="置顶">
            <el-switch v-model="form.pinned" :active-value="1" :inactive-value="0" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="推荐">
            <el-switch v-model="form.recommended" :active-value="1" :inactive-value="0" />
          </el-form-item>
        </el-col>
      </el-row>
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

const categories = ['学习路线', '面试经验', '刷题技巧', '面经', '技术科普']

const isEdit = computed(() => !!route.params.id)

const loading = ref(false)
const saving = ref(false)

const form = reactive({
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
})

const goBack = () => {
  if (window.history.length > 1) router.back()
  else router.push('/content/article')
}

async function loadDetail() {
  loading.value = true
  try {
    const res = await api.get(`/admin/article/${route.params.id}`)
    const d = res.data || {}
    form.title = d.title || ''
    form.category = d.category || ''
    form.cover = d.cover || ''
    form.tags = d.tags || ''
    form.summary = d.summary || ''
    form.content = d.content || ''
    form.authorName = d.authorName || '官方'
    form.pinned = d.pinned ?? 0
    form.recommended = d.recommended ?? 0
    // 已下架(2)在表单中按草稿处理，与原弹窗回填行为一致
    form.status = d.status === 2 ? 0 : (d.status ?? 0)
  } catch (e) {
    ElMessage.error(e?.message || '加载文章详情失败')
  } finally {
    loading.value = false
  }
}

async function save() {
  if (!form.title.trim()) return ElMessage.warning('请填写文章标题')
  if (!form.content.trim()) return ElMessage.warning('请填写文章正文')
  saving.value = true
  try {
    if (isEdit.value) {
      await api.put('/admin/article', { id: route.params.id, ...form })
    } else {
      await api.post('/admin/article', form)
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
</style>
