import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'

// 本地 dev：页面同源走 /admin-api，由 vite 代理到生产后端，免配 CORS
export default defineConfig({
  plugins: [uni()],
  server: {
    proxy: {
      '/admin-api': {
        target: 'http://8.155.128.225',
        changeOrigin: true
      }
    }
  }
})
