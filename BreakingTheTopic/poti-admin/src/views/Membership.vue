<template>
  <div class="page-container">
    <div class="page-header">
      <h2>会员订单</h2>
    </div>

    <el-alert
      class="vip-alert"
      title="当前为模拟支付，订单由用户端开通生成"
      type="info"
      show-icon
      :closable="false"
    />

    <el-card class="content-card" shadow="never">
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <!-- ===== 会员订单 ===== -->
        <el-tab-pane label="会员订单" name="order">
          <div class="filter-bar">
            <el-input
              v-model="orderQuery.nickname"
              placeholder="按昵称筛选"
              clearable
              style="width: 220px"
              @keyup.enter="searchOrders"
              @clear="searchOrders"
            />
            <el-button type="primary" @click="searchOrders">查询</el-button>
            <el-button @click="resetOrders">重置</el-button>
          </div>

          <el-table :data="orders" v-loading="orderLoading">
            <el-table-column prop="orderNo" label="订单号" width="230" show-overflow-tooltip />
            <el-table-column label="昵称" min-width="120" show-overflow-tooltip>
              <template #default="{ row }">{{ row.nickname || '-' }}</template>
            </el-table-column>
            <el-table-column label="金额" width="100" align="center">
              <template #default="{ row }">¥{{ row.amount }}</template>
            </el-table-column>
            <el-table-column label="时长" width="90" align="center">
              <template #default="{ row }">{{ row.months }}个月</template>
            </el-table-column>
            <el-table-column label="支付状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.payStatus === 1 ? 'success' : 'info'" size="small">
                  {{ row.payStatus === 1 ? '已支付' : '未支付' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="模拟支付" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.simulated === 1 ? 'warning' : 'success'" size="small">
                  {{ row.simulated === 1 ? '模拟' : '真实' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="下单时间" width="170" align="center">
              <template #default="{ row }">
                <span class="text-muted">{{ formatTime(row.createTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="支付时间" width="170" align="center">
              <template #default="{ row }">
                <span class="text-muted">{{ formatTime(row.payTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center">
              <template #default="{ row }">
                <el-button link type="danger" @click="deleteOrder(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="orderCurrent"
              v-model:page-size="orderPageSize"
              :total="orderTotal"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next"
              @size-change="loadOrders"
              @current-change="loadOrders"
            />
          </div>
        </el-tab-pane>

        <!-- ===== 会员用户 ===== -->
        <el-tab-pane label="会员用户" name="user">
          <el-table :data="vipUsers" v-loading="userLoading">
            <el-table-column prop="id" label="ID" width="80" align="center" />
            <el-table-column label="头像" width="80" align="center">
              <template #default="{ row }">
                <el-avatar :size="36" :src="row.avatarUrl">{{ (row.nickname || '用')[0] }}</el-avatar>
              </template>
            </el-table-column>
            <el-table-column label="昵称" min-width="140" show-overflow-tooltip>
              <template #default="{ row }">{{ row.nickname || '-' }}</template>
            </el-table-column>
            <el-table-column label="到期时间" width="180" align="center">
              <template #default="{ row }">
                <span class="text-muted">{{ formatTime(row.vipExpireTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="累计消费" width="120" align="center">
              <template #default="{ row }">¥{{ row.totalAmount }}</template>
            </el-table-column>
            <el-table-column label="操作" width="100" align="center">
              <template #default="{ row }">
                <el-button link type="danger" @click="removeVipUser(row)">移除会员</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="userCurrent"
              v-model:page-size="userPageSize"
              :total="userTotal"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next"
              @size-change="loadVipUsers"
              @current-change="loadVipUsers"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import api from '../utils/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDefaultPageSize } from '../utils/config'
import { formatDate } from '../utils/formatDate'

const activeTab = ref('order')
const userLoaded = ref(false)

/** 统一时间显示：兼容 ISO（含T）与空格两种格式 */
const formatTime = t => (t ? formatDate(String(t).replace('T', ' ')) : '-')

// ===== 会员订单 =====
const orderLoading = ref(false)
const orders = ref([])
const orderCurrent = ref(1)
// 每页条数持久化：用户选择优先，其次系统配置默认值
const orderPageSize = ref(Number(localStorage.getItem('vip_order_page_size')) || getDefaultPageSize())
watch(orderPageSize, v => localStorage.setItem('vip_order_page_size', String(v)))
const orderTotal = ref(0)
const orderQuery = reactive({ nickname: '' })

/** 加载会员订单分页（昵称筛选由后端先查 user 再 in 订单表） */
const loadOrders = async () => {
  orderLoading.value = true
  try {
    const res = await api.get('/admin/vip/order/page', {
      params: {
        pageNum: orderCurrent.value,
        pageSize: orderPageSize.value,
        nickname: orderQuery.nickname.trim() || undefined
      }
    })
    orders.value = res.data.list || []
    orderTotal.value = res.data.total || 0
  } catch (error) {
    console.error('加载会员订单失败', error)
  } finally {
    orderLoading.value = false
  }
}

const searchOrders = () => {
  orderCurrent.value = 1
  loadOrders()
}

const resetOrders = () => {
  orderQuery.nickname = ''
  searchOrders()
}

/**
 * 删除订单（完整闭环）：删除订单记录并同步回收该订单赋予的会员权益
 * （后端按用户剩余有效订单重算到期时间，全部删光则会员回收）。
 */
const deleteOrder = (row) => {
  ElMessageBox.confirm(
    `确认删除订单 ${row.orderNo}（¥${row.amount} / ${row.months}个月）？删除后将按剩余订单重新计算该用户的会员到期时间，此操作不可恢复。`,
    '删除订单',
    { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    try {
      const res = await api.delete(`/admin/vip/order/${row.id}`)
      if (res.data && res.data.vipExpireTime) {
        ElMessage.success('订单已删除，会员到期时间已重算')
      } else {
        ElMessage.success('订单已删除，该用户会员权益已回收')
      }
      loadOrders()
    } catch (error) {
      console.error('删除订单失败', error)
      ElMessage.error((error.response && error.response.data && error.response.data.msg) || '删除失败，请重试')
    }
  }).catch(() => {})
}

/**
 * 移除会员身份（完整闭环）：回收该用户的会员权益（到期时间清空、立即掉出会员列表），
 * 不删除用户账号，历史订单记录保留可追溯。
 */
const removeVipUser = (row) => {
  ElMessageBox.confirm(
    `确认移除用户「${row.nickname || row.id}」的会员身份？其会员权益将立即失效，用户账号与订单记录保留。`,
    '移除会员',
    { confirmButtonText: '移除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    try {
      await api.delete(`/admin/vip/user/${row.id}`)
      ElMessage.success('已移除该用户的会员身份')
      loadVipUsers()
    } catch (error) {
      console.error('移除会员失败', error)
      ElMessage.error((error.response && error.response.data && error.response.data.msg) || '移除失败，请重试')
    }
  }).catch(() => {})
}

// ===== 会员用户 =====
const userLoading = ref(false)
const vipUsers = ref([])
const userCurrent = ref(1)
const userPageSize = ref(Number(localStorage.getItem('vip_user_page_size')) || getDefaultPageSize())
watch(userPageSize, v => localStorage.setItem('vip_user_page_size', String(v)))
const userTotal = ref(0)

/** 加载当前有效会员分页（vip_expire_time > now，附累计消费） */
const loadVipUsers = async () => {
  userLoading.value = true
  try {
    const res = await api.get('/admin/vip/user/page', {
      params: { pageNum: userCurrent.value, pageSize: userPageSize.value }
    })
    vipUsers.value = res.data.list || []
    userTotal.value = res.data.total || 0
    userLoaded.value = true
  } catch (error) {
    console.error('加载会员用户失败', error)
  } finally {
    userLoading.value = false
  }
}

/** 页签懒加载：首次切到会员用户时再请求 */
const onTabChange = (name) => {
  if (name === 'user' && !userLoaded.value) {
    loadVipUsers()
  }
}

onMounted(() => {
  loadOrders()
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

.vip-alert {
  margin-bottom: 16px;
}

.content-card {
  border-radius: 4px;
  border: 1px solid #f0f0f0;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
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
</style>
