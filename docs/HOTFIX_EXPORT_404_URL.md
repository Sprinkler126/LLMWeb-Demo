# 紧急修复：导出功能 404 错误 - URL 端口问题

## 🚨 问题描述

点击导出按钮时，浏览器打开的 URL 是：
```
http://localhost:3000/api/export/session/2/json?token=xxx
```

返回 **404 Not Found** 错误。

---

## 🔍 根本原因

### 问题分析

#### ❌ 错误的请求流程

```
用户点击导出
  ↓
window.open('/api/export/session/2/json?token=xxx')
  ↓
浏览器打开: http://localhost:3000/api/export/session/2/json
                          ^^^^^ 前端端口
  ↓
前端服务器 (Vite) 没有这个接口
  ↓
404 Not Found ❌
```

#### ✅ 正确的请求流程

```
用户点击导出
  ↓
window.open('http://localhost:8080/api/export/session/2/json?token=xxx')
  ↓
浏览器打开: http://localhost:8080/api/export/session/2/json
                          ^^^^^ 后端端口
  ↓
后端服务器 (Spring Boot) 处理请求
  ↓
下载文件 ✅
```

### 为什么会出现这个问题？

#### Vite 代理的工作原理

**正常的 API 请求** (使用 axios)：
```javascript
// 前端代码
axios.get('/api/chat/sessions')

// 实际请求流程
浏览器 → http://localhost:3000/api/chat/sessions (前端域名)
  ↓ Vite 代理拦截
  ↓ 转发到 http://localhost:8080/api/chat/sessions (后端域名)
  ↓ 返回数据
```

**window.open() 请求**：
```javascript
// 前端代码
window.open('/api/export/session/1/json?token=xxx')

// 实际请求流程
浏览器 → http://localhost:3000/api/export/session/1/json
  ↓ window.open() 直接打开新窗口
  ↓ 不经过 Vite 代理！
  ↓ 404 错误 ❌
```

**关键区别**：
- `axios` 请求：经过 Vite 代理 ✅
- `window.open()`：不经过 Vite 代理 ❌

---

## ✅ 修复方案

### 修改前端代码

**文件**: `frontend/src/views/Export.vue`

#### 修改前（错误）

```javascript
const exportSession = (sessionId, format) => {
  const token = userStore.token
  // ❌ 使用相对路径 /api
  const url = `${import.meta.env.VITE_API_BASE_URL || '/api'}/export/session/${sessionId}/${format}?token=${token}`
  window.open(url, '_blank')
}
```

生成的 URL：`/api/export/session/2/json?token=xxx`  
浏览器打开：`http://localhost:3000/api/export/session/2/json` ❌

#### 修改后（正确）

```javascript
const exportSession = (sessionId, format) => {
  const token = userStore.token
  // ✅ 使用完整的后端 URL
  const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
  const url = `${baseUrl}/export/session/${sessionId}/${format}?token=${token}`
  window.open(url, '_blank')
}
```

生成的 URL：`http://localhost:8080/api/export/session/2/json?token=xxx`  
浏览器打开：`http://localhost:8080/api/export/session/2/json` ✅

---

## 🚀 立即更新

### 1. 拉取最新代码

```bash
cd D:\JavaBank\LLMWeb-Demo
git pull origin main
```

### 2. 前端无需重启

如果前端开发服务器 (`npm run dev`) 正在运行：
- **无需重启**
- Vite 会自动热更新
- 刷新浏览器页面即可

如果已关闭，启动前端：
```bash
cd frontend
npm run dev
```

### 3. 验证修复

1. 打开浏览器 `http://localhost:3000`
2. 登录系统（admin / admin123）
3. 访问"数据导出"页面
4. 点击任意导出按钮
5. 查看浏览器地址栏，应该显示：
   ```
   http://localhost:8080/api/export/session/2/json?token=xxx
   ```
6. ✅ 文件成功下载

---

## 📊 开发环境 vs 生产环境

### 开发环境（当前）

**前端**: `http://localhost:3000` (Vite 开发服务器)  
**后端**: `http://localhost:8080` (Spring Boot)

**导出 URL**: `http://localhost:8080/api/export/...`

### 生产环境（部署后）

**统一域名**: `https://your-domain.com`

**Nginx 配置示例**：
```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    # 前端静态文件
    location / {
        root /var/www/frontend/dist;
        try_files $uri $uri/ /index.html;
    }
    
    # 后端 API
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

**环境变量配置**：

创建 `frontend/.env.production`：
```env
# 生产环境使用相对路径即可
VITE_API_BASE_URL=/api
```

**导出 URL**: `https://your-domain.com/api/export/...`

---

## 🐛 调试技巧

### 如何查看实际的导出 URL？

**方法 1：浏览器地址栏**

点击导出按钮后，查看新打开的标签页地址栏：
- ✅ 正确：`http://localhost:8080/api/export/...`
- ❌ 错误：`http://localhost:3000/api/export/...`

**方法 2：浏览器开发者工具**

1. 按 `F12` 打开开发者工具
2. 切换到 `Network` 标签
3. 点击导出按钮
4. 查看请求列表中的 URL

**方法 3：控制台日志**

在 `Export.vue` 中添加日志：
```javascript
const exportSession = (sessionId, format) => {
  const token = userStore.token
  const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
  const url = `${baseUrl}/export/session/${sessionId}/${format}?token=${token}`
  
  console.log('🔍 导出 URL:', url)  // 查看生成的 URL
  
  window.open(url, '_blank')
  ElMessage.success('开始导出...')
}
```

---

## 📝 常见问题

### Q1：为什么 axios 请求可以用 /api，window.open 不行？

**A**: 
- `axios` 是 JavaScript 发起的 HTTP 请求，会被 Vite 代理拦截
- `window.open()` 是浏览器直接打开 URL，不经过 Vite 代理
- 类似的还有：`<a href="/api/...">` 也不会经过代理

### Q2：生产环境还需要修改吗？

**A**: 不需要。生产环境配置 Nginx 反向代理后：
- 前端和后端在同一域名下
- 可以使用相对路径 `/api`
- 通过环境变量 `VITE_API_BASE_URL=/api` 配置

### Q3：如何支持不同的后端地址？

**A**: 使用环境变量。

创建 `frontend/.env.development`：
```env
# 开发环境
VITE_API_BASE_URL=http://localhost:8080/api
```

创建 `frontend/.env.production`：
```env
# 生产环境
VITE_API_BASE_URL=/api
```

### Q4：还有哪些地方需要注意？

**A**: 所有使用 `window.open()`, `<a href>`, `<form action>` 的地方都需要使用完整 URL。

例如：
```javascript
// ❌ 错误
window.location.href = '/api/download/file.pdf'

// ✅ 正确
window.location.href = 'http://localhost:8080/api/download/file.pdf'
```

---

## 🔧 相关配置文件

### vite.config.js

```javascript
export default defineConfig({
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

**说明**：
- 这个配置只对 `axios` 等 JavaScript 请求有效
- 不影响 `window.open()` 等浏览器直接导航

---

## 📚 相关文档

- Vite 代理文档：https://vitejs.dev/config/server-options.html#server-proxy
- MDN window.open：https://developer.mozilla.org/en-US/docs/Web/API/Window/open

---

## 📝 相关提交

- **修复提交**: `ad236d5` - fix: 修复导出功能使用错误的URL导致404

---

## ✅ 验证清单

- [x] 拉取最新代码
- [x] 前端页面刷新（自动热更新）
- [x] 点击导出按钮
- [x] 查看地址栏 URL 为 `http://localhost:8080/api/...`
- [x] 文件成功下载
- [x] 测试所有导出格式（JSON/CSV/Excel）

---

**状态**: ✅ **已修复**  
**修复时间**: 2025-12-08  
**最新提交**: `ad236d5`  
**GitHub**: https://github.com/Sprinkler126/LLMWeb-Demo

**只需拉取代码并刷新浏览器，无需重启服务！** 🎉
