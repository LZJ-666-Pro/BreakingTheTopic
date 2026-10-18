<template>
  <div class="page">
    <el-page-header class="page-header" @back="goBack">
      <template #content>
        <span class="header-title">竞赛成绩</span>
        <span class="header-sub">{{ compTitle }}</span>
      </template>
    </el-page-header>

    <el-card shadow="never">
      <el-table :data="recordList" v-loading="loading">
        <el-table-column prop="rank" label="名次" width="80" align="center">
          <template #default="{ row }">
            <span :class="['rank-badge', { 'rank-top': row.rank <= 3 }]">{{ row.rank }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="昵称" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ row.nickname || '-' }}</template>
        </el-table-column>
        <el-table-column prop="score" label="得分" width="90" align="center">
          <template #default="{ row }">
            <el-tag type="success" size="small">{{ row.score ?? '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="correctCount" label="答对题数" width="100" align="center" />
        <el-table-column label="用时" width="120" align="center">
          <template #default="{ row }">{{ formatCost(row.costSeconds) }}</template>
        </el-table-column>
        <el-table-column label="交卷时间" width="180" align="center">
          <template #default="{ row }">
            <span class="text-muted">{{ formatTime(row.submitTime) }}</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="current"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadRecords"
          @current-change="loadRecords"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../utils/api'
import { getDefaultPageSize } from '../utils/config'
import { formatDate } from '../utils/formatDate'

const route = useRoute()
const router = useRouter()

const compTitle = route.query.title || ''
const compId = route.params.id

const loading = ref(false)
const recordList = ref([])
const current = ref(1)
// 每页条数持久化：用户选择优先，其次系统配置默认值
const size = ref(Number(localStorage.getItem('competition_records_page_size')) || getDefaultPageSize())
watch(size, v => localStorage.setItem('competition_records_page_size', String(v)))
const total = ref(0)

const goBack = () => {
  router.push('/competition')
}

const loadRecords = async () => {
  loading.value = true
  try {
    const res = await api.get(`/admin/competition/${compId}/records`, {
      params: { pageNum: current.value, pageSize: size.value }
    })
    recordList.value = res.data.list || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('加载成绩失败', error)
  } finally {
    loading.value = false
  }
}

const formatTime = t => (t ? formatDate(String(t).replace('T', ' ')) : '-')

const formatCost = s => {
  const sec = Number(s) || 0
  if (sec < 60) return `${sec}秒`
  const m = Math.floor(sec / 60)
  const r = sec % 60
  return r ? `${m}分${r}秒` : `${m}分钟`
}

onMounted(() => {
  loadRecords()
})
</script>

<style scoped>
.page-header {
  margin-bottom: 16px;
}

.header-title {
  font-size: 17px;
  font-weight: 600;
}

.header-sub {
  margin-left: 12px;
  font-size: 14px;
  color: #909399;
  font-weight: 400;
}

.rank-badge {
  display: inline-block;
  min-width: 24px;
  line-height: 24px;
  font-weight: 600;
}

.rank-top {
  color: #e6a23c;
}

.text-muted {
  color: #909399;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}
</style>
