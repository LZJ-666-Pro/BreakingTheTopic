<template>
  <div class="layout">
    <el-container>
      <el-aside :width="isCollapse ? '64px' : '200px'" class="aside">
        <div class="logo">
          <div class="logo-icon">
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <transition name="fade">
            <span v-if="!isCollapse" class="logo-text">破题管理</span>
          </transition>
        </div>
        <el-menu
          :default-active="activeMenu"
          router
          :collapse="isCollapse"
          background-color="transparent"
          text-color="rgba(255,255,255,0.65)"
          active-text-color="#fff"
          class="sidebar-menu"
        >
          <el-menu-item index="/dashboard">
            <el-icon><HomeFilled /></el-icon>
            <template #title>工作台</template>
          </el-menu-item>
          <el-menu-item index="/category">
            <el-icon><Folder /></el-icon>
            <template #title>分类管理</template>
          </el-menu-item>
          <el-menu-item index="/question">
            <el-icon><Document /></el-icon>
            <template #title>题目管理</template>
          </el-menu-item>
          <el-menu-item index="/ai-tasks">
            <el-icon><MagicStick /></el-icon>
            <template #title>AI任务</template>
          </el-menu-item>
          <el-menu-item index="/user">
            <el-icon><User /></el-icon>
            <template #title>用户管理</template>
          </el-menu-item>
          <el-menu-item index="/feedback">
            <el-icon><ChatDotRound /></el-icon>
            <template #title>反馈管理</template>
          </el-menu-item>
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
                  <el-dropdown-item @click="router.push('/settings')">
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
import { ref, computed } from 'vue'
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
  ChatDotRound
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)

const activeMenu = computed(() => route.path)
const currentRoute = computed(() => route)

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
  background: #001529;
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
}

.logo-icon svg {
  width: 100%;
  height: 100%;
  color: #1890ff;
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
  background-color: #1890ff;
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
  color: #1890ff;
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
  background-color: #f0f2f5;
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
