<template>
  <div class="page-container">
    <div class="page-header">
      <h2>题库设置</h2>
      <el-button type="primary" @click="currentTab === 'category' ? handleAdd() : handleAddGroup()">
        <el-icon><Plus /></el-icon>
        {{ currentTab === 'category' ? '添加题型分类' : '添加分组标签' }}
      </el-button>
    </div>

    <el-tabs v-model="currentTab" class="main-tabs">
      <!-- ==================== 题型分类 ==================== -->
      <el-tab-pane label="题型分类" name="category">
        <el-card class="content-card" shadow="never">
          <div class="toolbar">
            <el-input
              v-model="searchName"
              placeholder="按分类名称搜索"
              clearable
              style="width: 220px"
              @keyup.enter="handleSearch"
              @clear="handleSearch"
            >
              <template #append>
                <el-button @click="handleSearch">
                  <el-icon><Search /></el-icon>
                </el-button>
              </template>
            </el-input>
          </div>

          <el-table :data="categories" v-loading="loading">
            <el-table-column prop="id" label="ID" width="70" align="center" />
            <el-table-column label="图标" width="80" align="center">
              <template #default="{ row }">
                <img v-if="row.iconUrl" :src="row.iconUrl" class="icon-preview" alt="icon" />
                <el-tag v-else-if="row.icon" size="small">{{ row.icon }}</el-tag>
                <span v-else class="text-muted">-</span>
              </template>
            </el-table-column>
            <el-table-column prop="name" label="分类名称" min-width="130" />
            <el-table-column prop="questionCount" label="题目数" width="90" align="center">
              <template #default="{ row }">
                <span class="num-cell">{{ row.questionCount }}</span>
              </template>
            </el-table-column>
            <el-table-column label="所属分组标签" min-width="220">
              <template #default="{ row }">
                <template v-if="row.groupKeys && row.groupKeys.length">
                  <el-tag v-for="key in row.groupKeys" :key="key" size="small" class="group-tag" type="info">
                    {{ groupLabel(key) }}
                  </el-tag>
                </template>
                <span v-else class="text-muted">未设置</span>
              </template>
            </el-table-column>
            <el-table-column prop="sort" label="排序" width="80" align="center" />
            <el-table-column prop="createTime" label="创建时间" width="120" align="center">
              <template #default="{ row }">
                <span class="text-muted">{{ formatDate(row.createTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140" align="center" fixed="right">
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
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              background
              @size-change="loadCategories"
              @current-change="loadCategories"
            />
          </div>
        </el-card>
      </el-tab-pane>

      <!-- ==================== 分组标签 ==================== -->
      <el-tab-pane label="分组标签" name="group">
        <el-card class="content-card" shadow="never">
          <el-table :data="groups" v-loading="groupLoading">
            <el-table-column prop="id" label="ID" width="70" align="center" />
            <el-table-column prop="name" label="标签名称" min-width="130">
              <template #default="{ row }">
                <el-tag :type="row.groupKey === 'hidden' ? 'warning' : 'primary'" effect="light">{{ row.name }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="groupKey" label="标识" min-width="110" />
            <el-table-column prop="categoryCount" label="关联分类数" width="110" align="center">
              <template #default="{ row }">
                <span class="num-cell">{{ row.categoryCount }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="sort" label="排序" width="80" align="center" />
            <el-table-column label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                  {{ row.status === 1 ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="120" align="center">
              <template #default="{ row }">
                <span class="text-muted">{{ formatDate(row.createTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140" align="center" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleEditGroup(row)">编辑</el-button>
                <el-button type="danger" link size="small" @click="handleDeleteGroup(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="groupPagination.page"
              v-model:page-size="groupPagination.size"
              :total="groupPagination.total"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              background
              @size-change="loadGroups"
              @current-change="loadGroups"
            />
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 分类弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑题型分类' : '添加题型分类'" width="520px" destroy-on-close>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="如：Rust、微服务" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="图标URL" prop="iconUrl">
          <el-input v-model="form.iconUrl" placeholder="小程序展示图标地址（可选，留空用默认图标）" maxlength="500" />
        </el-form-item>
        <el-form-item label="图标标识" prop="icon">
          <el-input v-model="form.icon" placeholder="图标关键字（可选）" maxlength="50" />
        </el-form-item>
        <el-form-item label="所属标签" prop="groupKeys">
          <el-select v-model="form.groupKeys" multiple placeholder="选择题库页顶部筛选标签" style="width: 100%">
            <el-option v-for="g in groupOptions" :key="g.groupKey" :label="g.name" :value="g.groupKey" />
          </el-select>
          <div class="form-tip">决定新分类出现在题库页哪些筛选标签下；选「隐藏」则不在宫格展示</div>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="分类简介（可选）" maxlength="200" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分组标签弹窗 -->
    <el-dialog v-model="groupDialogVisible" :title="isGroupEdit ? '编辑分组标签' : '添加分组标签'" width="460px" destroy-on-close>
      <el-form :model="groupForm" :rules="groupRules" ref="groupFormRef" label-width="90px">
        <el-form-item label="标签名称" prop="name">
          <el-input v-model="groupForm.name" placeholder="如：后端、人工智能" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item label="标识" prop="groupKey">
          <el-input v-model="groupForm.groupKey" placeholder="英文标识，如 backend" maxlength="50" :disabled="isGroupEdit" />
          <div class="form-tip">{{ isGroupEdit ? '标识创建后不可修改' : '程序使用，仅限字母数字下划线和短横线' }}</div>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="groupForm.sort" :min="0" :max="999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="groupForm.status" :active-value="1" :inactive-value="0" active-text="启用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="groupDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleGroupSubmit" :loading="groupSubmitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { getDefaultPageSize } from '../utils/config'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import api from '../utils/api'
import { formatDate } from '../utils/formatDate'

const currentTab = ref('category')

/* ==================== 题型分类 ==================== */
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const categories = ref([])
const formRef = ref(null)
const searchName = ref('')

// 每页条数持久化：用户选择优先，其次系统配置默认值
const pagination = reactive({ page: 1, size: Number(localStorage.getItem('category_page_size')) || getDefaultPageSize(), total: 0 })
watch(() => pagination.size, v => localStorage.setItem('category_page_size', String(v)))

const form = reactive({
  id: null,
  name: '',
  icon: '',
  iconUrl: '',
  description: '',
  sort: 0,
  groupKeys: []
})

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

const groupOptions = computed(() => groupOptionsList.value.filter(g => g.status === 1))
const groupLabel = (key) => groupOptionsList.value.find(g => g.groupKey === key)?.name || key

const loadCategories = async () => {
  loading.value = true
  try {
    const res = await api.get('/admin/category/page', {
      params: { page: pagination.page, size: pagination.size, name: searchName.value || undefined }
    })
    categories.value = res.data?.list || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadCategories()
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, { id: null, name: '', icon: '', iconUrl: '', description: '', sort: 0, groupKeys: [] })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    name: row.name,
    icon: row.icon || '',
    iconUrl: row.iconUrl || '',
    description: row.description || '',
    sort: row.sort ?? 0,
    groupKeys: row.groupKeys || []
  })
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除分类「${row.name}」吗？分类下有题目时将无法删除。`,
      '删除确认',
      { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' }
    )
    await api.delete(`/admin/category/${row.id}`)
    ElMessage.success('删除成功')
    loadCategories()
    loadGroups()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (isEdit.value) {
        await api.put('/admin/category', form)
        ElMessage.success('更新成功')
      } else {
        await api.post('/admin/category', form)
        ElMessage.success('添加成功')
      }
      dialogVisible.value = false
      loadCategories()
      loadGroups()
    } catch (error) {
      console.error(error)
    } finally {
      submitLoading.value = false
    }
  })
}

/* ==================== 分组标签 ==================== */
const groupLoading = ref(false)
const groupSubmitLoading = ref(false)
const groupDialogVisible = ref(false)
const isGroupEdit = ref(false)
const groups = ref([])
const groupFormRef = ref(null)

// 分组列表每页条数持久化
const groupPagination = reactive({ page: 1, size: Number(localStorage.getItem('category_group_page_size')) || 10, total: 0 })
watch(() => groupPagination.size, v => localStorage.setItem('category_group_page_size', String(v)))

// 全量分组列表：用于分组标签下拉选项与 key 翻译（分页后 groups 只含当前页数据）
const groupOptionsList = ref([])

const loadGroupOptions = async () => {
  try {
    const res = await api.get('/admin/category-group/list', { params: { pageNum: 1, pageSize: 999 } })
    groupOptionsList.value = res.data?.list || []
  } catch (error) {
    console.error(error)
  }
}

const groupForm = reactive({ id: null, name: '', groupKey: '', sort: 0, status: 1 })

const groupRules = {
  name: [{ required: true, message: '请输入标签名称', trigger: 'blur' }],
  groupKey: [
    { required: true, message: '请输入分组标识', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_-]+$/, message: '仅限字母、数字、下划线和短横线', trigger: 'blur' }
  ]
}

const loadGroups = async () => {
  groupLoading.value = true
  try {
    const res = await api.get('/admin/category-group/list', {
      params: { pageNum: groupPagination.page, pageSize: groupPagination.size }
    })
    groups.value = res.data?.list || []
    groupPagination.total = res.data?.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    groupLoading.value = false
  }
}

const handleAddGroup = () => {
  isGroupEdit.value = false
  Object.assign(groupForm, { id: null, name: '', groupKey: '', sort: 0, status: 1 })
  groupDialogVisible.value = true
}

const handleEditGroup = (row) => {
  isGroupEdit.value = true
  Object.assign(groupForm, { id: row.id, name: row.name, groupKey: row.groupKey, sort: row.sort ?? 0, status: row.status ?? 1 })
  groupDialogVisible.value = true
}

const handleDeleteGroup = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除分组标签「${row.name}」吗？将同时解除与 ${row.categoryCount} 个分类的绑定。`,
      '删除确认',
      { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' }
    )
    await api.delete(`/admin/category-group/${row.id}`)
    ElMessage.success('删除成功')
    loadGroups()
    loadGroupOptions()
    loadCategories()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const handleGroupSubmit = async () => {
  if (!groupFormRef.value) return
  await groupFormRef.value.validate(async (valid) => {
    if (!valid) return
    groupSubmitLoading.value = true
    try {
      if (isGroupEdit.value) {
        await api.put('/admin/category-group', groupForm)
        ElMessage.success('更新成功')
      } else {
        await api.post('/admin/category-group', groupForm)
        ElMessage.success('添加成功')
      }
      groupDialogVisible.value = false
      loadGroups()
      loadGroupOptions()
    } catch (error) {
      console.error(error)
    } finally {
      groupSubmitLoading.value = false
    }
  })
}

watch(currentTab, (tab) => {
  if (tab === 'group' && !groups.value.length) loadGroups()
})

onMounted(() => {
  loadCategories()
  loadGroups()
  loadGroupOptions()
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

.main-tabs :deep(.el-tabs__header) {
  margin-bottom: 12px;
}

.content-card {
  border-radius: 4px;
  border: 1px solid #f0f0f0;
}

.toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}

.icon-preview {
  width: 28px;
  height: 28px;
  object-fit: contain;
  vertical-align: middle;
  border-radius: 6px;
}

.group-tag {
  margin-right: 4px;
  margin-bottom: 2px;
}

.num-cell {
  white-space: nowrap;
}

.text-muted {
  color: #999;
}

.form-tip {
  font-size: 12px;
  color: #999;
  line-height: 1.4;
  margin-top: 4px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}
</style>
