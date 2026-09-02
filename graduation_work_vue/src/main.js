import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import 'element-plus/dist/index.css'
import { createPinia } from 'pinia'
import api from '@/api'

const storedRole = localStorage.getItem('role')
if (storedRole) {
  localStorage.setItem('role', storedRole.toLowerCase())
}

const app = createApp(App)
app.use(ElementPlus, {
  locale: zhCn,
})
app.use(createPinia())
app.config.globalProperties.$API = api
app.use(router)
app.mount('#app')
