# API配置指南

## 📋 支持的配置模式

本平台支持两种API配置模式，适配不同的大模型服务提供商。

---

## 模式1：标准模式（模型名称在请求体中）

### 适用场景
- OpenAI官方API
- Anthropic Claude API
- 大多数标准化的大模型API

### 配置方式

**API端点**: 固定的URL，不包含模型名称
```
https://api.openai.com/v1/chat/completions
https://api.anthropic.com/v1/messages
```

**模型名称**: 单独配置，将作为请求体参数发送
```
gpt-4
gpt-3.5-turbo
claude-3-opus
```

### 示例配置

```json
{
  "config_name": "OpenAI GPT-4",
  "provider": "OpenAI",
  "model_name": "gpt-4",
  "api_endpoint": "https://api.openai.com/v1/chat/completions",
  "api_key": "sk-xxx",
  "api_type": "ONLINE",
  "max_tokens": 2000,
  "temperature": 0.7
}
```

### 实际请求示例

**请求URL**:
```
POST https://api.openai.com/v1/chat/completions
```

**请求体**:
```json
{
  "model": "gpt-4",
  "messages": [...],
  "max_tokens": 2000,
  "temperature": 0.7
}
```

---

## 模式2：URL路径模式（模型名称在URL中）

### 适用场景
- 企业统一网关
- 代理服务
- 自定义路由服务
- 很多公司的标准做法

### 配置方式

**API端点**: 使用 `{model}` 或 `{modelName}` 作为占位符
```
https://api.company.com/v1/chat/{model}
https://gateway.example.com/llm/{model}/chat
```

**模型名称**: 将替换URL中的占位符
```
gpt-4
claude-3
qwen-turbo
```

### 示例配置

```json
{
  "config_name": "企业网关-GPT4",
  "provider": "OpenAI",
  "model_name": "gpt-4",
  "api_endpoint": "https://api.company.com/v1/chat/{model}",
  "api_key": "your-api-key",
  "api_type": "ONLINE",
  "max_tokens": 2000,
  "temperature": 0.7
}
```

### 实际请求示例

**请求URL** (占位符被替换):
```
POST https://api.company.com/v1/chat/gpt-4
```

**请求体** (不再包含model参数):
```json
{
  "messages": [...],
  "max_tokens": 2000,
  "temperature": 0.7
}
```

---

## 🔧 配置详解

### 必填字段

| 字段 | 说明 | 示例 |
|------|------|------|
| config_name | 配置名称 | "OpenAI GPT-4" |
| provider | 提供商 | OpenAI, Anthropic, Google, Aliyun, Baidu, Local |
| model_name | 模型名称 | gpt-4, claude-3-opus |
| api_endpoint | API端点 | https://api.openai.com/v1/chat/completions |
| api_type | API类型 | ONLINE（在线）, LOCAL（本地） |

### 可选字段

| 字段 | 说明 | 默认值 |
|------|------|--------|
| api_key | API密钥 | - |
| max_tokens | 最大token数 | 2000 |
| temperature | 温度参数 | 0.7 |
| timeout | 超时时间(秒) | 30 |

---

## 📝 常见配置示例

### 1. OpenAI官方（标准模式）

```
配置名称: OpenAI GPT-4
提供商: OpenAI
模型名称: gpt-4
API端点: https://api.openai.com/v1/chat/completions
API密钥: sk-xxxxxxxxxxxxxxxxxxxxxxxx
```

### 2. 企业统一网关（URL路径模式）

```
配置名称: 企业网关-GPT4
提供商: OpenAI
模型名称: gpt-4
API端点: https://api.company.com/v1/chat/{model}
API密钥: company-api-key-xxx
```

### 3. 阿里云通义千问（URL路径模式）

```
配置名称: 阿里云通义千问
提供商: Aliyun
模型名称: qwen-turbo
API端点: https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/{model}
API密钥: sk-xxxxxxxx
```

### 4. 本地部署模型（标准模式）

```
配置名称: 本地Llama2
提供商: Local
模型名称: llama-2-7b-chat
API端点: http://localhost:8080/v1/chat/completions
API密钥: (留空)
API类型: LOCAL
```

### 5. 本地部署模型（URL路径模式）

```
配置名称: 本地模型网关
提供商: Local
模型名称: llama-2-7b
API端点: http://localhost:8080/v1/models/{model}/chat
API密钥: (留空)
API类型: LOCAL
```

---

## 🎯 占位符说明

系统支持以下占位符：

| 占位符 | 说明 | 示例 |
|--------|------|------|
| `{model}` | 模型名称 | https://api.example.com/v1/{model} |
| `{modelName}` | 模型名称（别名） | https://api.example.com/chat/{modelName} |

**使用规则**:
- 占位符会被替换为实际的模型名称
- 如果URL中包含占位符，模型名称不会出现在请求体中
- 支持在URL的任意位置使用占位符

---

## 💡 如何选择配置模式

### 使用标准模式（模型名在请求体）的情况：
✅ 使用OpenAI官方API  
✅ 使用Anthropic官方API  
✅ 使用支持OpenAI格式的第三方服务  
✅ 本地部署的标准兼容服务  

### 使用URL路径模式（模型名在URL）的情况：
✅ 企业统一API网关  
✅ 自建代理服务  
✅ 第三方API聚合平台  
✅ 需要模型名称路由的服务  

---

## 🔍 测试配置

添加配置后，可以使用"测试"按钮验证配置是否正确：

1. 在API配置管理页面点击"测试"按钮
2. 系统会发送测试消息："Hi, this is a test message."
3. 如果配置正确，会返回"测试成功"
4. 如果失败，会显示具体的错误信息

---

## ❗ 常见问题

### Q1: 如何知道应该使用哪种模式？

**A**: 查看API提供商的文档：
- 如果文档中请求示例的URL是固定的（如 `/v1/chat/completions`），使用标准模式
- 如果文档中URL包含模型名称（如 `/v1/chat/gpt-4`），使用URL路径模式

### Q2: 可以同时配置多个相同提供商的不同模型吗？

**A**: 可以！每个配置都是独立的，你可以为同一提供商配置多个模型：
```
- OpenAI GPT-4
- OpenAI GPT-3.5-turbo
- OpenAI GPT-4-turbo
```

### Q3: URL路径模式的占位符必须是 `{model}` 吗？

**A**: 支持 `{model}` 和 `{modelName}` 两种写法，系统会自动识别并替换。

### Q4: 如果我的企业网关既需要URL中的模型名，又需要请求体中的model参数怎么办？

**A**: 这种情况比较特殊，建议：
1. 不使用占位符，直接在URL中写死模型名称
2. 联系技术支持进行定制开发

---

## 📞 技术支持

如有其他配置问题，请：
1. 查看提供商的API文档
2. 使用测试功能验证配置
3. 查看后端日志获取详细错误信息
4. 提交Issue到GitHub仓库

---

**最后更新**: 2024-12-08
