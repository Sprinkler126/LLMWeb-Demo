# 对话部分文件上传功能实现文档

## 功能概述
为聊天对话功能添加了文件上传支持，允许用户上传文本类文件，文件内容将被解析并作为上下文与用户消息一起发送给AI模型。

## 支持的文件类型
- 纯文本文件：.txt, .md, .log
- 配置文件：.json, .xml, .yml, .yaml, .properties, .csv
- 代码文件：.java, .py, .js, .ts, .html, .css

## 功能特性

### 1. 文件上传限制
- 单个文件大小：最大 10MB
- 最多上传：10个文件
- 文本内容长度：最大 100KB（超过部分将被截断）

### 2. 用户体验
- **拖拽上传**：支持直接拖拽文件到上传区域
- **多文件上传**：支持一次选择多个文件
- **文件预览**：显示已上传文件的名称和大小
- **文件移除**：可以在发送前移除不需要的文件
- **实时反馈**：上传过程中显示加载状态

### 3. 消息构建
当用户上传文件时，最终发送给AI的消息格式为：
```
[用户输入的文本]

--- 附件内容 ---

【文件: filename1.txt】
[文件1的内容]

【文件: filename2.py】
[文件2的内容]
```

## 技术实现

### 后端实现

#### 1. 文件解析工具 - FileParser.java
位置：`backend/src/main/java/com/qna/platform/util/FileParser.java`

主要功能：
- 根据文件扩展名识别文件类型
- 使用UTF-8编码读取文本文件
- 提供文件大小检查

关键方法：
```java
public String parseFile(MultipartFile file) throws IOException
public boolean checkFileSize(long fileSize, int maxSizeMB)
```

#### 2. 文件上传结果 DTO - FileUploadResult.java
位置：`backend/src/main/java/com/qna/platform/dto/FileUploadResult.java`

包含字段：
- fileName: 文件名
- fileType: 文件MIME类型
- fileSize: 文件大小（字节）
- content: 提取的文本内容
- success: 是否成功
- errorMessage: 错误信息

#### 3. 控制器端点 - ChatController.java
位置：`backend/src/main/java/com/qna/platform/controller/ChatController.java`

新增端点：
```java
POST /api/chat/upload-files
- 参数：MultipartFile[] files
- 返回：List<FileUploadResult>
- 权限：需要 API_USE 权限
```

处理流程：
1. 验证用户身份
2. 检查文件数量（最多10个）
3. 遍历处理每个文件：
   - 检查文件大小
   - 解析文件内容
   - 限制内容长度
4. 返回处理结果

### 前端实现

#### 1. API调用 - chat.js
位置：`frontend/src/api/chat.js`

新增函数：
```javascript
export function uploadFiles(files)
```

使用 FormData 发送多部分表单数据，设置正确的 Content-Type。

#### 2. Chat组件更新 - Chat.vue
位置：`frontend/src/views/Chat.vue`

##### 新增UI元素：
1. **文件上传按钮**
   - 使用 el-upload 组件
   - 配置了支持的文件类型
   - 限制文件数量为10个
   - 自动上传设为false（手动控制）

2. **已上传文件展示**
   - 使用 el-tag 展示文件信息
   - 显示文件名和大小
   - 可关闭删除

##### 新增响应式变量：
```javascript
const uploadedFiles = ref([])  // 已上传的文件列表
const uploading = ref(false)   // 上传状态
const uploadRef = ref(null)    // 上传组件引用
```

##### 新增方法：

1. **handleFileChange(uploadFile)**
   - 处理文件选择事件
   - 验证文件大小和数量
   - 调用后端API上传文件
   - 更新已上传文件列表

2. **removeFile(index)**
   - 从已上传列表中移除文件

3. **formatFileSize(bytes)**
   - 格式化文件大小显示（B/KB/MB）

##### 修改的方法：

**sendMessage()**
- 在发送前合并用户输入和文件内容
- 构建完整的消息内容
- 发送成功后清空文件列表
- 发送失败时恢复文件列表

## 使用流程

### 用户操作步骤：
1. 进入对话页面
2. 选择AI模型（必需）
3. 可选：输入文本消息
4. 点击"上传文件"按钮，选择一个或多个文本文件
5. 等待文件上传完成（会显示已上传的文件标签）
6. 可以继续添加更多文件或移除不需要的文件
7. 点击"发送"按钮
8. 系统将文本输入和所有文件内容一起发送给AI
9. AI会基于完整的上下文生成回复

### 注意事项：
- 必须先选择AI模型才能发送消息
- 可以只上传文件而不输入文本（至少需要一项）
- 文件内容会被完整包含在消息中，可能会消耗较多token
- 超大文件内容会被自动截断以保护系统性能

## 错误处理

### 前端错误提示：
- 文件超过10MB：显示错误消息，不上传该文件
- 文件数量超过10个：显示警告，阻止继续上传
- 上传失败：显示具体错误信息

### 后端错误处理：
- 不支持的文件类型：返回 UnsupportedOperationException
- 文件为空：返回 IllegalArgumentException
- 读取失败：返回 IOException
- 每个文件独立处理，一个失败不影响其他文件

## 最佳实践

### 性能优化：
1. 限制文件大小（10MB）和数量（10个）
2. 限制提取的文本内容长度（100KB）
3. 使用异步上传，不阻塞UI
4. 分别处理每个文件，提供详细反馈

### 安全考虑：
1. 严格验证文件类型（通过扩展名）
2. 限制文件大小，防止拒绝服务攻击
3. 使用UTF-8编码，避免编码问题
4. 需要用户登录和API使用权限

### 用户体验：
1. 清晰的文件类型提示
2. 实时上传状态反馈
3. 可视化的已上传文件列表
4. 灵活的文件管理（添加/删除）
5. 友好的错误提示

## 未来扩展

### 可能的改进方向：
1. 支持PDF文件解析（需要Apache PDFBox）
2. 支持Word文档解析（需要Apache POI的更多功能）
3. 支持Excel文件解析
4. 支持图片OCR文本提取
5. 文件内容预览功能
6. 更智能的内容摘要（对超长内容）
7. 支持从云存储选择文件
8. 文件上传进度条
9. 拖拽上传UI优化

## 测试建议

### 功能测试：
1. ✅ 上传不同类型的文本文件
2. ✅ 上传多个文件
3. ✅ 测试文件大小限制
4. ✅ 测试文件数量限制
5. ✅ 测试文件移除功能
6. ✅ 测试只上传文件不输入文本
7. ✅ 测试只输入文本不上传文件
8. ✅ 测试同时输入文本和上传文件

### 边界测试：
1. ✅ 上传空文件
2. ✅ 上传不支持的文件类型
3. ✅ 上传超大文件
4. ✅ 上传11个文件
5. ✅ 上传包含特殊字符的文件名
6. ✅ 上传非UTF-8编码的文件

### 集成测试：
1. ✅ 验证文件内容正确传递给AI
2. ✅ 验证AI能够理解文件内容
3. ✅ 验证权限控制
4. ✅ 验证配额系统正常工作

## 部署说明

### 配置要求：
- application.yml 中已配置文件上传大小限制：
  ```yaml
  spring:
    servlet:
      multipart:
        enabled: true
        max-file-size: 50MB
        max-request-size: 50MB
  ```

### 依赖要求：
- Spring Boot Web（已包含）
- Spring Boot Validation（已包含）
- Lombok（已包含）

### 启动顺序：
1. 启动后端服务（端口8080）
2. 启动前端开发服务器（端口3000）
3. 确保nginx或API网关正确配置代理

## 总结

本实现为聊天对话功能添加了完整的文件上传支持，遵循以下设计原则：

1. **简单易用**：用户界面清晰，操作流程简单
2. **安全可靠**：严格的验证和错误处理
3. **性能优化**：合理的限制和资源管理
4. **可扩展性**：清晰的代码结构，易于添加新功能
5. **最佳实践**：遵循Spring Boot和Vue3的标准实践

该功能已完全实现并可投入使用，为文本类AI模型提供了强大的文档分析能力。
