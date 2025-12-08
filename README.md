# 在线问答平台 - QnA Platform

一个功能完整的在线问答平台，支持多种大模型API接入、对话记录管理、数据导出和合规检测。

## 📋 功能特性

### 1. 用户管理
- ✅ 用户注册/登录（JWT认证）
- ✅ 角色权限控制（管理员/普通用户）
- ✅ API调用次数配额管理
- ✅ 合规检测权限管理

### 2. API配置管理
- ✅ 支持多种大模型API提供商
  - OpenAI (GPT-4, GPT-3.5等)
  - Anthropic (Claude系列)
  - Google (Gemini系列)
  - 阿里云通义千问
  - 百度文心一言
  - 本地部署模型
- ✅ 可视化配置界面
- ✅ API测试功能
- ✅ 在线/本地API切换

### 3. 对话功能
- ✅ 多会话管理
- ✅ 实时对话交互
- ✅ 历史记录查看
- ✅ 自动记录API调用日志
- ✅ 消息自动保存
- ✅ 会话删除和归档

### 4. 数据导出
- ✅ 支持多种导出格式
  - JSON格式
  - CSV格式
  - Excel格式
- ✅ 单个会话导出
- ✅ 全部记录导出
- ✅ 自定义导出字段

### 5. 合规检测
- ✅ 批量检测历史记录
- ✅ 文件上传检测
- ✅ 检测任务管理
- ✅ 检测结果查看
- ✅ 预留Python服务接口
- 🔧 支持自定义检测逻辑
  - 大模型+Prompt判别
  - 传统算法检测
  - 混合方案

## 🏗️ 技术架构

### 后端技术栈
- **框架**: Spring Boot 3.2.0
- **数据库**: MySQL 8.0
- **ORM**: MyBatis-Plus 3.5.5
- **认证**: JWT (jjwt 0.12.3)
- **工具**: Hutool 5.8.23
- **HTTP客户端**: OkHttp 4.12.0
- **Excel**: Apache POI 5.2.5

### 前端技术栈
- **框架**: Vue 3.3
- **构建工具**: Vite 5.0
- **UI组件**: Element Plus 2.4
- **路由**: Vue Router 4.2
- **状态管理**: Pinia 2.1
- **HTTP客户端**: Axios 1.6

### 合规检测服务
- **语言**: Python 3.11
- **框架**: Flask 3.0
- **部署**: Docker容器化

## 📦 项目结构

```
qna-platform/
├── backend/                    # Spring Boot后端
│   ├── src/main/java/
│   │   └── com/qna/platform/
│   │       ├── controller/     # 控制器层
│   │       ├── service/        # 服务层
│   │       ├── mapper/         # 数据访问层
│   │       ├── entity/         # 实体类
│   │       ├── dto/            # 数据传输对象
│   │       ├── config/         # 配置类
│   │       ├── common/         # 通用类
│   │       ├── util/           # 工具类
│   │       └── interceptor/    # 拦截器
│   ├── src/main/resources/
│   │   └── application.yml     # 配置文件
│   ├── pom.xml                 # Maven配置
│   └── Dockerfile              # Docker构建文件
│
├── frontend/                   # Vue3前端
│   ├── src/
│   │   ├── views/              # 页面组件
│   │   ├── components/         # 通用组件
│   │   ├── api/                # API接口
│   │   ├── router/             # 路由配置
│   │   ├── store/              # 状态管理
│   │   ├── utils/              # 工具函数
│   │   ├── App.vue             # 根组件
│   │   └── main.js             # 入口文件
│   ├── package.json            # 依赖配置
│   ├── vite.config.js          # Vite配置
│   ├── nginx.conf              # Nginx配置
│   └── Dockerfile              # Docker构建文件
│
├── compliance-service/         # Python合规检测服务
│   ├── app.py                  # Flask应用
│   ├── requirements.txt        # Python依赖
│   └── Dockerfile              # Docker构建文件
│
├── sql/                        # 数据库脚本
│   └── schema.sql              # 建表脚本
│
├── docker-compose.yml          # Docker编排配置
└── README.md                   # 项目文档
```

## 🚀 快速开始

### 方式1: Docker部署（推荐）

#### 前提条件
- Docker 20.10+
- Docker Compose 2.0+

#### 部署步骤

1. **克隆项目**
```bash
cd /home/user/webapp
```

2. **启动所有服务**
```bash
docker-compose up -d
```

3. **查看服务状态**
```bash
docker-compose ps
```

4. **访问应用**
- 前端: http://localhost:3000
- 后端API: http://localhost:8080/api
- MySQL: localhost:3306

5. **默认账号**
- 管理员: `admin` / `admin123`
- 测试用户: `testuser` / `user123`

### 方式2: 本地开发部署

#### 后端部署

1. **安装MySQL 8.0**

2. **创建数据库并导入表结构**
```bash
mysql -u root -p < sql/schema.sql
```

3. **配置数据库连接**
编辑 `backend/src/main/resources/application.yml`，修改数据库连接信息

4. **启动后端**
```bash
cd backend
mvn spring-boot:run
```

后端将在 http://localhost:8080 启动

#### 前端部署

1. **安装依赖**
```bash
cd frontend
npm install
```

2. **启动开发服务器**
```bash
npm run dev
```

前端将在 http://localhost:3000 启动

3. **构建生产版本**
```bash
npm run build
```

#### Python合规检测服务（可选）

1. **安装Python依赖**
```bash
cd compliance-service
pip install -r requirements.txt
```

2. **启动服务**
```bash
python app.py
```

服务将在 http://localhost:5000 启动

## 🔧 配置说明

### 后端配置

编辑 `backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/qna_platform
    username: root
    password: your_password

jwt:
  secret: your_jwt_secret_key
  expiration: 86400000  # 24小时

app:
  compliance:
    service-url: http://localhost:5000/api/compliance/check
```

### 前端配置

编辑 `frontend/vite.config.js`:

```javascript
server: {
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

### 合规检测服务配置

编辑 `compliance-service/app.py`，实现你的检测逻辑：

```python
def check_with_llm(content):
    """
    使用大模型进行合规检测
    
    1. 配置你的大模型API（OpenAI/Anthropic/本地模型等）
    2. 设计合适的Prompt来判断内容合规性
    3. 解析模型返回的结果
    """
    # TODO: 实现你的检测逻辑
    pass
```

## 📖 API文档

### 认证接口

#### 用户登录
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

#### 用户注册
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "newuser",
  "password": "password123"
}
```

### 对话接口

#### 发送消息
```http
POST /api/chat/send
Authorization: Bearer <token>
Content-Type: application/json

{
  "sessionId": null,           // 新会话时为null
  "apiConfigId": 1,            // API配置ID
  "message": "你好",           // 消息内容
  "sessionTitle": "新对话"     // 会话标题（可选）
}
```

#### 获取会话历史
```http
GET /api/chat/session/{sessionId}
Authorization: Bearer <token>
```

#### 获取所有会话
```http
GET /api/chat/sessions
Authorization: Bearer <token>
```

### API配置接口

#### 获取启用的API配置
```http
GET /api/api-config/enabled
Authorization: Bearer <token>
```

#### 创建API配置
```http
POST /api/api-config
Authorization: Bearer <token>
Content-Type: application/json

{
  "configName": "OpenAI GPT-4",
  "provider": "OpenAI",
  "modelName": "gpt-4",
  "apiEndpoint": "https://api.openai.com/v1/chat/completions",
  "apiKey": "your-api-key",
  "apiType": "ONLINE",
  "maxTokens": 2000,
  "temperature": 0.7
}
```

### 导出接口

#### 导出会话（JSON）
```http
GET /api/export/session/{sessionId}/json
Authorization: Bearer <token>
```

#### 导出所有记录
```http
GET /api/export/all/{format}
Authorization: Bearer <token>

format: json | csv | excel
```

### 合规检测接口

#### 创建检测任务
```http
POST /api/compliance/task
Authorization: Bearer <token>
Content-Type: application/json

{
  "taskName": "历史记录检测",
  "taskType": "LOG",
  "startTime": "2024-01-01 00:00:00",
  "endTime": "2024-12-31 23:59:59"
}
```

## 🔐 安全说明

1. **JWT密钥**: 生产环境请修改 `application.yml` 中的 `jwt.secret`
2. **数据库密码**: 修改默认的数据库密码
3. **API密钥**: API配置中的密钥会加密存储
4. **CORS配置**: 生产环境建议限制跨域来源

## 📊 数据库设计

### 主要表说明

- `sys_user`: 系统用户表
- `api_config`: API配置表
- `chat_session`: 对话会话表
- `chat_message`: 对话消息表
- `compliance_task`: 合规检测任务表
- `compliance_result`: 合规检测结果表
- `api_call_log`: API调用日志表

详细表结构请查看 `sql/schema.sql`

## 🤝 合规检测实现指南

### 方案1: 使用大模型+Prompt（推荐）

```python
import openai

def check_with_llm(content):
    prompt = f"""
    请判断以下文本内容是否合规。评估维度包括：
    1. 是否包含违法违规信息
    2. 是否包含色情、暴力、恐怖内容
    3. 是否包含歧视、仇恨言论
    4. 是否包含虚假信息或诈骗内容
    
    待检测内容：{content}
    """
    
    response = openai.ChatCompletion.create(
        model="gpt-4",
        messages=[{"role": "user", "content": prompt}]
    )
    
    # 解析返回结果
    return parse_result(response)
```

### 方案2: 使用传统算法

```python
def check_with_rules(content):
    # 敏感词检测
    sensitive_words = load_sensitive_words()
    
    # 文本分类
    category = classify_text(content)
    
    # 规则判断
    if has_sensitive_words(content, sensitive_words):
        return {"result": "FAIL", "risk_level": "HIGH"}
    
    return {"result": "PASS", "risk_level": "LOW"}
```

## 📝 常见问题

### Q1: 如何添加新的大模型API？

1. 在API配置管理页面点击"添加配置"
2. 选择对应的提供商
3. 填写模型名称、API端点和密钥
4. 点击"测试"验证配置
5. 保存后即可在对话中使用

### Q2: 合规检测服务如何对接？

1. 实现Python服务的 `/api/compliance/check` 接口
2. 确保接口返回符合规范的JSON格式
3. 在后端配置文件中设置服务URL
4. 启动Python服务

### Q3: 如何修改API调用配额？

管理员可以直接在数据库中修改 `sys_user` 表的 `api_quota` 字段

### Q4: 如何备份数据？

使用 mysqldump 备份数据库：
```bash
mysqldump -u root -p qna_platform > backup.sql
```

## 🛠️ 开发调试

### 后端调试

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

### 前端调试

```bash
cd frontend
npm run dev
```

访问 http://localhost:3000，Chrome DevTools 进行调试

### 查看日志

```bash
# Docker方式
docker-compose logs -f backend
docker-compose logs -f frontend

# 本地方式
tail -f backend/logs/qna-platform.log
```

## 📈 性能优化建议

1. **数据库优化**
   - 为常用查询字段添加索引
   - 定期清理历史数据
   - 使用连接池

2. **API调用优化**
   - 实现请求缓存
   - 设置合理的超时时间
   - 使用连接复用

3. **前端优化**
   - 启用Gzip压缩
   - 使用CDN加速
   - 图片懒加载

## 📄 许可证

MIT License

## 👥 贡献

欢迎提交Issue和Pull Request！

## 📧 联系方式

如有问题，请通过以下方式联系：
- 提交Issue
- 发送邮件

---

**祝你使用愉快！** 🎉
