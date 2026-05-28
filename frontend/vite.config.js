import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3000,
    proxy: [
      {
        context: [
          '/auth', '/user', '/missing-persons', '/admin', '/clues',
          '/success-cases', '/community', '/notifications', '/tasks',
          '/volunteer', '/volunteer-activities', '/ai', '/uploads'
        ],
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    ]
  },
  build: {
    outDir: 'dist',
    assetsDir: 'assets'
  }
})