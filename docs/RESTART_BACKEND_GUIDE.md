# 后端重启完整指南

## 🚨 当前问题

`/api/export/test` 返回 401，说明拦截器配置**没有生效**。

**根本原因**：后端没有真正重启，或者 IDEA/Maven 使用了缓存。

---

## 🔧 方法 1：IDEA 完全重启（推荐）

### 步骤 1：停止现有进程

1. 在 IDEA 底部的 `Run` 标签页中
2. 找到正在运行的 `QnaPlatformApplication`
3. 点击红色停止按钮 ⏹️
4. **等待完全停止**（看到 "Process finished" 或 "Disconnected"）

### 步骤 2：清理编译缓存

在 IDEA 菜单栏：
1. **File** → **Invalidate Caches...**
2. 勾选所有选项
3. 点击 **Invalidate and Restart**

**或者使用 Maven 清理**：
```bash
cd D:\JavaBank\LLMWeb-Demo\backend
mvn clean
```

### 步骤 3：重新编译

在 IDEA 菜单栏：
1. **Build** → **Rebuild Project**
2. 等待编译完成

### 步骤 4：启动应用

1. 找到 `QnaPlatformApplication.java`
2. 右键 → **Run 'QnaPlatformApplication'**
3. 或点击绿色三角 ▶️

### 步骤 5：确认启动成功

**在 IDEA 控制台查看日志**，必须看到：

```
=================================================
🔧 配置拦截器 - 排除路径：
   - /auth/**
   - /export/**
   - /error
   - /swagger-ui/**
   - /v3/api-docs/**
=================================================

...

2025-12-08 ... INFO ... : Started QnaPlatformApplication in X.XXX seconds
```

**如果没看到 🔧 emoji 日志** → 说明代码没有更新或配置未加载！

---

## 🔧 方法 2：命令行重启（彻底）

### 步骤 1：更新代码

```bash
cd D:\JavaBank\LLMWeb-Demo
git pull origin main
```

确认看到：
```
Already up to date.
或
Updating xxx..xxx
```

### 步骤 2：停止所有 Java 进程

**在 Windows PowerShell 中**：

```powershell
# 查找所有 Java 进程
Get-Process java

# 如果有多个 Java 进程，停止它们
Stop-Process -Name java -Force
```

**或者在 CMD 中**：

```cmd
# 查找 Java 进程
tasklist | findstr java

# 记下 PID，然后停止
taskkill /PID <PID号> /F
```

### 步骤 3：清理并重新编译

```bash
cd D:\JavaBank\LLMWeb-Demo\backend
mvn clean
mvn compile
```

### 步骤 4：启动应用

```bash
mvn spring-boot:run
```

### 步骤 5：确认启动成功

**在命令行输出中查看**：

```
=================================================
🔧 配置拦截器 - 排除路径：
   - /auth/**
   - /export/**
   ...
=================================================

...

Started QnaPlatformApplication in X.XXX seconds
```

---

## ✅ 验证步骤

### 1. 检查启动日志

**必须看到**：
```
=================================================
🔧 配置拦截器 - 排除路径：
   - /auth/**
   - /export/**
   - /error
   - /swagger-ui/**
   - /v3/api-docs/**
=================================================
```

**如果没有** → 代码未更新，执行 `git pull origin main` 并重启

### 2. 测试拦截器配置

**在浏览器访问**：
```
http://localhost:8080/api/export/test
```

**查看后端控制台**，应该看到：
```
🛡️ 拦截器检查路径: /export/test
   ✅ 非方法处理器，直接放行
```

**预期响应**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": "导出接口拦截器配置正常，token: 未提供"
}
```

**如果仍是 401**：
- 检查是否看到 🛡️ 日志
- 如果**看到日志** → 拦截器仍在拦截，配置有问题
- 如果**没看到日志** → 请求没到后端，检查端口和 URL

### 3. 测试实际导出

1. 重新登录获取新 token（`http://localhost:3000`）
2. 访问：
   ```
   http://localhost:8080/api/export/session/2/json?token=YOUR_TOKEN
   ```
3. 查看后端控制台日志：
   ```
   🛡️ 拦截器检查路径: /export/session/2/json
      ✅ 非方法处理器，直接放行
   🔍 导出接口被调用 - sessionId: 2
   🔍 请求 URI: /session/2/json
   🔍 Token 参数: 已提供
   ✅ 用户验证成功 - userId: 1
   ```

---

## 🐛 常见问题

### 问题 1：启动时没有看到 🔧 日志

**原因**：代码未更新或 Git 有冲突

**解决**：
```bash
cd D:\JavaBank\LLMWeb-Demo

# 检查当前版本
git log --oneline -1
# 应该显示: b6feea5 debug: 添加拦截器配置和执行详细日志

# 如果不是，强制更新
git fetch origin
git reset --hard origin/main

# 重新启动后端
```

### 问题 2：启动失败，报错 "Port 8080 already in use"

**原因**：之前的进程没有完全停止

**解决**：

**Windows PowerShell**：
```powershell
# 查找占用 8080 端口的进程
netstat -ano | findstr :8080

# 停止该进程（替换 <PID> 为实际 PID）
Stop-Process -Id <PID> -Force
```

**CMD**：
```cmd
# 查找占用 8080 端口的进程
netstat -ano | findstr :8080

# 停止该进程（替换 <PID> 为实际 PID）
taskkill /PID <PID> /F
```

### 问题 3：mvn 命令不存在

**原因**：Maven 未安装或未配置环境变量

**解决**：

**在 IDEA 中使用内置 Maven**：
1. 打开 IDEA 右侧的 Maven 面板
2. 展开 `qna-platform` → `Lifecycle`
3. 双击 `clean`
4. 双击 `install`
5. 右键 `QnaPlatformApplication` → Run

### 问题 4：IDEA 没有找到 Main 类

**原因**：项目配置损坏

**解决**：
1. **File** → **Project Structure**
2. **Modules** → 选择 `backend`
3. **Sources** 标签 → 确认 `src/main/java` 标记为 Sources（蓝色）
4. 点击 **OK**
5. **File** → **Invalidate Caches...** → **Invalidate and Restart**

---

## 📋 完整的重启检查清单

- [ ] 代码已更新（`git pull origin main`）
- [ ] 确认最新提交（`git log --oneline -1` 显示 `b6feea5`）
- [ ] 所有 Java 进程已停止
- [ ] Maven 已清理（`mvn clean`）
- [ ] 后端已重新启动
- [ ] 看到启动日志中的 🔧 emoji
- [ ] 看到 "Started QnaPlatformApplication"
- [ ] `/api/export/test` 返回 200
- [ ] 看到拦截器日志 🛡️

---

## 📞 如仍有问题，请提供

1. **Git 版本**：
   ```bash
   git log --oneline -1
   ```

2. **启动日志**：
   - 从启动开始到 "Started" 的完整日志
   - 特别是是否有 🔧 日志

3. **请求日志**：
   - 访问 `/api/export/test` 时的完整日志
   - 是否有 🛡️ 日志

4. **错误信息**：
   - 浏览器控制台的完整错误
   - 后端控制台的完整错误

---

**最新提交**: `b6feea5`  
**GitHub**: https://github.com/Sprinkler126/LLMWeb-Demo

**关键**：必须看到启动日志中的 🔧 emoji！
