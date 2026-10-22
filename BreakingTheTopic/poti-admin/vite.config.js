import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 后端接口与静态媒体的代理配置
// 说明：后端只存相对路径（/avatars/xxx.jpg、/uploads/xxx），
// 开发模式下必须把这几类路径也代理到后端，否则 el-avatar 会 404 退化成首字母。
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000,
    proxy: {
      // 管理端接口：/api/xxx -> 网关 /xxx
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      },
      // 用户服务直连（Dashboard 读取用户信息）
      '/user-api': {
        target: 'http://localhost:8200',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/user-api/, '')
      },
      // 用户头像
      '/avatars': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      // 聊天图片与语音
      '/chat': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      // 后台上传文件（管理员头像、文章配图等）
      '/uploads': {
        target: 'http://localhost:8088',
        changeOrigin: true
      }
    }
  }
})
