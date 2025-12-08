# DeepSeek API 配置指南

## 📋 概述

DeepSeek是由深度求索（DeepSeek）公司开发的大语言模型，提供与OpenAI兼容的API接口。本平台已完全支持DeepSeek API。

---

## 🌟 DeepSeek特点

1. **兼容OpenAI格式** - 可以直接使用OpenAI SDK
2. **高性价比** - 价格优惠，性能优秀
3. **多种模型** - 支持对话、推理、代码等场景
4. **中英文优化** - 对中文支持优秀

---

## 🔑 获取API密钥

1. 访问 [DeepSeek平台](https://platform.deepseek.com/)
2. 注册/登录账号
3. 进入 [API密钥页面](https://platform.deepseek.com/api_keys)
4. 点击"创建新密钥"
5. 复制并保存API密钥（格式：`sk-xxx...`）

---

## 📦 可用模型

### 1. DeepSeek Chat (deepseek-chat)
- **描述**: DeepSeek-V3.2 非思考模式
- **用途**: 通用对话、文本生成
- **特点**: 响应快速，适合常规对话
- **上下文**: 最大64K tokens
- **推荐场景**: 日常对话、问答、文本生成

### 2. DeepSeek Reasoner (deepseek-reasoner)
- **描述**: DeepSeek-V3.2 思考模式
- **用途**: 复杂推理、数学、逻辑问题
- **特点**: 展示思考过程，推理能力强
- **上下文**: 最大64K tokens
- **推荐场景**: 数学题、逻辑推理、复杂问题分析

### 3. DeepSeek Coder (deepseek-coder)
- **描述**: 代码专用模型
- **用途**: 代码生成、代码理解、代码补全
- **特点**: 对编程语言优化
- **上下文**: 最大16K tokens
- **推荐场景**: 编程助手、代码生成、代码审查

---

## ⚙️ 配置步骤

### 在平台中配置

1. **登录平台**
2. **进入"API配置管理"**
3. **点击"添加配置"**
4. **填写配置信息**

---

## 📝 配置示例

### 配置1: DeepSeek Chat (推荐)

```
配置名称: DeepSeek对话模型
提供商: DeepSeek
模型名称: deepseek-chat
API端点: https://api.deepseek.com/chat/completions
API密钥: sk-xxxxxxxxxxxxxxxxxxxxxxxx
API类型: ONLINE
最大Token数: 4096
温度: 0.7
```

### 配置2: DeepSeek Reasoner (推理模式)

```
配置名称: DeepSeek推理模型
提供商: DeepSeek
模型名称: deepseek-reasoner
API端点: https://api.deepseek.com/chat/completions
API密钥: sk-xxxxxxxxxxxxxxxxxxxxxxxx
API类型: ONLINE
最大Token数: 4096
温度: 0.7
```

### 配置3: DeepSeek Coder (代码专用)

```
配置名称: DeepSeek代码助手
提供商: DeepSeek
模型名称: deepseek-coder
API端点: https://api.deepseek.com/chat/completions
API密钥: sk-xxxxxxxxxxxxxxxxxxxxxxxx
API类型: ONLINE
最大Token数: 4096
温度: 0.3
```

---

## 🔗 API端点说明

DeepSeek支持两种Base URL格式：

### 标准格式（推荐）
```
https://api.deepseek.com/chat/completions
```

### OpenAI兼容格式
```
https://api.deepseek.com/v1/chat/completions
```

⚠️ **注意**: 这里的 `v1` 只是为了兼容OpenAI，与模型版本无关。

---

## 💡 使用建议

### 温度参数设置

| 场景 | 推荐温度 | 说明 |
|------|---------|------|
| 精确回答 | 0.1 - 0.3 | 更确定、更一致的输出 |
| 通用对话 | 0.5 - 0.7 | 平衡创造性和准确性 |
| 创意写作 | 0.8 - 1.0 | 更有创造性和多样性 |

### 模型选择

| 需求 | 推荐模型 | 理由 |
|------|---------|------|
| 日常对话 | deepseek-chat | 响应快，通用性强 |
| 复杂推理 | deepseek-reasoner | 思考能力强 |
| 编程相关 | deepseek-coder | 专为代码优化 |
| 数学问题 | deepseek-reasoner | 逻辑推理能力强 |

---

## 🧪 测试配置

配置完成后，建议立即测试：

1. 在API配置列表中找到刚添加的配置
2. 点击"测试"按钮
3. 系统会发送测试消息
4. 如果返回"测试成功"，说明配置正确

### 常见测试问题

**问题1**: "API调用失败: 401"
- **原因**: API密钥错误或已过期
- **解决**: 检查API密钥是否正确复制

**问题2**: "API调用失败: 429"
- **原因**: 超出速率限制或余额不足
- **解决**: 检查账户余额和调用频率

**问题3**: "连接超时"
- **原因**: 网络问题或端点错误
- **解决**: 检查网络连接和API端点URL

---

## 💰 定价说明

DeepSeek的定价非常有竞争力（按100万tokens计费）：

### DeepSeek-V3.2
- **输入**: $0.14 / 1M tokens (约 ¥1.00)
- **输出**: $0.28 / 1M tokens (约 ¥2.00)
- **缓存输入**: $0.014 / 1M tokens (约 ¥0.10)

**示例成本计算**:
- 1000次对话（每次约500 tokens输入 + 500 tokens输出）
- 总tokens: 1M tokens
- 成本: 约 ¥1.50

💡 **提示**: DeepSeek的价格比OpenAI便宜很多，适合大规模应用。

---

## 🔐 安全建议

1. **保护API密钥**
   - 不要在代码中硬编码
   - 不要提交到Git仓库
   - 定期更换密钥

2. **设置使用限制**
   - 在DeepSeek平台设置月度预算
   - 启用使用提醒
   - 监控API调用量

3. **内容安全**
   - 注意输入内容的合规性
   - 避免敏感信息泄露
   - 遵守使用条款

---

## 📊 性能对比

| 特性 | DeepSeek | OpenAI GPT-4 | 优势 |
|------|----------|--------------|------|
| 价格 | ¥1.5/1M | ¥70/1M | DeepSeek便宜47倍 |
| 中文能力 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | DeepSeek更强 |
| 推理能力 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | 相当 |
| 响应速度 | 快 | 快 | 相当 |
| 上下文 | 64K | 128K | GPT-4更长 |

---

## 🚀 高级功能

### 1. 流式响应

本平台暂不支持流式响应，但DeepSeek API支持。

### 2. 思考模式

使用 `deepseek-reasoner` 模型时，响应会包含思考过程。

### 3. 上下文缓存

DeepSeek支持上下文缓存，可以降低重复输入的成本。

---

## ❓ 常见问题

### Q1: DeepSeek和OpenAI有什么区别？

**A**: 
- DeepSeek价格更便宜（便宜约40-50倍）
- DeepSeek对中文支持更好
- DeepSeek API完全兼容OpenAI格式
- OpenAI生态更成熟，支持更多工具

### Q2: 可以用OpenAI的代码直接调用DeepSeek吗？

**A**: 可以！只需要修改两个地方：
1. Base URL改为 `https://api.deepseek.com`
2. API Key改为DeepSeek的密钥

### Q3: deepseek-chat和deepseek-reasoner应该选哪个？

**A**: 
- **日常对话、文本生成**: 选 `deepseek-chat`
- **数学、逻辑、复杂推理**: 选 `deepseek-reasoner`
- **编程相关**: 选 `deepseek-coder`

### Q4: 为什么我的API调用失败？

**A**: 检查以下几点：
1. API密钥是否正确
2. 账户余额是否充足
3. API端点URL是否正确
4. 网络是否可以访问DeepSeek服务

### Q5: DeepSeek支持哪些语言？

**A**: DeepSeek支持多语言，包括：
- 中文（支持优秀）
- 英文
- 以及其他主流语言

---

## 📚 更多资源

- [DeepSeek官方网站](https://www.deepseek.com/)
- [DeepSeek平台](https://platform.deepseek.com/)
- [API文档](https://api-docs.deepseek.com/)
- [定价详情](https://api-docs.deepseek.com/quick_start/pricing)

---

## 📞 技术支持

如有问题：
1. 查看DeepSeek官方文档
2. 访问DeepSeek社区
3. 联系DeepSeek客服
4. 在本项目GitHub提Issue

---

**最后更新**: 2024-12-08  
**DeepSeek版本**: V3.2
