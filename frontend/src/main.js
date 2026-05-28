import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { useAuthStore } from '@/store/auth'
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'
import '@fortawesome/fontawesome-free/css/all.min.css'
import './assets/styles/main.css'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

app.mount('#app')

window.addEventListener('pageshow', (event) => {
  if (event.persisted) {
    const auth = useAuthStore()
    if (!auth.isLoggedIn) {
      const currentRoute = router.currentRoute.value
      if (currentRoute.meta && currentRoute.meta.requiresAuth) {
        router.replace('/login')
      }
    }
  }
})