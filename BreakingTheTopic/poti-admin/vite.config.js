import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8088',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      },
      '/user-api': {
        target: 'http://localhost:8200',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/user-api/, '')
      }
    }
  }
})
