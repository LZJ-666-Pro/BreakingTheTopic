<template>
  <div class="layout">
    <el-container>
      <el-aside :width="isCollapse ? '64px' : '200px'" class="aside">
        <div class="logo">
          <img class="logo-icon" src="../static/log.png" alt="破题 Logo" />
          <transition name="fade">
            <span v-if="!isCollapse" class="logo-text">破题管理</span>
          </transition>
        </div>
        <el-menu
          ref="menuRef"
          :default-active="activeMenu"
          :default-openeds="defaultOpeneds"
          router
          :collapse="isCollapse"
          background-color="transparent"
          text-color="rgba(255,255,255,0.65)"
          active-text-color="#fff"
          class="sidebar-menu"
        >
          <template v-for="menu in menus" :key="menu.index">
            <!-- 分组菜单 -->
            <el-sub-menu v-if="menu.children" :index="menu.index">
              <template #title>
                <el-icon><component :is="menu.icon" /></el-icon>
                <span>{{ menu.title }}</span>
              </template>
              <el-menu-item v-for="child in menu.children" :key="child.index" :index="child.index">
                {{ child.title }}
              </el-menu-item>
            </el-sub-menu>
            <!-- 单项菜单 -->
            <el-menu-item v-else :index="menu.index">
              <el-icon><component :is="menu.icon" /></el-icon>
              <template #title>{{ menu.title }}</template>
            </el-menu-item>
          </template>
        </el-menu>
        <div class="collapse-btn" @click="toggleCollapse">
          <el-icon :size="16">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
        </div>
      </el-aside>
      <el-container>
        <el-header class="header">
          <div class="header-left">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item v-if="currentRoute.meta?.title">{{ currentRoute.meta.title }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="header-right">
            <el-tooltip content="刷新" placement="bottom">
              <div class="header-action" @click="refreshPage">
                <el-icon :size="18"><Refresh /></el-icon>
              </div>
            </el-tooltip>
            <el-tooltip content="全屏" placement="bottom">
              <div class="header-action" @click="toggleFullscreen">
                <el-icon :size="18"><FullScreen /></el-icon>
              </div>
            </el-tooltip>
            <el-dropdown trigger="click">
              <div class="user-dropdown">
                <el-avatar :size="32" :src="userStore.userInfo?.avatar">
                  {{ userStore.userInfo?.nickname?.charAt(0) || 'A' }}
                </el-avatar>
                <span class="user-name">{{ userStore.userInfo?.nickname || '管理员' }}</span>
                <el-icon :size="12"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="router.push('/profile')">
                    <el-icon><User /></el-icon>
                    个人信息
                  </el-dropdown-item>
                  <el-dropdown-item @click="router.push('/settings/config')">
                    <el-icon><Setting /></el-icon>
                    系统设置
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        <el-main class="main">
          <router-view v-slot="{ Component }">
            <transition name="fade-transform" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, watch, markRaw } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import {
  HomeFilled,
  Folder,
  Document,
  User,
  Fold,
  Expand,
  Refresh,
  FullScreen,
  ArrowDown,
  Setting,
  SwitchButton,
  MagicStick,
  ChatDotRound,
  Plus,
  PriceTag,
  Collection,
  AlarmClock,
  Trophy,
  Notebook,
  Memo,
  Bell,
  Goods,
  DataAnalysis,
  Key,
  Tickets
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)

// ===== 侧边栏菜单结构 =====
const menus = [
  { index: '/dashboard', title: '仪表盘', icon: markRaw(HomeFilled) },
  {
    index: '/user',
    title: '用户管理',
    icon: markRaw(User),
    children: [
      { index: '/user/list', title: '用户列表' },
      { index: '/user/feedback', title: '反馈与举报' }
    ]
  },
  {
    index: '/question',
    title: '题目管理',
    icon: markRaw(Document),
    children: [
      { index: '/question/list', title: '题目列表' },
      { index: '/question/add', title: '新增题目' },
      { index: '/question/category', title: '分类管理' },
      { index: '/question/tag', title: '标签管理' }
    ]
  },
  {
    index: '/special',
    title: '题库专题',
    icon: markRaw(Collection),
    children: [
      { index: '/special/manage', title: '专题管理' },
      { index: '/special/daily', title: '每日一题' }
    ]
  },
  { index: '/competition', title: '竞赛活动', icon: markRaw(Trophy) },
  {
    index: '/content',
    title: '内容管理',
    icon: markRaw(Notebook),
    children: [
      { index: '/content/solution-review', title: '题解审核' },
      { index: '/content/article', title: '文章管理' },
      { index: '/content/notice', title: '公告管理' }
    ]
  },
  { index: '/membership', title: '会员订单', icon: markRaw(Goods) },
  { index: '/stats', title: '数据统计', icon: markRaw(DataAnalysis) },
  { index: '/ai-tasks', title: 'AI任务', icon: markRaw(MagicStick) },
  {
    index: '/settings',
    title: '系统设置',
    icon: markRaw(Setting),
    children: [
      { index: '/settings/permission', title: '权限管理' },
      { index: '/settings/config', title: '系统配置' },
      { index: '/settings/logs', title: '操作日志' }
    ]
  }
]

const activeMenu = computed(() => route.path)
const currentRoute = computed(() => route)

// 当前路由所属的一级分组
const topGroupIndex = computed(() => {
  const top = '/' + (route.path.split('/')[1] || '')
  return menus.some(m => m.index === top && m.children) ? top : null
})

// 刷新页面时自动展开当前分组
const defaultOpeneds = computed(() => (topGroupIndex.value ? [topGroupIndex.value] : []))

// 运行时跨分组跳转（如工作台快捷按钮）自动展开对应分组
const menuRef = ref()
watch(
  () => route.path,
  () => {
    if (topGroupIndex.value && !isCollapse.value) {
      menuRef.value?.open(topGroupIndex.value)
    }
  },
  { immediate: true }
)

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const refreshPage = () => {
  router.go(0)
}

const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  height: 100%;
}

.aside {
  background: #0F172A;
  transition: width 0.2s;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.logo {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 0 16px;
  background: rgba(255, 255, 255, 0.02);
}

.logo-icon {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
  border-radius: 8px;
  display: block;
  object-fit: cover;
}

.logo-text {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  white-space: nowrap;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.sidebar-menu {
  flex: 1;
  border-right: none;
  padding: 8px 0;
}

.sidebar-menu :deep(.el-menu-item) {
  height: 44px;
  line-height: 44px;
  margin: 0;
  padding: 0 24px !important;
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background-color: rgba(255, 255, 255, 0.08);
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background-color: var(--color-primary);
}

.sidebar-menu :deep(.el-menu-item.is-active::before) {
  content: none;
}

.collapse-btn {
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.65);
  cursor: pointer;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.2s;
}

.collapse-btn:hover {
  color: #fff;
  background-color: rgba(255, 255, 255, 0.08);
}

.header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 56px;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-action {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  cursor: pointer;
  color: #666;
  transition: all 0.2s;
}

.header-action:hover {
  background-color: #f5f5f5;
  color: var(--color-primary);
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.2s;
}

.user-dropdown:hover {
  background-color: #f5f5f5;
}

.user-name {
  font-size: 14px;
  color: #333;
}

.main {
  background-color: var(--color-bg);
  padding: 20px;
  min-height: calc(100vh - 56px);
}

.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.2s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-10px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(10px);
}
</style>
