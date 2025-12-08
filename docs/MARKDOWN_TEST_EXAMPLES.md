# Markdown 测试样例

## 测试对话样例

### 样例 1：代码解释
**用户问：** 
```
请解释这段 Python 代码：

```python
def fibonacci(n):
    if n <= 1:
        return n
    return fibonacci(n-1) + fibonacci(n-2)
```
```

**AI 应该回复：**
```markdown
# Fibonacci 函数解释

这是一个 **递归实现** 的斐波那契数列函数。

## 代码分析

```python
def fibonacci(n):
    if n <= 1:
        return n
    return fibonacci(n-1) + fibonacci(n-2)
```

## 工作原理

1. **基本情况**：当 `n <= 1` 时，直接返回 `n`
2. **递归情况**：返回前两项之和 `fibonacci(n-1) + fibonacci(n-2)`

## 示例执行流程

计算 `fibonacci(5)` 的过程：

```
fibonacci(5)
├── fibonacci(4)
│   ├── fibonacci(3)
│   │   ├── fibonacci(2)
│   │   │   ├── fibonacci(1) → 1
│   │   │   └── fibonacci(0) → 0
│   │   └── fibonacci(1) → 1
│   └── fibonacci(2)
│       ├── fibonacci(1) → 1
│       └── fibonacci(0) → 0
└── fibonacci(3)
    ├── fibonacci(2)
    │   ├── fibonacci(1) → 1
    │   └── fibonacci(0) → 0
    └── fibonacci(1) → 1
```

结果：`5`

## 优缺点

| 方面 | 说明 |
|------|------|
| ✅ 优点 | 代码简洁，易于理解 |
| ❌ 缺点 | 时间复杂度 O(2^n)，效率低 |

> 💡 **优化建议**：使用动态规划或记忆化搜索提升性能
```

---

### 样例 2：技术比较
**用户问：**
```
对比 Vue 3 和 React 的主要区别
```

**AI 应该回复：**
```markdown
# Vue 3 vs React 对比

## 核心差异

### 1. 编程范式

**Vue 3：**
- 选项式 API（Options API）
- 组合式 API（Composition API）
- 模板语法（Template）

```vue
<template>
  <div>{{ count }}</div>
  <button @click="increment">+1</button>
</template>

<script setup>
import { ref } from 'vue'

const count = ref(0)
const increment = () => count.value++
</script>
```

**React：**
- 函数式组件 + Hooks
- JSX 语法

```jsx
import { useState } from 'react'

function Counter() {
  const [count, setCount] = useState(0)
  
  return (
    <div>
      <div>{count}</div>
      <button onClick={() => setCount(count + 1)}>+1</button>
    </div>
  )
}
```

### 2. 响应式系统

| 特性 | Vue 3 | React |
|------|-------|-------|
| 响应式原理 | Proxy 代理 | 不可变数据 + setState |
| 自动追踪依赖 | ✅ 是 | ❌ 否 |
| 性能 | 精确更新 | 虚拟 DOM Diff |

### 3. 学习曲线

```
Vue 3: 低 → 中
React: 中 → 高
```

> **Vue 3** 更容易上手，渐进式学习
> 
> **React** 需要掌握 JSX、Hooks、状态管理等概念

## 选择建议

### 选择 Vue 3 的场景
- 📱 中小型项目
- 🚀 快速原型开发
- 👥 团队前端经验较少
- 🎨 需要官方完整生态（Router、Vuex/Pinia）

### 选择 React 的场景
- 🏢 大型企业应用
- 📦 需要丰富的第三方库
- 👨‍💻 团队熟悉 JavaScript
- 🌍 跨平台开发（React Native）

## 总结

两者都是优秀的前端框架，选择取决于：
1. **项目需求**
2. **团队技术栈**
3. **开发效率**
4. **长期维护性**
```

---

### 样例 3：问题排查
**用户问：**
```
Spring Boot 启动时报错：
java.lang.IllegalArgumentException: Invalid value type for attribute 'factoryBeanObjectType': java.lang.String
```

**AI 应该回复：**
```markdown
# 问题诊断

## 错误原因

这是一个 **版本兼容性问题**，通常发生在：

> ⚠️ Spring Boot 3.2.0 与 MyBatis-Plus 3.5.5 之间存在兼容性冲突

## 解决方案

### 方案 1：降级 Spring Boot（推荐）

修改 `pom.xml`：

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.1.5</version>  <!-- 从 3.2.0 降级到 3.1.5 -->
</parent>
```

### 方案 2：升级 MyBatis-Plus

```xml
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>
    <version>3.5.7</version>  <!-- 升级到 3.5.7 -->
</dependency>
```

## 执行步骤

1. **修改配置文件**
   ```bash
   vim pom.xml
   ```

2. **清理缓存**
   ```bash
   mvn clean
   ```

3. **重新构建**
   ```bash
   mvn install -U -DskipTests
   ```

4. **重启应用**
   ```bash
   mvn spring-boot:run
   ```

## 验证方法

启动成功后，应该看到：

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v3.1.5)

✅ Application started successfully
```

## 相关资源

- [Spring Boot 版本兼容性](https://spring.io/projects/spring-boot#learn)
- [MyBatis-Plus 官方文档](https://baomidou.com/)

---

*问题解决了吗？如有其他疑问，随时提问！*
```

---

## 如何在平台测试

1. **启动项目**
   ```bash
   cd /home/user/webapp
   ./start.sh
   ```

2. **访问前端**
   ```
   http://localhost:3000
   ```

3. **登录账号**
   - 用户名：`admin`
   - 密码：`admin123`

4. **开始测试**
   - 进入「在线对话」
   - 选择一个 API 配置
   - 粘贴上述样例中的问题
   - 观察 Markdown 渲染效果

## 效果预览

- ✅ 代码块带语法高亮
- ✅ 表格整齐排列
- ✅ 列表层级清晰
- ✅ 引用样式美观
- ✅ 链接可点击
- ✅ 图片自适应

Happy testing! 🎉
