# 数据导出功能 401 错误修复

## 🐛 问题描述

用户点击数据导出功能时，出现 **401 Unauthorized** 错误，无法下载导出文件。

### 影响功能

- ❌ 导出会话消息（JSON/CSV/Excel）
- ❌ 导出所有对话记录

---

## 🔍 根本原因

### 问题分析

1. **前端使用 `window.open()` 下载**：
   - 导出功能使用 `window.open(url)` 打开新窗口下载文件
   - 这种方式**无法携带 HTTP Header**（如 `Authorization: Bearer xxx`）
   - 只能通过 URL 参数传递 token

2. **后端拦截器配置问题**：
   - `PermissionInterceptor` 拦截所有 `/**` 请求
   - `/api/export/**` 路径**没有被排除**
   - 导出请求被拦截，返回 401 错误

3. **token 验证流程**：
   ```
   前端 window.open(url?token=xxx)
     ↓
   后端拦截器检查 Authorization Header
     ↓
   Header 为空 → 返回 401 ❌
   ```

### 预期流程

```
前端 window.open(url?token=xxx)
  ↓
后端跳过拦截器（/api/export/** 被排除）
  ↓
ExportController 从 URL token 参数获取用户ID
  ↓
验证 token → 生成文件 → 下载 ✅
```

---

## ✅ 修复方案

### 修复内容

**文件**: `backend/src/main/java/com/qna/platform/config/WebMvcConfig.java`

**修改前**:
```java
@Override
public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(permissionInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns(
                    "/api/auth/**",           // 认证接口
                    "/error",                 // 错误页面
                    "/swagger-ui/**",         // Swagger UI
                    "/v3/api-docs/**"        // API 文档
            );
}
```

**修改后**:
```java
@Override
public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(permissionInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns(
                    "/api/auth/**",           // 认证接口
                    "/api/export/**",         // 导出接口（通过 URL token 验证）⭐ 新增
                    "/error",                 // 错误页面
                    "/swagger-ui/**",         // Swagger UI
                    "/v3/api-docs/**"        // API 文档
            );
}
```

### 工作原理

#### 1. 前端发起导出请求

**文件**: `frontend/src/views/Export.vue`

```javascript
const exportSession = (sessionId, format) => {
  // 从 store 获取 token
  const token = userStore.token
  
  // 拼接 URL，将 token 作为查询参数
  const url = `${import.meta.env.VITE_API_BASE_URL || '/api'}/export/session/${sessionId}/${format}?token=${token}`
  
  // 打开新窗口下载
  window.open(url, '_blank')
  ElMessage.success('开始导出...')
}

const exportAll = () => {
  const token = userStore.token
  const url = `${import.meta.env.VITE_API_BASE_URL || '/api'}/export/all/${exportFormat.value}?token=${token}`
  window.open(url, '_blank')
  ElMessage.success('开始导出...')
}
```

#### 2. 后端处理导出请求

**文件**: `backend/src/main/java/com/qna/platform/controller/ExportController.java`

```java
@GetMapping("/session/{sessionId}/json")
public void exportSessionJson(
        @PathVariable Long sessionId,
        @RequestParam(required = false) String token,  // 接收 URL 参数中的 token
        HttpServletRequest request,
        HttpServletResponse response) {
    // 从 token 或 request 获取用户ID
    Long userId = getUserIdFromTokenOrRequest(token, request);
    exportService.exportSessionToJson(sessionId, userId, response);
}

/**
 * 灵活的用户ID获取策略
 */
private Long getUserIdFromTokenOrRequest(String token, HttpServletRequest request) {
    // 1. 优先从 request attribute 获取（正常 API 请求）
    Long userId = (Long) request.getAttribute("userId");
    if (userId != null) {
        return userId;
    }
    
    // 2. 从 URL token 参数获取（导出下载请求）
    if (token != null && !token.isEmpty()) {
        try {
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            throw new RuntimeException("Token 无效或已过期");
        }
    }
    
    throw new RuntimeException("未授权访问");
}
```

#### 3. JwtUtil 解析 token

**文件**: `backend/src/main/java/com/qna/platform/util/JwtUtil.java`

```java
/**
 * 从 Token 中获取用户ID
 */
public Long getUserIdFromToken(String token) {
    Claims claims = getClaimsFromToken(token);
    return claims.get("userId", Long.class);
}

/**
 * 从 Token 中获取 Claims
 */
public Claims getClaimsFromToken(String token) {
    return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
}
```

---

## 🚀 更新步骤

### 1. 拉取最新代码

```bash
cd D:\JavaBank\LLMWeb-Demo
git pull origin main
```

### 2. 重启后端服务

在 IDEA 中：
1. 停止运行中的 Spring Boot 应用
2. 重新运行 `QnaPlatformApplication`

或使用命令行：
```bash
cd backend
mvn spring-boot:run
```

### 3. 前端无需修改

前端代码之前已经修复，无需重启。

---

## ✅ 验证修复效果

### 测试步骤

1. **登录系统**:
   ```
   用户名：admin
   密码：admin123
   ```

2. **访问数据导出页面**:
   - 点击左侧菜单"数据导出"

3. **测试导出所有对话**:
   - 选择导出格式（JSON/CSV/Excel）
   - 点击"导出所有对话记录"按钮
   - ✅ 应该弹出下载对话框
   - ✅ 文件成功下载

4. **测试导出单个会话**:
   - 在会话列表中选择任意会话
   - 点击对应的 JSON/CSV/Excel 按钮
   - ✅ 应该弹出下载对话框
   - ✅ 文件成功下载

5. **检查文件内容**:
   - 打开下载的文件
   - ✅ JSON 格式正确，包含消息内容
   - ✅ CSV 格式正确，可以用 Excel 打开
   - ✅ Excel 格式正确，包含完整数据

---

## 📊 导出格式说明

### JSON 格式

```json
{
  "sessionId": 1,
  "sessionTitle": "测试会话",
  "messages": [
    {
      "id": 1,
      "role": "user",
      "content": "你好",
      "timestamp": "2025-12-08T10:30:00"
    },
    {
      "id": 2,
      "role": "assistant",
      "content": "你好！有什么可以帮助你的吗？",
      "timestamp": "2025-12-08T10:30:05"
    }
  ]
}
```

### CSV 格式

```csv
消息ID,角色,内容,时间戳
1,user,你好,2025-12-08T10:30:00
2,assistant,你好！有什么可以帮助你的吗？,2025-12-08T10:30:05
```

### Excel 格式

| 消息ID | 角色 | 内容 | 时间戳 |
|--------|------|------|--------|
| 1 | user | 你好 | 2025-12-08T10:30:00 |
| 2 | assistant | 你好！有什么可以帮助你的吗？ | 2025-12-08T10:30:05 |

---

## 🔍 控制台日志

### 前端日志（浏览器 Console）

```
开始导出...
```

### 后端日志

```
[INFO] 导出会话 1 为 JSON 格式，用户ID: 1
[INFO] 会话消息查询完成，共 10 条消息
[INFO] JSON 文件生成成功
```

---

## 🐛 常见问题排查

### 问题 1：仍然显示 401 错误

**可能原因**:
1. 后端代码未更新
2. 后端服务未重启

**解决方案**:
```bash
# 1. 确认代码已更新
cd D:\JavaBank\LLMWeb-Demo
git log --oneline -1
# 应显示: 0e34a5b fix: 修复数据导出功能401错误

# 2. 重启后端服务
# 在 IDEA 中停止并重新运行，或使用 mvn spring-boot:run
```

### 问题 2：Token 无效或已过期

**可能原因**:
- Token 已过期（默认有效期 24 小时）

**解决方案**:
1. 重新登录获取新 token
2. 再次尝试导出

### 问题 3：下载的文件为空

**可能原因**:
- 会话没有消息
- 数据库连接问题

**解决方案**:
1. 先在对话页面发送几条消息
2. 再尝试导出
3. 检查后端日志是否有错误

### 问题 4：CSV 乱码问题

**可能原因**:
- 文件编码不匹配

**解决方案**:
1. 使用记事本打开 CSV 文件
2. 另存为，选择编码 "UTF-8"
3. 再用 Excel 打开

---

## 📝 API 接口文档

### 导出单个会话

**接口**: `GET /api/export/session/{sessionId}/{format}`

**参数**:
- `sessionId`: 会话ID（路径参数）
- `format`: 导出格式（路径参数），可选值：`json`、`csv`、`excel`
- `token`: JWT Token（查询参数）

**示例**:
```
GET /api/export/session/1/json?token=eyJhbGciOiJIUzI1NiJ9...
```

**响应**:
- 成功：文件下载
- 失败：HTTP 状态码 401、403 或 500

### 导出所有消息

**接口**: `GET /api/export/all/{format}`

**参数**:
- `format`: 导出格式（路径参数），可选值：`json`、`csv`、`excel`
- `token`: JWT Token（查询参数）

**示例**:
```
GET /api/export/all/excel?token=eyJhbGciOiJIUzI1NiJ9...
```

**响应**:
- 成功：文件下载
- 失败：HTTP 状态码 401、403 或 500

---

## 🔒 安全说明

### Token 安全

1. **HTTPS 部署**:
   - 生产环境必须使用 HTTPS
   - 防止 token 在传输过程中被窃取

2. **Token 有效期**:
   - 默认 24 小时
   - 过期后需要重新登录

3. **Token 刷新**:
   - 建议实现 token 刷新机制
   - 避免频繁要求用户重新登录

### 权限控制

导出接口虽然不经过权限拦截器，但仍然：
1. 验证 token 有效性
2. 只能导出当前用户自己的数据
3. 防止跨用户数据访问

---

## 📚 相关文档

- **快速修复指南**: `docs/QUICK_FIX_GUIDE.md`
- **权限系统文档**: `docs/BUG_FIX_PERMISSIONS.md`

---

## 📝 相关提交

- **修复提交**: `0e34a5b` - fix: 修复数据导出功能401错误

---

**状态**: ✅ **已修复**  
**修复时间**: 2025-12-08  
**GitHub**: https://github.com/Sprinkler126/LLMWeb-Demo

立即更新代码，导出功能恢复正常！🎉
