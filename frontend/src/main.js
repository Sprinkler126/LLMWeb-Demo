import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import { setupGlobalErrorHandler } from './router/errorHandler'

const app = createApp(App)
const pinia = createPinia()

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 设置全局错误处理器（防止快速路由切换错误）
setupGlobalErrorHandler(app)

app.use(pinia)
app.use(ElementPlus)
app.use(router)

app.mount('#app')
