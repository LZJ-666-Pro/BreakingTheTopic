<template>
  <div class="page-card">
    <!-- 顶部操作 -->
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="搜索专题名称" clearable style="width: 220px" @keyup.enter="search" @clear="search" />
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px" @change="search">
        <el-option label="已上架" :value="1" />
        <el-option label="已下架" :value="0" />
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button type="success" @click="goEdit()">+ 新增专题</el-button>
    </div>

    <!-- 列表 -->
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="专题名称" min-width="180" show-overflow-tooltip />
      <el-table-column label="难度" width="80">
        <template #default="{ row }">
          <el-tag :type="difficultyType(row.difficulty)" size="small">{{ difficultyLabel(row.difficulty) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="questionCount" label="题目数" width="80" />
      <el-table-column prop="estimatedDays" label="预计天数" width="90" />
      <el-table-column label="解锁规则" width="100">
        <template #default="{ row }">{{ row.unlockType === 1 ? '按顺序解锁' : '自由刷' }}</template>
      </el-table-column>
      <el-table-column prop="sort" label="权重" width="70" />
      <el-table-column label="上架" width="80">
        <template #default="{ row }">
          <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="toggleStatus(row)" />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170">
        <template #default="{ row }">{{ fmtTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="goEdit(row)">编辑</el-button>
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
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../utils/api'
import { getDefaultPageSize } from '../utils/config'

const router = useRouter()

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: Number(localStorage.getItem('special_page_size')) || getDefaultPageSize(), keyword: '', status: null })
// 每页条数持久化：用户选择优先，其次系统配置默认值
watch(() => query.pageSize, v => localStorage.setItem('special_page_size', String(v)))

const difficultyLabel = (d) => ({ 1: '简单', 2: '中等', 3: '困难' }[d] || '中等')
const difficultyType = (d) => ({ 1: 'success', 2: 'warning', 3: 'danger' }[d] || 'info')
const fmtTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '')

function search() {
  query.pageNum = 1
  loadList()
}

async function loadList() {
  loading.value = true
  try {
    const res = await api.get('/admin/special/list', { params: { ...query } })
    list.value = res.data.list || []
    total.value = res.data.total || 0
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function goEdit(row) {
  router.push(row ? `/special/edit/${row.id}` : '/special/edit')
}

async function toggleStatus(row) {
  try {
    await api.put('/admin/special', { id: row.id, status: row.status, name: row.name })
    ElMessage.success(row.status === 1 ? '已上架' : '已下架')
  } catch (e) {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error(e?.message || '操作失败')
  }
}

function remove(row) {
  ElMessageBox.confirm(`确定删除专题「${row.name}」？`, '提示', { type: 'warning' }).then(async () => {
    try {
      await api.delete(`/admin/special/${row.id}`)
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
.toolbar .el-button {
  margin-left: 0;
}
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
