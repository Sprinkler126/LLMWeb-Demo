# 调试指南：导出功能 401 错误

## 🔍 当前状态

您的导出功能仍然返回 401 错误，我已经添加了详细的调试日志。

**当前 URL**: `http://localhost:8080/api/export/session/2/json?token=xxx`  
**错误**: 401 Unauthorized

---

## 🚀 立即调试步骤

### 步骤 1：更新代码

```bash
cd D:\JavaBank\LLMWeb-Demo
git pull origin main
```

应该看到：
```
From https://github.com/Sprinkler126/LLMWeb-Demo
   86de3db..2288ab2  main -> main
```

### 步骤 2：**必须重启后端**

**⚠️ 非常重要**：必须完全重启后端，配置才能生效！

**IDEA 方式**（推荐）:
1. 找到运行中的 `QnaPlatformApplication`
2. 点击红色停止按钮 ⏹️
3. 等待完全停止（控制台显示 "Disconnected"）
4. 点击绿色运行按钮 ▶️
5. 等待启动完成，看到 `Started QnaPlatformApplication`

**命令行方式**:
```bash
cd D:\JavaBank\LLMWeb-Demo\backend
# 如果之前用 mvn 启动，先按 Ctrl+C 停止
mvn clean spring-boot:run
```

### 步骤 3：测试拦截器配置

**在浏览器中访问**:
```
http://localhost:8080/api/export/test
```

**预期结果**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": "导出接口拦截器配置正常，token: 未提供"
}
```

**如果看到 401 错误** → 说明拦截器配置仍未生效，后端可能没有正确重启。

**如果看到上述 JSON** → 说明拦截器配置正确，问题在其他地方。

### 步骤 4：测试实际导出

**在浏览器中访问**:
```
http://localhost:8080/api/export/session/2/json?token=YOUR_TOKEN_HERE
```

替换 `YOUR_TOKEN_HERE` 为您的实际 token。

### 步骤 5：查看后端日志

**在 IDEA 控制台或命令行中查看日志**，应该看到：

```
🔍 导出接口被调用 - sessionId: 2
🔍 请求 URI: /session/2/json
🔍 Token 参数: 已提供
🔍 Authorization Header: null
✅ 用户验证成功 - userId: 1
```

**或者看到错误**：
```
❌ 导出失败: Token 无效或已过期
```

---

## 🐛 可能的问题和解决方案

### 问题 1：后端没有真正重启

**症状**: 访问 `/api/export/test` 仍然返回 401

**解决方案**:
1. 完全停止后端进程
2. 检查是否有多个 Java 进程在运行
3. 清理编译缓存：
   ```bash
   cd D:\JavaBank\LLMWeb-Demo\backend
   mvn clean
   ```
4. 重新启动：
   ```bash
   mvn spring-boot:run
   ```

### 问题 2：Token 格式错误

**症状**: 后端日志显示 "Token 无效或已过期"

**解决方案**:

**获取新的 token**：
1. 打开浏览器开发者工具（F12）
2. 切换到 Application 标签
3. 左侧选择 Local Storage > http://localhost:3000
4. 找到 `token` 键
5. 复制完整的 token 值（不要包含引号）

**示例**:
```
eyJhbGciOiJIUzM4NCJ9.eyJyb2xlIjoiU1VQRVJfQURNSU4iLCJ1c2VySWQiOjEsInVzZXJuYW1lIjoiYWRtaW4iLCJzdWIiOiJhZG1pbiIsImlhdCI6MTc2NTE4Njg3NSwiZXhwIjoxNzY1MjczMjc1fQ.2lHS8vhcYHMsLPJAK-eWtCF9UhqSL_oOX3nGL3lh0qHZjs9iCKH0uzxs2X4DBw_J
```

### 问题 3：Token 已过期

**症状**: Token 默认 24 小时过期

**解决方案**:
1. 退出登录
2. 重新登录
3. 获取新的 token
4. 再次测试导出

### 问题 4：拦截器仍在拦截

**症状**: 访问 `/api/export/test` 返回 401，并且**没有**后端日志输出

**原因**: 请求被拦截器拦截，没有到达 Controller

**解决方案**:

**检查拦截器配置是否生效**：

在 `PermissionInterceptor.java` 的 `preHandle` 方法开头添加日志：

```java
@Override
public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    String uri = request.getRequestURI();
    System.out.println("🛡️ 拦截器检查路径: " + uri);
    
    // 如果不是方法处理器，直接放行
    if (!(handler instanceof HandlerMethod)) {
        System.out.println("✅ 非方法处理器，直接放行");
        return true;
    }
    
    // ... 其他代码
}
```

重启后端，再次访问 `/api/export/test`，查看是否输出：
```
🛡️ 拦截器检查路径: /export/test
✅ 非方法处理器，直接放行
```

如果**没有输出**，说明拦截器配置已生效，路径被正确排除。

如果**有输出**，说明拦截器配置未生效，需要检查 `WebMvcConfig.java`。

---

## 📋 完整的检查清单

### 后端检查

- [ ] 代码已更新到最新版本（`git pull origin main`）
- [ ] 后端服务已完全重启（看到 "Started QnaPlatformApplication"）
- [ ] `WebMvcConfig.java` 包含 `excludePathPatterns("/export/**")`
- [ ] 访问 `/api/export/test` 返回 200（不是 401）
- [ ] 后端控制台有日志输出（🔍 导出接口被调用...）

### Token 检查

- [ ] Token 从 localStorage 正确获取
- [ ] Token 格式正确（3 段，用 . 分隔）
- [ ] Token 未过期（登录时间在 24 小时内）
- [ ] URL 中正确附加了 `?token=xxx` 参数

### 网络检查

- [ ] 后端运行在 8080 端口
- [ ] 前端运行在 3000 端口
- [ ] URL 使用 `http://localhost:8080/api/export/...`（不是 3000）
- [ ] 浏览器可以访问 `http://localhost:8080/api/export/test`

---

## 🔧 手动测试命令

### 使用 curl 测试

```bash
# 1. 测试拦截器配置
curl http://localhost:8080/api/export/test

# 预期输出：
# {"code":200,"message":"操作成功","data":"导出接口拦截器配置正常，token: 未提供"}

# 2. 测试导出接口
curl "http://localhost:8080/api/export/session/2/json?token=YOUR_TOKEN_HERE"

# 如果成功，应该返回 JSON 文件内容
# 如果失败，返回 401 错误信息
```

### 使用 Postman 测试

1. **测试拦截器配置**:
   - Method: GET
   - URL: `http://localhost:8080/api/export/test`
   - 点击 Send
   - 预期: 200 OK

2. **测试导出接口**:
   - Method: GET
   - URL: `http://localhost:8080/api/export/session/2/json`
   - Params: 
     - Key: `token`
     - Value: `YOUR_TOKEN_HERE`
   - 点击 Send
   - 预期: 200 OK，返回 JSON 数据

---

## 📊 日志分析指南

### 正常的日志输出

```
🔍 导出接口被调用 - sessionId: 2
🔍 请求 URI: /session/2/json
🔍 Token 参数: 已提供
🔍 Authorization Header: null
✅ 用户验证成功 - userId: 1
```

### Token 无效的日志

```
🔍 导出接口被调用 - sessionId: 2
🔍 请求 URI: /session/2/json
🔍 Token 参数: 已提供
🔍 Authorization Header: null
❌ 导出失败: Token 无效或已过期
java.lang.RuntimeException: Token 无效或已过期
    at com.qna.platform.controller.ExportController.getUserIdFromTokenOrRequest(...)
```

**解决**: 重新登录获取新 token

### 拦截器拦截的日志

**如果没有任何日志输出**，但返回 401，说明被拦截器拦截了。

**原因**: `WebMvcConfig` 配置未生效，或后端未重启。

---

## 💡 下一步操作

**请按照以下步骤操作并反馈结果**:

1. **更新代码**: `git pull origin main`
2. **重启后端**: 完全停止并重新启动
3. **测试**: 访问 `http://localhost:8080/api/export/test`
4. **查看日志**: 后端控制台的输出
5. **反馈**: 
   - `/api/export/test` 的响应
   - 后端日志内容
   - 实际导出 URL 的响应

---

**状态**: 🔍 **调试中**  
**最新提交**: `2288ab2`  
**需要**: 重启后端 + 查看日志

根据日志输出，我们可以准确定位问题！
