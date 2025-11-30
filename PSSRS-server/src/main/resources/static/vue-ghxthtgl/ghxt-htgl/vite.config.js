import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  optimizeDeps: {
    include: ['@element-plus/icons-vue','element-plus','vue','vue-router','pinia']
  },
  base: '/',
  server: {
    headers: {
      'Cache-Control': 'no-store',
      'Pragma': 'no-cache'
    },
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
        // 注意：后端已包含 /api/v1 前缀，这里不做 rewrite，直接透传
      },
      '/ws': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        ws: true,
        secure: false
      }
    }
  },
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: false,
    minify: 'esbuild',
    rollupOptions: {
      output: {
        manualChunks: {
          'vue-core': ['vue'],
          'vue-router': ['vue-router'],
          'pinia': ['pinia'],
          'element-plus': ['element-plus'],
          'element-icons': ['@element-plus/icons-vue']
        }
      }
    }
  }
})
