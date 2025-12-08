# Markdown 使用指南

在线问答平台支持完整的 Markdown 格式，让对话内容更加丰富和美观。

## 🎨 支持的 Markdown 语法

### 1. 标题

```markdown
# 一级标题
## 二级标题
### 三级标题
```

### 2. 文本样式

```markdown
**粗体文本**
*斜体文本*
~~删除线~~
`行内代码`
```

### 3. 列表

**无序列表：**
```markdown
- 项目 1
- 项目 2
  - 子项目 2.1
  - 子项目 2.2
```

**有序列表：**
```markdown
1. 第一项
2. 第二项
3. 第三项
```

### 4. 代码块

**单行代码：**
```markdown
使用 `console.log()` 输出信息
```

**多行代码块：**
````markdown
```python
def hello_world():
    print("Hello, World!")
```
````

**支持的语言高亮：**
- Python
- JavaScript
- Java
- C/C++
- SQL
- Shell/Bash
- HTML/CSS
- JSON
- Markdown
- 等更多...

### 5. 引用

```markdown
> 这是一段引用文本
> 可以包含多行
```

### 6. 链接

```markdown
[链接文本](https://example.com)
```

### 7. 图片

```markdown
![图片描述](图片URL)
```

### 8. 表格

```markdown
| 列1 | 列2 | 列3 |
|-----|-----|-----|
| 数据1 | 数据2 | 数据3 |
| 数据4 | 数据5 | 数据6 |
```

### 9. 分隔线

```markdown
---
```

## 💡 使用示例

### 示例 1：技术讨论

**用户输入：**
```markdown
请解释一下 Python 的列表推导式，并给出示例代码。
```

**AI 回复：**
```markdown
# Python 列表推导式

列表推导式是 Python 中创建列表的简洁方式。

## 基本语法

```python
[expression for item in iterable if condition]
```

## 示例代码

```python
# 生成平方数列表
squares = [x**2 for x in range(10)]
print(squares)  # [0, 1, 4, 9, 16, 25, 36, 49, 64, 81]

# 过滤偶数
evens = [x for x in range(20) if x % 2 == 0]
print(evens)  # [0, 2, 4, 6, 8, 10, 12, 14, 16, 18]
```

## 优点

- **简洁**：一行代码完成多行循环
- **高效**：比传统循环更快
- **可读性强**：符合 Pythonic 风格
```

### 示例 2：数据分析

**用户输入：**
```markdown
比较一下以下三种数据库的特点：

| 数据库 | 类型 | 优点 |
|--------|------|------|
| MySQL | 关系型 | 成熟稳定 |
| MongoDB | NoSQL | 灵活扩展 |
| Redis | 键值存储 | 高性能 |
```

### 示例 3：项目规划

**用户输入：**
```markdown
帮我制定一个学习计划：

## 学习目标
- [ ] 掌握 Vue 3 基础
- [ ] 学习 Spring Boot
- [ ] 完成项目实战

## 时间安排
1. **第一周**：Vue 3 核心概念
2. **第二周**：Spring Boot 入门
3. **第三周**：项目实战开发

> 注意：每天学习时间不少于2小时
```

## 🎯 最佳实践

### 1. 代码分享
使用代码块分享代码时，记得指定语言类型以获得语法高亮：

````markdown
```javascript
function greet(name) {
  console.log(`Hello, ${name}!`);
}
```
````

### 2. 结构化输出
使用标题和列表让回答更有条理：

```markdown
# 主题

## 要点一
- 详细说明1
- 详细说明2

## 要点二
- 详细说明1
- 详细说明2
```

### 3. 突出重点
使用 **粗体** 和 `代码标记` 突出关键信息：

```markdown
在配置文件中设置 **BASE_URL** 为 `https://api.example.com`
```

### 4. 添加引用
引用重要提示或警告：

```markdown
> ⚠️ **警告**：修改配置前请先备份！
> 
> 💡 **提示**：建议使用环境变量存储敏感信息
```

## 🚀 高级技巧

### 混合使用多种格式

```markdown
# API 接入指南

## 步骤 1：获取 API Key

访问 [控制台](https://console.example.com) 获取密钥。

## 步骤 2：配置客户端

```python
import requests

api_key = "your-api-key"
url = "https://api.example.com/v1/chat"

response = requests.post(url, headers={
    "Authorization": f"Bearer {api_key}"
})
```

## 注意事项

> - 妥善保管 API Key
> - 遵守调用频率限制
> - 查看官方文档获取更多信息

## 费用说明

| 模型 | 输入价格 | 输出价格 |
|------|---------|----------|
| GPT-4 | $0.03/1K tokens | $0.06/1K tokens |
| GPT-3.5 | $0.0015/1K tokens | $0.002/1K tokens |
```

## 📚 参考资源

- [Markdown 官方教程](https://www.markdownguide.org/)
- [GitHub Flavored Markdown](https://github.github.com/gfm/)
- [Highlight.js 支持的语言](https://github.com/highlightjs/highlight.js/blob/main/SUPPORTED_LANGUAGES.md)

## 💬 平台特性

本平台的 Markdown 渲染特性：

✅ **支持特性**
- GitHub Flavored Markdown (GFM)
- 代码语法高亮（190+ 语言）
- 表格、任务列表
- 自动链接识别
- HTML 标签（部分）

🎨 **样式优化**
- 深色代码主题
- 响应式表格
- 美观的引用样式
- 图片自适应

⚡ **性能优化**
- 实时渲染
- 安全的 HTML 过滤
- 优化的滚动性能

---

享受 Markdown 带来的更好对话体验吧！🎉
