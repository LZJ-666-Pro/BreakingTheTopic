import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
import { startProgress, doneProgress } from '../utils/pageProgress'

const placeholder = () => import('../views/Placeholder.vue')

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
      // ===== 仪表盘 =====
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '仪表盘', icon: 'HomeFilled' }
      },

      // ===== 用户管理 =====
      {
        path: 'user',
        redirect: '/user/list',
        meta: { icon: 'User' },
        children: [
          {
            path: 'list',
            name: 'User',
            component: () => import('../views/User.vue'),
            meta: { title: '用户列表', icon: 'User' }
          },
          {
            path: 'feedback',
            name: 'Feedback',
            component: () => import('../views/Feedback.vue'),
            meta: { title: '反馈与举报', icon: 'ChatDotRound' }
          }
        ]
      },

      // ===== 题目管理 =====
      {
        path: 'question',
        redirect: '/question/list',
        meta: { icon: 'Document' },
        children: [
          {
            path: 'list',
            name: 'Question',
            component: () => import('../views/Question.vue'),
            meta: { title: '题目列表', icon: 'Document' }
          },
          {
            path: 'add',
            name: 'QuestionAdd',
            component: () => import('../views/QuestionEdit.vue'),
            meta: { title: '新增题目', icon: 'Plus' }
          },
          {
            path: 'edit/:id?',
            name: 'QuestionEdit',
            component: () => import('../views/QuestionEdit.vue'),
            meta: { title: '编辑题目', icon: 'Document' }
          },
          {
            path: 'category',
            name: 'Category',
            component: () => import('../views/Category.vue'),
            meta: { title: '题库设置', icon: 'Folder' }
          },
          {
            path: 'tag',
            name: 'Tag',
            component: () => import('../views/Tag.vue'),
            meta: { title: '标签管理', icon: 'PriceTag' }
          }
        ]
      },

      // ===== 题库专题 =====
      {
        path: 'special',
        redirect: '/special/manage',
        meta: { icon: 'Collection' },
        children: [
          {
            path: 'manage',
            name: 'SpecialManage',
            component: () => import('../views/Special.vue'),
            meta: { title: '专题管理', icon: 'Collection' }
          },
          {
            path: 'daily',
            name: 'SpecialDaily',
            component: () => import('../views/Daily.vue'),
            meta: { title: '每日一题', icon: 'AlarmClock' }
          },
          {
            path: 'edit/:id?',
            name: 'SpecialEdit',
            component: () => import('../views/SpecialEdit.vue'),
            meta: { title: '编辑专题', icon: 'Collection' }
          }
        ]
      },

      // ===== 竞赛活动 =====
      {
        path: 'competition',
        name: 'Competition',
        component: () => import('../views/Competition.vue'),
        meta: { title: '竞赛活动', icon: 'Trophy' }
      },
      {
        path: 'competition/records/:id',
        name: 'CompetitionRecords',
        component: () => import('../views/CompetitionRecords.vue'),
        meta: { title: '竞赛成绩', icon: 'Trophy' }
      },
      {
        path: 'competition/edit/:id?',
        name: 'CompetitionEdit',
        component: () => import('../views/CompetitionEdit.vue'),
        meta: { title: '编辑竞赛', icon: 'Trophy' }
      },

      // ===== 内容管理 =====
      {
        path: 'content',
        redirect: '/content/solution-review',
        meta: { icon: 'Notebook' },
        children: [
          {
            path: 'solution-review',
            name: 'SolutionReview',
            component: () => import('../views/SolutionReview.vue'),
            meta: { title: '题解审核', icon: 'Notebook' }
          },
          {
            path: 'solution-review/edit/:id',
            name: 'SolutionReviewEdit',
            component: () => import('../views/SolutionReviewEdit.vue'),
            meta: { title: '编辑题解', icon: 'Notebook' }
          },
          {
            path: 'article',
            name: 'Article',
            component: () => import('../views/Article.vue'),
            meta: { title: '文章管理', icon: 'Memo' }
          },
          {
            path: 'article/edit/:id?',
            name: 'ArticleEdit',
            component: () => import('../views/ArticleEdit.vue'),
            meta: { title: '编辑文章', icon: 'Memo' }
          },
          {
            path: 'notice',
            name: 'Notice',
            component: () => import('../views/Notice.vue'),
            meta: { title: '公告管理', icon: 'Bell' }
          }
        ]
      },

      // ===== 会员订单 =====
      {
        path: 'membership',
        name: 'Membership',
        component: () => import('../views/Membership.vue'),
        meta: { title: '会员订单', icon: 'Goods' }
      },

      // ===== 数据统计 =====
      {
        path: 'stats',
        name: 'Stats',
        component: () => import('../views/Stats.vue'),
        meta: { title: '数据统计', icon: 'DataAnalysis' }
      },

      // ===== AI 助手监控 =====
      {
        path: 'ai-monitor',
        name: 'AiMonitor',
        component: () => import('../views/AiMonitor.vue'),
        meta: { title: 'AI助手监控', icon: 'Monitor' }
      },
      {
        path: 'ai-monitor/session/:id',
        name: 'AiSessionDetail',
        component: () => import('../views/AiSessionDetail.vue'),
        meta: { title: '会话详情', icon: 'Monitor' }
      },

      // ===== AI 任务（独立） =====
      {
        path: 'ai-tasks',
        name: 'AiTasks',
        component: () => import('../views/AiTaskList.vue'),
        meta: { title: 'AI任务', icon: 'MagicStick' }
      },

      // ===== 系统设置 =====
      {
        path: 'settings',
        redirect: '/settings/config',
        meta: { icon: 'Setting' },
        children: [
          {
            path: 'permission',
            name: 'Permission',
            component: () => import('../views/Permission.vue'),
            meta: { title: '权限管理', icon: 'Key' }
          },
          {
            path: 'config',
            name: 'Settings',
            component: () => import('../views/Settings.vue'),
            meta: { title: '系统配置', icon: 'Setting' }
          },
          {
            path: 'logs',
            name: 'OperationLogs',
            component: () => import('../views/OperationLogs.vue'),
            meta: { title: '操作日志', icon: 'Tickets' }
          }
        ]
      },

      // ===== 个人信息（不在菜单显示） =====
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/Profile.vue'),
        meta: { title: '个人信息', icon: 'UserFilled', hidden: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  startProgress()
  document.title = to.meta.title ? `${to.meta.title} - 破题管理后台` : '破题管理后台'

  const userStore = useUserStore()
  if (to.path !== '/login' && !userStore.token) {
    next('/login')
  } else {
    next()
  }
})

router.afterEach(() => {
  doneProgress()
})

router.onError(() => {
  doneProgress()
})

export default router
