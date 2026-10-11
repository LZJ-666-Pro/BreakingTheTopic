<template>
  <div class="page">
    <el-card shadow="never">
      <el-tabs v-model="activeTab">
        <!-- ===== Tab 1：管理员账号 ===== -->
        <el-tab-pane label="管理员账号" name="account">
          <div class="toolbar">
            <el-button type="primary" @click="openAccountDialog()">+ 新增管理员</el-button>
          </div>
          <el-table :data="accounts" v-loading="loading" stripe>
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="username" label="用户名" width="130" />
            <el-table-column prop="nickname" label="昵称" width="130" />
            <el-table-column label="角色" width="140">
              <template #default="{ row }">
                <el-tag v-if="row.roleName" :type="row.role === 2 ? 'danger' : 'info'" size="small">{{ row.roleName }}</el-tag>
                <span v-else class="muted">未分配</span>
              </template>
            </el-table-column>
            <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="lastLoginTime" label="最后登录" width="160">
              <template #default="{ row }">{{ (row.lastLoginTime || '').replace('T', ' ') || '—' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="250" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="openAccountDialog(row)">编辑</el-button>
                <el-button link type="primary" size="small" @click="openResetPwd(row)">重置密码</el-button>
                <el-button link type="danger" size="small" @click="deleteAccount(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- ===== Tab 2：角色管理 ===== -->
        <el-tab-pane label="角色管理" name="role">
          <div class="toolbar">
            <el-button type="primary" @click="openRoleDialog()">+ 新增角色</el-button>
          </div>
          <el-table :data="roles" v-loading="roleLoading" stripe>
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="name" label="角色名称" width="150" />
            <el-table-column prop="code" label="编码" width="170" />
            <el-table-column label="菜单权限" min-width="300">
              <template #default="{ row }">
                <span v-if="!row.permissions || row.code === 'super'" class="muted">全部权限</span>
                <span v-else class="perm-text">{{ formatPermissions(row.permissions) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="openRoleDialog(row)">编辑</el-button>
                <el-button v-if="row.code !== 'super'" link type="danger" size="small" @click="deleteRole(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 新增/编辑管理员弹窗 -->
    <el-dialog v-model="accountDialog" :title="accountForm.id ? '编辑管理员' : '新增管理员'" width="480px">
      <el-form :model="accountForm" label-width="80px">
        <el-form-item label="用户名" required>
          <el-input v-model="accountForm.username" :disabled="!!accountForm.id" placeholder="3-20位字符" />
        </el-form-item>
        <el-form-item v-if="!accountForm.id" label="密码" required>
          <el-input v-model="accountForm.password" type="password" autocomplete="new-password" placeholder="6-20位字符" show-password />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="accountForm.nickname" placeholder="显示名称" />
        </el-form-item>
        <el-form-item label="角色" required>
          <el-select v-model="accountForm.roleId" placeholder="选择角色" style="width: 100%">
            <el-option v-for="r in enabledRoles" :key="r.id" :label="r.name" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="accountForm.email" placeholder="选填" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="accountForm.phone" placeholder="选填" />
        </el-form-item>
        <el-form-item v-if="accountForm.id" label="状态">
          <el-radio-group v-model="accountForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="accountDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveAccount">保存</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码弹窗 -->
    <el-dialog v-model="pwdDialog" title="重置密码" width="420px">
      <el-form label-width="80px">
        <el-form-item label="账号">
          <span>{{ pwdForm.username }}</span>
        </el-form-item>
        <el-form-item label="新密码" required>
          <el-input v-model="pwdForm.newPassword" type="password" autocomplete="new-password" placeholder="6-20位字符" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveResetPwd">确认重置</el-button>
      </template>
    </el-dialog>

    <!-- 新增/编辑角色弹窗 -->
    <el-dialog v-model="roleDialog" :title="roleForm.id ? '编辑角色' : '新增角色'" width="520px">
      <el-form :model="roleForm" label-width="80px">
        <el-form-item label="角色名称" required>
          <el-input v-model="roleForm.name" placeholder="如：内容运营" />
        </el-form-item>
        <el-form-item label="编码" required>
          <el-input v-model="roleForm.code" :disabled="roleForm.code === 'super'" placeholder="以字母开头的英文编码" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="roleForm.remark" placeholder="角色职责说明" />
        </el-form-item>
        <el-form-item label="菜单权限">
          <div class="perm-tree-wrap">
            <el-checkbox v-model="allChecked" :disabled="roleForm.code === 'super'" @change="toggleAll">全部权限</el-checkbox>
            <el-tree
              ref="permTreeRef"
              :data="menuTree"
              show-checkbox
              node-key="index"
              :props="{ label: 'title', children: 'children' }"
              :check-strictly="false"
              @check="onPermCheck"
            />
            <div class="perm-tip">不勾选任何权限时视为「全部权限」</div>
          </div>
        </el-form-item>
        <el-form-item v-if="roleForm.id && roleForm.code !== 'super'" label="状态">
          <el-radio-group v-model="roleForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveRole">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../utils/api'

// 与侧边栏菜单一致的权限树（叶子 index 为路由地址）
const menuTree = [
  { index: '/dashboard', title: '仪表盘' },
  {
    index: '/user', title: '用户管理', children: [
      { index: '/user/list', title: '用户列表' },
      { index: '/user/feedback', title: '反馈与举报' }
    ]
  },
  {
    index: '/question', title: '题目管理', children: [
      { index: '/question/list', title: '题目列表' },
      { index: '/question/add', title: '新增题目' },
      { index: '/question/category', title: '分类管理' },
      { index: '/question/tag', title: '标签管理' }
    ]
  },
  {
    index: '/special', title: '题库专题', children: [
      { index: '/special/manage', title: '专题管理' },
      { index: '/special/daily', title: '每日一题' }
    ]
  },
  { index: '/competition', title: '竞赛活动' },
  {
    index: '/content', title: '内容管理', children: [
      { index: '/content/solution-review', title: '题解审核' },
      { index: '/content/article', title: '文章管理' },
      { index: '/content/notice', title: '公告管理' }
    ]
  },
  { index: '/membership', title: '会员订单' },
  { index: '/stats', title: '数据统计' },
  { index: '/ai-tasks', title: 'AI任务' },
  {
    index: '/settings', title: '系统设置', children: [
      { index: '/settings/permission', title: '权限管理' },
      { index: '/settings/config', title: '系统配置' },
      { index: '/settings/logs', title: '操作日志' }
    ]
  }
]

const activeTab = ref('account')
const loading = ref(false)
const roleLoading = ref(false)
const saving = ref(false)
const accounts = ref([])
const roles = ref([])
const permTreeRef = ref()
const allChecked = ref(false)

const enabledRoles = computed(() => roles.value.filter(r => r.status === 1))

// ===== 管理员账号 =====
const accountDialog = ref(false)
const accountForm = ref({ id: null, username: '', password: '', nickname: '', roleId: null, email: '', phone: '', status: 1 })

const pwdDialog = ref(false)
const pwdForm = ref({ id: null, username: '', newPassword: '' })

const loadAccounts = async () => {
  loading.value = true
  try {
    const res = await api.get('/admin/account/list')
    accounts.value = res.data || []
  } finally {
    loading.value = false
  }
}

const openAccountDialog = (row) => {
  accountForm.value = row
    ? { id: row.id, username: row.username, password: '', nickname: row.nickname || '', roleId: row.roleId, email: row.email || '', phone: row.phone || '', status: row.status }
    : { id: null, username: '', password: '', nickname: '', roleId: null, email: '', phone: '', status: 1 }
  accountDialog.value = true
}

const saveAccount = async () => {
  if (!accountForm.value.id && !accountForm.value.username) return ElMessage.warning('请输入用户名')
  if (!accountForm.value.id && !accountForm.value.password) return ElMessage.warning('请输入密码')
  if (!accountForm.value.roleId) return ElMessage.warning('请选择角色')
  saving.value = true
  try {
    if (accountForm.value.id) {
      await api.put('/admin/account/update', accountForm.value)
    } else {
      await api.post('/admin/account/create', accountForm.value)
    }
    ElMessage.success('保存成功')
    accountDialog.value = false
    loadAccounts()
  } finally {
    saving.value = false
  }
}

const openResetPwd = (row) => {
  pwdForm.value = { id: row.id, username: row.username, newPassword: '' }
  pwdDialog.value = true
}

const saveResetPwd = async () => {
  if (!pwdForm.value.newPassword || pwdForm.value.newPassword.length < 6) {
    return ElMessage.warning('新密码至少6位')
  }
  saving.value = true
  try {
    await api.post(`/admin/account/reset-password/${pwdForm.value.id}`, { newPassword: pwdForm.value.newPassword })
    ElMessage.success('密码已重置')
    pwdDialog.value = false
  } finally {
    saving.value = false
  }
}

const deleteAccount = (row) => {
  ElMessageBox.confirm(`确定删除管理员「${row.username}」吗？删除后无法恢复`, '删除确认', { type: 'warning' })
    .then(async () => {
      await api.delete(`/admin/account/delete/${row.id}`)
      ElMessage.success('已删除')
      loadAccounts()
    })
    .catch(() => {})
}

// ===== 角色管理 =====
const roleDialog = ref(false)
const roleForm = ref({ id: null, name: '', code: '', permissions: '', remark: '', status: 1 })

const loadRoles = async () => {
  roleLoading.value = true
  try {
    const res = await api.get('/admin/role/list')
    roles.value = res.data || []
  } finally {
    roleLoading.value = false
  }
}

const formatPermissions = (permissions) => {
  const indexes = (permissions || '').split(',').filter(Boolean)
  const nameMap = {}
  const walk = (nodes) => {
    nodes.forEach(n => { nameMap[n.index] = n.title; if (n.children) walk(n.children) })
  }
  walk(menuTree)
  return indexes.map(i => nameMap[i] || i).join('、')
}

const openRoleDialog = async (row) => {
  roleForm.value = row
    ? { id: row.id, name: row.name, code: row.code, permissions: row.permissions || '', remark: row.remark || '', status: row.status }
    : { id: null, name: '', code: '', permissions: '', remark: '', status: 1 }
  roleDialog.value = true
  await nextTick()
  // 回显勾选
  const checked = (roleForm.value.permissions || '').split(',').filter(Boolean)
  if (permTreeRef.value) {
    permTreeRef.value.setCheckedKeys(checked)
  }
  // 编辑时按已存权限判断是否「全部」；新增默认不勾，避免新角色意外获得全部权限
  allChecked.value = row ? checked.length === 0 : false
}

const toggleAll = (val) => {
  if (val && permTreeRef.value) {
    permTreeRef.value.setCheckedKeys([])
  }
}

const onPermCheck = () => {
  allChecked.value = false
}

const saveRole = async () => {
  if (!roleForm.value.name) return ElMessage.warning('请输入角色名称')
  if (!roleForm.value.id && !roleForm.value.code) return ElMessage.warning('请输入角色编码')
  saving.value = true
  try {
    // getCheckedKeys 含全勾选的父节点，回显时父子联动语义一致；空数组 = 全部权限
    const checked = permTreeRef.value ? permTreeRef.value.getCheckedKeys() : []
    roleForm.value.permissions = checked.join(',')
    if (roleForm.value.id) {
      await api.put('/admin/role/update', roleForm.value)
    } else {
      await api.post('/admin/role/create', roleForm.value)
    }
    ElMessage.success('保存成功')
    roleDialog.value = false
    loadRoles()
  } finally {
    saving.value = false
  }
}

const deleteRole = (row) => {
  ElMessageBox.confirm(`确定删除角色「${row.name}」吗？`, '删除确认', { type: 'warning' })
    .then(async () => {
      await api.delete(`/admin/role/delete/${row.id}`)
      ElMessage.success('已删除')
      loadRoles()
    })
    .catch(() => {})
}

onMounted(() => {
  loadAccounts()
  loadRoles()
})
</script>

<style scoped>
.page {
  padding: 20px;
}
.toolbar {
  margin-bottom: 16px;
}
.muted {
  color: #999;
  font-size: 12px;
}
.perm-text {
  font-size: 12px;
  color: #666;
}
.perm-tree-wrap {
  width: 100%;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 10px;
  max-height: 260px;
  overflow: auto;
}
.perm-tip {
  margin-top: 6px;
  font-size: 12px;
  color: #999;
}
</style>
