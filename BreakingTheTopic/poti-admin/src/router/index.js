import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('../layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      },
      {
        path: 'category',
        name: 'Category',
        component: () => import('../views/Category.vue'),
        meta: { title: '分类管理', icon: 'Folder' }
      },
      {
        path: 'question',
        name: 'Question',
        component: () => import('../views/Question.vue'),
        meta: { title: '题目管理', icon: 'Document' }
      },
      {
        path: 'ai-tasks',
        name: 'AiTasks',
        component: () => import('../views/AiTaskList.vue'),
        meta: { title: 'AI任务', icon: 'MagicStick' }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('../views/User.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'feedback',
        name: 'Feedback',
        component: () => import('../views/Feedback.vue'),
        meta: { title: '反馈管理', icon: 'ChatDotRound' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/Profile.vue'),
        meta: { title: '个人信息', icon: 'UserFilled' }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('../views/Settings.vue'),
        meta: { title: '系统设置', icon: 'Setting' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 破题管理后台` : '破题管理后台'
  
  const userStore = useUserStore()
  if (to.path !== '/login' && !userStore.token) {
    next('/login')
  } else {
    next()
  }
})

export default router
