<template>
  <div class="page-container">
    <div class="toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索标签名称"
        clearable
        class="search-input"
        @keyup.enter="() => loadList(true)"
        @clear="() => loadList(true)"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="query.groupName" placeholder="全部分组" clearable style="width: 140px" @change="() => loadList(true)">
        <el-option v-for="g in groupOptions" :key="g" :label="g" :value="g" />
      </el-select>
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 120px" @change="() => loadList(true)">
        <el-option label="启用" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" @click="() => loadList(true)">查询</el-button>
      <div class="toolbar-right">
        <el-button type="primary" @click="openForm()">
          <el-icon><Plus /></el-icon>新增标签
        </el-button>
      </div>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column label="标签" min-width="160">
        <template #default="{ row }">
          <div class="tag-cell">
            <span class="color-dot" :style="{ background: row.color || '#409EFF' }" />
            <span>{{ row.name }}</span>
            <el-tag v-if="row.hot" type="danger" size="small" effect="plain">热门</el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="分组" width="110">
        <template #default="{ row }">
          <span v-if="row.groupName">{{ row.groupName }}</span>
          <span v-else class="text-muted">未分组</span>
        </template>
      </el-table-column>
      <el-table-column label="描述" min-width="160" show-overflow-tooltip>
        <template #default="{ row }">{{ row.description || '-' }}</template>
      </el-table-column>
      <el-table-column label="关联题目数" width="110" align="center">
        <template #default="{ row }">
          <span class="count-num">{{ row.questionCount }}</span>
        </template>
      </el-table-column>
      <el-table-column label="排序权重" prop="sort" width="90" align="center" />
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-switch :model-value="row.status === 1" @change="val => toggleStatus(row, val)" />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="110">
        <template #default="{ row }">{{ (row.createTime || '').slice(0, 10) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="130" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="openForm(row)">编辑</el-button>
          <el-button link type="danger" size="small" @click="removeTag(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="query.pageNum"
      v-model:page-size="query.pageSize"
      :total="total"
      layout="total, prev, pager, next, sizes"
      :page-sizes="[10, 20, 50, 100]"
      class="pagination"
      @current-change="() => loadList()"
      @size-change="savePageSize"
    />

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="formVisible" :title="form.id ? '编辑标签' : '新增标签'" width="480px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="标签名称" required>
          <el-input v-model="form.name" placeholder="如：动态规划" maxlength="50" />
        </el-form-item>
        <el-form-item label="所属分组">
          <el-select v-model="form.groupName" placeholder="选择分组（可选）" clearable filterable allow-create style="width: 100%">
            <el-option v-for="g in groupOptions" :key="g" :label="g" :value="g" />
          </el-select>
        </el-form-item>
        <el-form-item label="颜色">
          <el-color-picker v-model="form.color" show-alpha />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" maxlength="200" placeholder="简短说明这个标签是什么（可选）" />
        </el-form-item>
        <el-form-item label="排序权重">
          <el-input-number v-model="form.sort" :min="0" :max="99999" />
          <span class="form-tip">数字越大越靠前</span>
        </el-form-item>
        <el-form-item label="热门标签">
          <el-switch v-model="form.hotBool" active-text="前台优先展示" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.statusBool" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import api from '../utils/api'

const groupOptions = ['算法', '数据结构', '语言基础', '框架与中间件', '数据库', '运维部署', '数学基础', '面试场景', '公司真题']

const loading = ref(false)
const list = ref([])
const total = ref(0)
const PAGE_SIZE_KEY = 'tag_page_size'
const query = reactive({
  pageNum: 1,
  pageSize: Number(localStorage.getItem(PAGE_SIZE_KEY)) || 20,
  keyword: '',
  groupName: '',
  status: ''
})

// 持久化每页条数，刷新后保持
function savePageSize() {
  localStorage.setItem(PAGE_SIZE_KEY, String(query.pageSize))
  loadList(true)
}

const formVisible = ref(false)
const saving = ref(false)
const form = reactive({ id: null, name: '', groupName: '', color: '', description: '', sort: 0, hotBool: false, statusBool: true })

async function loadList(resetPage) {
  if (resetPage) query.pageNum = 1
  loading.value = true
  try {
    const res = await api.get('/admin/tag/list', {
      params: {
        pageNum: query.pageNum,
        pageSize: query.pageSize,
        keyword: query.keyword || undefined,
        groupName: query.groupName || undefined,
        status: query.status === '' ? undefined : query.status
      }
    })
    const d = res.data || {}
    list.value = d.list || []
    total.value = d.total || 0
  } catch (e) {
    console.error('加载标签失败', e)
  } finally {
    loading.value = false
  }
}

function openForm(row) {
  if (row) {
    Object.assign(form, {
      id: row.id,
      name: row.name,
      groupName: row.groupName || '',
      color: row.color || '',
      description: row.description || '',
      sort: row.sort ?? 0,
      hotBool: row.hot === 1,
      statusBool: row.status === 1
    })
  } else {
    Object.assign(form, { id: null, name: '', groupName: '', color: '', description: '', sort: 0, hotBool: false, statusBool: true })
  }
  formVisible.value = true
}

async function save() {
  if (!form.name.trim()) {
    ElMessage.warning('请填写标签名称')
    return
  }
  saving.value = true
  try {
    const body = {
      id: form.id,
      name: form.name.trim(),
      groupName: form.groupName || '',
      color: form.color || '',
      description: form.description || '',
      sort: form.sort,
      hot: form.hotBool ? 1 : 0,
      status: form.statusBool ? 1 : 0
    }
    const res = form.id ? await api.put('/admin/tag', body) : await api.post('/admin/tag', body)
    if (res.code === 200) {
      ElMessage.success(form.id ? '保存成功' : '新增成功')
      formVisible.value = false
      loadList()
    } else {
      ElMessage.error(res.msg || '保存失败')
    }
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row, val) {
  try {
    const res = await api.put('/admin/tag', { id: row.id, status: val ? 1 : 0 })
    if (res.code === 200) {
      row.status = val ? 1 : 0
      ElMessage.success(val ? '已启用' : '已禁用')
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

async function removeTag(row) {
  const confirmed = await ElMessageBox.confirm(
    `确定删除标签「${row.name}」吗？删除后前台不再显示该标签（题目上已打的标签保留）。`,
    '删除标签',
    { type: 'warning' }
  ).catch(() => null)
  if (!confirmed) return
  try {
    const res = await api.delete(`/admin/tag/${row.id}`)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadList()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

onMounted(loadList)
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.search-input {
  width: 220px;
}

.toolbar-right {
  margin-left: auto;
}

.tag-cell {
  display: flex;
  align-items: center;
  gap: 6px;
}

.color-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.count-num {
  font-weight: 600;
  color: #409eff;
}

.text-muted {
  color: #999;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

.form-tip {
  margin-left: 10px;
  font-size: 12px;
  color: #999;
}
</style>
