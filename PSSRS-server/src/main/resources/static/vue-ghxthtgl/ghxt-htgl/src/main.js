import { createApp } from 'vue'
import './style.css'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import { createPinia } from 'pinia'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
const permission = {
  mounted(el, binding) {
    const req = Array.isArray(binding.value) ? binding.value : [binding.value].filter(Boolean)
    const disable = !!(binding.modifiers && binding.modifiers.disable)
    let roles = [], permissions = []
    try {
      const user = JSON.parse(localStorage.getItem('auth_user') || 'null')
      roles = user?.roles || []
      permissions = user?.permissions || []
    } catch {}
    const isAdmin = roles.includes('ADMIN')
    const ok = isAdmin || req.every(p => permissions.includes(p))
    if (!ok) {
      if (disable) {
        el.classList.add('is-disabled')
        el.setAttribute('disabled', 'disabled')
        el.style.pointerEvents = 'none'
        el.style.opacity = '0.6'
      } else {
        el.style.display = 'none'
      }
    } else {
      el.removeAttribute('disabled')
    }
  }
}

dayjs.locale('zh-cn')

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(ElementPlus, { locale: zhCn })
app.directive('permission', permission)
app.mount('#app')
