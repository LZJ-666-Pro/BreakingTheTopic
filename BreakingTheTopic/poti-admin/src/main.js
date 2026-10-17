import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import router from './router'
import App from './App.vue'
import { loadDefaultPageSize } from './utils/config'
import './style.css'

const app = createApp(App)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })

// 已登录时在页面挂载前预取「系统默认分页条数」，保证列表页初始化时缓存已就绪
async function bootstrap() {
  if (localStorage.getItem('admin_token')) {
    await loadDefaultPageSize()
  }
  app.mount('#app')
}
bootstrap()
