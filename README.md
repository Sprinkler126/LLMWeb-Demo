# QnA Platform - 智能问答与合规检测平台

<div align="center">

![Version](https://img.shields.io/badge/version-2.0.0-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)
![Vue](https://img.shields.io/badge/Vue-3.3-green.svg)
![License](https://img.shields.io/badge/license-MIT-orange.svg)

**一个功能完整的企业级问答平台，集成多种大模型API、智能对话管理、数据分析与合规检测**

[快速开始](#-快速开始) • [功能特性](#-功能特性) • [技术架构](#️-技术架构) • [API文档](#-api文档) • [部署指南](#-部署指南)

</div>

---

## 📋 目录

- [项目简介](#-项目简介)
- [核心功能](#-核心功能)
- [技术架构](#️-技术架构)
- [快速开始](#-快速开始)
- [功能详解](#-功能详解)
- [API文档](#-api文档)
- [部署指南](#-部署指南)
- [配置说明](#-配置说明)
- [常见问题](#-常见问题)
- [更新日志](#-更新日志)

---

## 🎯 项目简介

QnA Platform 是一个企业级智能问答与合规检测平台，提供了完整的用户管理、对话管理、数据分析和合规检测功能。平台支持接入多种主流大模型API，并提供灵活的权限控制和数据导出能力。

### 适用场景

- 🏢 **企业内部问答系统** - 接入私有大模型，为员工提供智能助手
- 🔒 **合规内容审核** - 批量检测历史对话，确保内容合规
- 📊 **数据分析与审计** - 平台级数据统计，管理员导出用户记录
- 🎓 **教育培训平台** - 为学生提供AI辅导，教师可查看学习数据
- 🛡️ **安全监控系统** - 实时监控对话内容，识别潜在风险

### 核心优势

- ✅ **完整的权限体系** - 超管/管理员/普通用户三级权限控制
- ✅ **灵活的API管理** - 支持多种大模型API，可视化配置界面
- ✅ **强大的数据能力** - 平台级统计、用户级导出、历史记录管理
- ✅ **可扩展的架构** - 模块化设计，易于扩展和定制
- ✅ **生产级代码质量** - 完整的错误处理、日志记录和安全防护

---

## 🚀 核心功能

### 1. 用户与权限管理

<table>
<tr>
<td width="50%">

#### 个人信息管理
- 📝 查看和修改个人资料（邮箱）
- 🔑 密码修改（原密码验证）
- 📊 API配额使用统计
- 📈 可视化使用率展示
- ⚠️ 配额预警提示

</td>
<td width="50%">

#### 权限控制
- 👑 超级管理员（SUPER_ADMIN）
- 👨‍💼 管理员（ADMIN）
- 👤 普通用户（USER）
- 🔐 基于注解的权限验证
- 🛡️ JWT Token 认证

</td>
</tr>
</table>

**访问路径**: 右上角头像 → 个人信息

---

### 2. 智能对话系统

<table>
<tr>
<td width="50%">

#### 对话功能
- 💬 多会话管理
- 🔄 实时对话交互
- 📜 历史记录查看
- 💾 自动保存消息
- 🗑️ 会话删除和管理

</td>
<td width="50%">

#### API配置
- 🌐 支持多种大模型
  - OpenAI (GPT-4, GPT-3.5)
  - Anthropic (Claude)
  - Google (Gemini)
  - 阿里云通义千问
  - 百度文心一言
  - 本地部署模型
- 🧪 API连接测试
- ⚙️ 参数配置（温度、Token数等）

</td>
</tr>
</table>

**访问路径**: 侧边栏 → 对话 / API配置

---

### 3. 数据导出功能

<table>
<tr>
<td width="50%">

#### 个人导出
- 📄 单个会话导出（JSON/CSV/Excel）
- 📦 全部记录导出
- 🎨 自定义导出格式
- 💾 一键下载

</td>
<td width="50%">

#### 管理员导出 🆕
- 🔍 按用户ID搜索
- 📋 查看用户所有会话
- 📥 导出指定用户的会话
- 🔒 权限自动验证

</td>
</tr>
</table>

**访问路径**: 
- 个人导出: 侧边栏 → 数据导出
- 管理员导出: 侧边栏 → 导出用户记录（需管理员权限）

---

### 4. 平台数据分析 🆕

<table>
<tr>
<td width="50%">

#### 核心指标
- 👥 用户总数
- 💬 对话总数
- 📊 最近一天对话数
- ⚠️ 违规占比

</td>
<td width="50%">

#### 详细统计
- 📈 会话统计（总数、活跃数）
- 👤 用户增长（今日新增）
- 🛡️ 合规监测（违规率、进度条）
- 🔄 实时刷新数据

</td>
</tr>
</table>

**访问路径**: 侧边栏 → 平台数据（需管理员权限）

---

### 5. 合规检测系统

<table>
<tr>
<td width="50%">

#### 检测功能
- 📝 批量检测历史记录
- 📤 文件上传检测
- 📋 检测任务管理
- 📊 检测结果查看
- 🔧 Python服务接口对接

</td>
<td width="50%">

#### 系统配置 🆕
- 🔗 Python接口地址配置
- ⏱️ 超时时间设置
- 🔌 启用/禁用开关
- 🧪 连接测试功能
- 📋 配置历史追踪

</td>
</tr>
</table>

**访问路径**: 
- 合规检测: 侧边栏 → 合规检测（需权限）
- 系统配置: 侧边栏 → 系统配置（仅超管）

---

### 6. 用户管理 & 角色权限

<table>
<tr>
<td width="50%">

#### 用户管理
- 👥 用户列表查看
- ➕ 创建新用户
- 🔍 搜索和筛选
- ✏️ 编辑用户信息
- 🗑️ 删除用户
- 📊 API配额管理

</td>
<td width="50%">

#### 角色权限
- 🎭 角色管理
- 🔐 权限配置
- 🔗 用户-角色绑定
- 🛡️ 权限继承体系
- 📝 操作日志记录

</td>
</tr>
</table>

**访问路径**: 
- 侧边栏 → 用户管理（需管理员权限）
- 侧边栏 → 角色权限（需管理员权限）

---

## 🏗️ 技术架构

### 系统架构图

```
┌─────────────────────────────────────────────────────────────┐
│                         前端层 (Vue3)                        │
│  ┌──────────┬──────────┬──────────┬──────────┬──────────┐  │
│  │  对话    │  API配置  │  数据导出 │  平台数据 │  系统配置 │  │
│  │ 管理页面 │  管理页面 │  页面    │  页面    │  页面    │  │
│  └──────────┴──────────┴──────────┴──────────┴──────────┘  │
└─────────────────────────────────────────────────────────────┘
                              ↕ HTTP/HTTPS
┌─────────────────────────────────────────────────────────────┐
│                    后端层 (Spring Boot)                      │
│  ┌────────────────────────────────────────────────────────┐ │
│  │              Controller 层 (REST API)                   │ │
│  ├────────────────────────────────────────────────────────┤ │
│  │  认证   │  对话   │  导出   │ Dashboard │  配置  │ 管理  │ │
│  │  鉴权   │  管理   │  功能   │  统计     │  管理  │ 功能  │ │
│  └────────────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────────────┐ │
│  │              Service 层 (业务逻辑)                      │ │
│  ├────────────────────────────────────────────────────────┤ │
│  │  用户   │  会话   │  消息   │  检测   │  配置   │ 权限   │ │
│  │  服务   │  服务   │  服务   │  服务   │  服务   │ 服务   │ │
│  └────────────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────────────┐ │
│  │           Mapper 层 (数据访问) - MyBatis-Plus           │ │
│  └────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                              ↕ JDBC
┌─────────────────────────────────────────────────────────────┐
│                      数据层 (MySQL 8.0)                      │
│  ┌──────────┬──────────┬──────────┬──────────┬──────────┐  │
│  │ sys_user │chat_     │compliance│sys_config│api_call_ │  │
│  │sys_role  │session   │_task     │sys_      │log       │  │
│  │          │chat_     │compliance│permission│          │  │
│  │          │message   │_result   │          │          │  │
│  └──────────┴──────────┴──────────┴──────────┴──────────┘  │
└─────────────────────────────────────────────────────────────┘

                    ↕ HTTP REST API (可选)
┌─────────────────────────────────────────────────────────────┐
│              Python合规检测服务 (Flask)                       │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  /check_compliance - 合规检测接口                       │ │
│  │  - 大模型+Prompt判别                                    │ │
│  │  - 传统算法检测                                         │ │
│  │  - 混合检测方案                                         │ │
│  └────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### 技术栈详情

#### 后端技术栈
```
框架层
├── Spring Boot 3.2.0          # 核心框架
├── Spring Security            # 安全框架
└── Spring Web                 # Web支持

数据层
├── MyBatis-Plus 3.5.5        # ORM框架
├── MySQL 8.0                 # 关系数据库
├── HikariCP                  # 连接池
└── Flyway                    # 数据库迁移

安全认证
├── JWT (jjwt 0.12.3)        # Token认证
└── BCrypt                    # 密码加密

工具库
├── Hutool 5.8.23            # Java工具库
├── Apache POI 5.2.5         # Excel处理
├── OkHttp 4.12.0            # HTTP客户端
└── Lombok                    # 代码简化
```

#### 前端技术栈
```
核心框架
├── Vue 3.3                   # 渐进式框架
├── Vite 5.0                  # 构建工具
└── JavaScript ES6+           # 编程语言

UI组件
├── Element Plus 2.4          # UI组件库
└── Element Plus Icons        # 图标库

路由与状态
├── Vue Router 4.2            # 路由管理
└── Pinia 2.1                 # 状态管理

网络请求
└── Axios 1.6                 # HTTP客户端
```

#### 合规检测服务（可选）
```
Python 3.11
├── Flask 3.0                 # Web框架
├── OpenAI SDK               # 大模型API
├── Requests                 # HTTP客户端
└── 自定义检测逻辑
```

---

## 📦 项目结构

```
LLMWeb-Demo/
├── backend/                           # Spring Boot 后端
│   ├── src/main/java/com/qna/platform/
│   │   ├── annotation/                # 自定义注解
│   │   │   ├── RequirePermission.java # 权限注解
│   │   │   └── RequireRole.java       # 角色注解
│   │   ├── common/                    # 通用类
│   │   │   └── Result.java            # 统一响应对象
│   │   ├── config/                    # 配置类
│   │   │   ├── JwtConfig.java         # JWT配置
│   │   │   ├── WebConfig.java         # Web配置
│   │   │   ├── WebMvcConfig.java      # MVC配置
│   │   │   └── RestTemplateConfig.java# HTTP客户端配置
│   │   ├── controller/                # 控制器层
│   │   │   ├── AuthController.java    # 认证控制器
│   │   │   ├── ChatController.java    # 对话控制器
│   │   │   ├── ExportController.java  # 导出控制器
│   │   │   ├── DashboardController.java         # 平台数据控制器 🆕
│   │   │   ├── UserProfileController.java       # 个人信息控制器 🆕
│   │   │   ├── SystemConfigController.java      # 系统配置控制器 🆕
│   │   │   ├── UserManagementController.java    # 用户管理控制器
│   │   │   └── RolePermissionController.java    # 角色权限控制器
│   │   ├── dto/                       # 数据传输对象
│   │   │   ├── UserProfileDTO.java    # 用户信息DTO 🆕
│   │   │   ├── UpdateProfileDTO.java  # 更新信息DTO 🆕
│   │   │   └── UpdatePasswordDTO.java # 修改密码DTO 🆕
│   │   ├── entity/                    # 实体类
│   │   │   ├── SysUser.java           # 用户实体
│   │   │   ├── SysRole.java           # 角色实体
│   │   │   ├── SysPermission.java     # 权限实体
│   │   │   ├── SysConfig.java         # 系统配置实体 🆕
│   │   │   ├── ChatSession.java       # 会话实体
│   │   │   ├── ChatMessage.java       # 消息实体
│   │   │   └── ApiConfig.java         # API配置实体
│   │   ├── interceptor/               # 拦截器
│   │   │   ├── JwtInterceptor.java    # JWT拦截器
│   │   │   └── PermissionInterceptor.java # 权限拦截器
│   │   ├── mapper/                    # 数据访问层
│   │   │   ├── SysUserMapper.java
│   │   │   ├── SysConfigMapper.java   # 系统配置Mapper 🆕
│   │   │   └── ...
│   │   ├── service/                   # 服务层接口
│   │   │   ├── AuthService.java
│   │   │   ├── ChatService.java
│   │   │   ├── ExportService.java
│   │   │   ├── DashboardService.java  # 平台数据服务 🆕
│   │   │   ├── UserProfileService.java# 个人信息服务 🆕
│   │   │   ├── SystemConfigService.java# 系统配置服务 🆕
│   │   │   └── ...
│   │   ├── service/impl/              # 服务层实现
│   │   │   └── ...
│   │   ├── util/                      # 工具类
│   │   │   └── JwtUtil.java           # JWT工具
│   │   └── QnaPlatformApplication.java# 启动类
│   ├── src/main/resources/
│   │   ├── application.yml            # 应用配置
│   │   └── db/migration/              # 数据库迁移脚本
│   │       ├── V1__init_schema.sql
│   │       ├── V2__add_permissions.sql
│   │       ├── V3__add_roles.sql
│   │       ├── V4__update_user_table.sql
│   │       └── V5__add_system_config_table.sql 🆕
│   └── pom.xml                        # Maven配置
│
├── frontend/                          # Vue3 前端
│   ├── src/
│   │   ├── api/                       # API接口
│   │   │   ├── auth.js                # 认证API
│   │   │   ├── chat.js                # 对话API
│   │   │   ├── profile.js             # 个人信息API 🆕
│   │   │   ├── adminExport.js         # 管理员导出API 🆕
│   │   │   ├── dashboard.js           # 平台数据API 🆕
│   │   │   └── systemConfig.js        # 系统配置API 🆕
│   │   ├── components/                # 通用组件
│   │   ├── composables/               # 组合式函数
│   │   │   └── useAsyncOperations.js  # 异步操作管理 🆕
│   │   ├── router/                    # 路由配置
│   │   │   ├── index.js               # 路由主文件
│   │   │   └── errorHandler.js        # 错误处理 🆕
│   │   ├── store/                     # 状态管理
│   │   │   └── user.js                # 用户状态
│   │   ├── utils/                     # 工具函数
│   │   │   └── request.js             # Axios封装
│   │   ├── views/                     # 页面组件
│   │   │   ├── Login.vue              # 登录页
│   │   │   ├── Layout.vue             # 布局页
│   │   │   ├── Chat.vue               # 对话页
│   │   │   ├── ApiConfig.vue          # API配置页
│   │   │   ├── Export.vue             # 数据导出页
│   │   │   ├── Compliance.vue         # 合规检测页
│   │   │   ├── Profile.vue            # 个人信息页 🆕
│   │   │   ├── AdminExport.vue        # 管理员导出页 🆕
│   │   │   ├── Dashboard.vue          # 平台数据页 🆕
│   │   │   ├── SystemConfig.vue       # 系统配置页 🆕
│   │   │   ├── UserManagement.vue     # 用户管理页
│   │   │   └── RolePermission.vue     # 角色权限页
│   │   ├── App.vue                    # 根组件
│   │   └── main.js                    # 入口文件
│   ├── package.json                   # 依赖配置
│   └── vite.config.js                 # Vite配置
│
├── docs/                              # 文档目录
│   ├── NEW_FEATURES_SUMMARY.md        # 新功能总结 🆕
│   ├── ALL_ISSUES_FIXED_SUMMARY.md    # 问题修复总结
│   ├── ROOT_CAUSE_JWT_INTERCEPTOR.md  # JWT拦截器分析
│   ├── QUICK_FIX_GUIDE.md             # 快速修复指南
│   └── ...
│
└── README.md                          # 项目说明
```

---

## 🚀 快速开始

### 前置要求

- **JDK**: 17+
- **Node.js**: 16+
- **MySQL**: 8.0+
- **Maven**: 3.8+
- **Git**: 2.0+

### 1. 克隆项目

```bash
git clone https://github.com/Sprinkler126/LLMWeb-Demo.git
cd LLMWeb-Demo
```

### 2. 配置数据库

#### 2.1 创建数据库
```sql
CREATE DATABASE qna_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### 2.2 配置连接信息
编辑 `backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/qna_platform?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: your_password  # 修改为你的MySQL密码
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### 3. 启动后端

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

**验证后端启动成功**:
- 后端运行在 `http://localhost:8080`
- 控制台显示 "Started QnaPlatformApplication"
- Flyway 会自动执行数据库迁移脚本

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

**验证前端启动成功**:
- 前端运行在 `http://localhost:3000`
- 浏览器自动打开登录页面

### 5. 登录测试

使用默认账号登录：

- **超级管理员**: `admin` / `admin123`
- **测试用户**: `testuser` / `user123`

---

## 💡 功能详解

### 1. 个人信息管理

**功能路径**: 右上角头像下拉菜单 → 个人信息

**主要功能**:

1. **基本信息展示**
   - 用户名（只读）
   - 邮箱（可修改）
   - 角色标签（SUPER_ADMIN/ADMIN/USER）
   - 账号状态（正常/禁用）
   - 注册时间
   - 最后登录时间

2. **API配额管理**
   ```
   总配额: 1000次
   已使用: 250次
   剩余: 750次
   使用率: 25%
   ```
   - 可视化进度条
   - 使用率预警（>80%显示提示）
   - 实时更新

3. **密码修改**
   - 原密码验证
   - 新密码要求（至少6位）
   - 密码确认验证
   - 修改成功后自动跳转登录

**后端API**:
```
GET  /user/profile                  # 获取个人信息
PUT  /user/profile                  # 更新个人信息
PUT  /user/profile/password         # 修改密码
GET  /user/profile/api-usage        # 获取API使用统计
```

---

### 2. 管理员导出用户记录

**功能路径**: 侧边栏 → 导出用户记录（需管理员权限）

**主要功能**:

1. **用户搜索**
   - 输入用户ID进行搜索
   - 显示用户名和会话总数
   - 搜索结果缓存

2. **会话列表**
   - 会话ID
   - 会话标题
   - 创建时间
   - 最后更新时间
   - 表格形式展示

3. **导出功能**
   - 单个会话导出为JSON
   - 文件命名：`user_{userId}_session_{sessionId}.json`
   - 包含完整聊天历史

**权限控制**:
- 自动验证当前用户是否是管理员
- 验证目标会话是否属于指定用户
- 防止越权访问

**后端API**:
```
GET  /export/admin/user/{targetUserId}/sessions        # 获取用户会话列表
GET  /export/admin/session/{sessionId}/json            # 导出指定会话
```

---

### 3. 平台数据展示（Dashboard）

**功能路径**: 侧边栏 → 平台数据（需管理员权限）

**主要功能**:

1. **核心指标卡片**（4个）
   ```
   ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
   │ 用户总数     │  │ 对话总数     │  │ 最近一天     │  │ 违规占比     │
   │    1,234     │  │   10,567     │  │     856      │  │    2.5%      │
   └──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘
   ```

2. **会话统计**
   - 会话总数
   - 活跃会话数（最近24小时有更新）

3. **用户增长**
   - 用户总数
   - 今日新增用户

4. **合规监测**
   - 总消息数
   - 违规消息数
   - 违规率（带进度条）
   - 颜色分级：
     - 绿色 < 5%
     - 黄色 5-10%
     - 红色 > 10%

5. **数据刷新**
   - 手动刷新按钮
   - 显示最后更新时间

**后端API**:
```
GET  /admin/dashboard/statistics    # 获取平台统计数据
```

---

### 4. Python检测接口配置

**功能路径**: 侧边栏 → 系统配置（仅超级管理员）

**主要功能**:

1. **配置项管理**
   ```
   接口地址: http://localhost:5000/check_compliance
   超时时间: 30000 毫秒
   启用状态: ☑ 启用
   ```

2. **连接测试**
   - 一键测试Python接口
   - 显示响应时间
   - 显示测试结果详情
   - 错误详情展示

3. **配置列表**
   - 查看所有系统配置
   - 配置键、值、描述
   - 配置类型（STRING/NUMBER/BOOLEAN/JSON）
   - 最后更新时间和更新人

**数据库存储**:
```sql
sys_config 表
├── python.compliance.endpoint   (接口地址)
├── python.compliance.timeout    (超时时间)
└── python.compliance.enabled    (是否启用)
```

**后端API**:
```
GET   /admin/system-config                      # 获取所有配置
PUT   /admin/system-config/batch                # 批量更新配置
POST  /admin/system-config/test-python-connection  # 测试连接
```

---

## 📚 API文档

### 认证接口

#### 用户登录
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}

Response:
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzM4NCJ9...",
    "userInfo": {
      "userId": 1,
      "username": "admin",
      "role": "SUPER_ADMIN",
      ...
    }
  }
}
```

#### 用户注册
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "newuser",
  "password": "password123",
  "email": "user@example.com"
}
```

### 个人信息接口

#### 获取个人信息
```http
GET /api/user/profile
Authorization: Bearer <token>

Response:
{
  "code": 200,
  "data": {
    "userId": 1,
    "username": "admin",
    "email": "admin@example.com",
    "roleName": "超级管理员",
    "roleCode": "SUPER_ADMIN",
    "apiQuota": 1000,
    "apiUsed": 250,
    ...
  }
}
```

#### 修改密码
```http
PUT /api/user/profile/password
Authorization: Bearer <token>
Content-Type: application/json

{
  "oldPassword": "admin123",
  "newPassword": "newpassword123"
}
```

### 平台数据接口

#### 获取统计数据
```http
GET /admin/dashboard/statistics
Authorization: Bearer <token>

Response:
{
  "code": 200,
  "data": {
    "totalUsers": 1234,
    "totalMessages": 10567,
    "recentDayMessages": 856,
    "violationRate": 2.5,
    "activeSessions": 150,
    "newUsersToday": 5,
    ...
  }
}
```

### 管理员导出接口

#### 获取用户会话列表
```http
GET /export/admin/user/{targetUserId}/sessions
Authorization: Bearer <token>

Response:
{
  "code": 200,
  "data": {
    "targetUserId": 2,
    "targetUsername": "testuser",
    "totalCount": 10,
    "sessions": [...]
  }
}
```

#### 导出用户会话
```http
GET /export/admin/session/{sessionId}/json?targetUserId={userId}&token={token}

# 注意：导出接口使用 window.open 打开，token 通过URL参数传递
```

### 系统配置接口

#### 获取所有配置
```http
GET /admin/system-config
Authorization: Bearer <token>

Response:
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "configKey": "python.compliance.endpoint",
      "configValue": "http://localhost:5000/check_compliance",
      "configDesc": "Python合规检测接口地址",
      ...
    },
    ...
  ]
}
```

#### 测试Python连接
```http
POST /admin/system-config/test-python-connection
Authorization: Bearer <token>

Response:
{
  "code": 200,
  "data": {
    "success": true,
    "endpoint": "http://localhost:5000/check_compliance",
    "responseTime": 125,
    "message": "连接成功",
    "response": {...}
  }
}
```

---

## 🔧 配置说明

### 后端配置

**文件位置**: `backend/src/main/resources/application.yml`

```yaml
# 服务器配置
server:
  port: 8080
  servlet:
    context-path: /api

# 数据源配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/qna_platform
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
    
  # JPA配置
  jpa:
    hibernate:
      ddl-auto: none
    show-sql: false

# JWT配置
jwt:
  secret: your-secret-key-here-please-change-in-production
  expiration: 86400000  # 24小时（毫秒）
  header: Authorization
  prefix: Bearer 

# Flyway配置
flyway:
  enabled: true
  baseline-on-migrate: true
```

### 前端配置

**文件位置**: `frontend/vite.config.js`

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

### Python服务配置

**文件位置**: `compliance-service/app.py`

```python
from flask import Flask, request, jsonify

app = Flask(__name__)

@app.route('/check_compliance', methods=['POST'])
def check_compliance():
    data = request.json
    content = data.get('text', '')
    
    # TODO: 实现你的合规检测逻辑
    # 可以使用大模型API或传统算法
    
    result = {
        'status': 'PASS',  # PASS, VIOLATION
        'riskLevel': 'LOW',  # LOW, MEDIUM, HIGH
        'confidence': 0.95,
        'violations': []
    }
    
    return jsonify(result)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
```

---

## 🚢 部署指南

### 开发环境部署

已在[快速开始](#-快速开始)部分说明。

### 生产环境部署

#### 1. 准备工作

- [ ] 修改 `application.yml` 中的数据库连接信息
- [ ] 修改 `application.yml` 中的JWT密钥（重要！）
- [ ] 修改数据库root密码
- [ ] 配置HTTPS证书（推荐）

#### 2. 后端部署

```bash
cd backend

# 打包
mvn clean package -DskipTests

# 运行
java -jar target/qna-platform-1.0.0.jar

# 或使用 nohup 后台运行
nohup java -jar target/qna-platform-1.0.0.jar > app.log 2>&1 &
```

#### 3. 前端部署

```bash
cd frontend

# 构建生产版本
npm run build

# dist目录中的文件部署到Nginx
```

**Nginx配置示例**:

```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    # 前端静态文件
    location / {
        root /path/to/frontend/dist;
        try_files $uri $uri/ /index.html;
    }
    
    # 后端API代理
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

#### 4. Python服务部署（可选）

```bash
cd compliance-service

# 安装依赖
pip install -r requirements.txt

# 使用 Gunicorn 运行
gunicorn -w 4 -b 0.0.0.0:5000 app:app

# 或使用 Supervisor 管理进程
```

### Docker部署（推荐）

```bash
# TODO: 添加Docker Compose配置
```

---

## ❓ 常见问题

### Q1: 启动后端时报数据库连接错误？

**A**: 
1. 确认MySQL服务已启动
2. 检查 `application.yml` 中的数据库连接信息
3. 确认数据库已创建：`CREATE DATABASE qna_platform`
4. 检查MySQL用户权限

### Q2: 前端启动后无法访问后端API？

**A**:
1. 确认后端已成功启动（访问 http://localhost:8080/api）
2. 检查 `vite.config.js` 中的代理配置
3. 检查浏览器控制台是否有CORS错误

### Q3: 登录后权限丢失？

**A**: 
1. 清除浏览器LocalStorage（F12 → Application → Local Storage）
2. 重新登录
3. 确认后端日志没有错误

### Q4: 如何添加新用户？

**A**:
- 方式1: 使用注册接口
- 方式2: 管理员在"用户管理"页面添加
- 方式3: 直接在数据库 `sys_user` 表插入（需要BCrypt加密密码）

### Q5: Python检测接口连接测试失败？

**A**:
1. 确认Python服务已启动（访问 http://localhost:5000）
2. 在系统配置中修改接口地址
3. 检查防火墙设置
4. 查看Python服务日志

### Q6: 导出功能返回404错误？

**A**:
已修复！确保后端版本为最新（提交记录 `47070ff` 之后）。如果仍有问题：
1. 重启后端服务
2. 清除浏览器缓存
3. 检查后端启动日志，确认拦截器配置正确

### Q7: 如何修改用户的API配额？

**A**:
- 方式1: 管理员在"用户管理"页面修改
- 方式2: 直接修改数据库 `sys_user` 表的 `api_quota` 字段

### Q8: 忘记管理员密码怎么办？

**A**:
直接在数据库中修改密码：
```sql
-- 密码为 admin123 的BCrypt加密结果
UPDATE sys_user 
SET password = '$2a$10$...' 
WHERE username = 'admin';
```

---

## 🔄 更新日志

### Version 2.0.0 (2025-12-09) 🆕

#### 新增功能
- ✅ **个人信息管理** - 用户可查看和修改个人信息、API配额统计、密码修改
- ✅ **管理员导出用户记录** - 管理员可搜索并导出任意用户的聊天记录
- ✅ **平台数据展示（Dashboard）** - 实时展示平台运营数据和统计信息
- ✅ **系统配置管理** - 超管可配置Python检测接口地址和参数

#### 问题修复
- 🐛 修复刷新页面后权限丢失问题
- 🐛 修复反复点击菜单导致空白页面问题
- 🐛 修复快速切换路由的DOM访问错误
- 🐛 修复超管无法访问用户管理页面问题
- 🐛 修复数据导出401错误（JwtInterceptor和PermissionInterceptor配置）

#### 技术改进
- 📈 完善的权限体系（基于注解的权限控制）
- 🔐 双重拦截器配置（JWT认证 + 权限验证）
- 💾 系统配置数据库化存储
- 🎨 精美的UI设计和用户体验优化
- 📝 完善的文档和部署指南

#### 文档更新
- 📚 `NEW_FEATURES_SUMMARY.md` - 新功能开发总结
- 📚 `ALL_ISSUES_FIXED_SUMMARY.md` - 问题修复总结
- 📚 `ROOT_CAUSE_JWT_INTERCEPTOR.md` - JWT拦截器分析
- 📚 更新README.md - 基于项目现状重写

---

### Version 1.0.0 (2024-12-01)

#### 初始版本
- ✅ 用户认证与授权
- ✅ 多会话对话管理
- ✅ API配置管理
- ✅ 数据导出功能
- ✅ 合规检测系统
- ✅ 用户管理和角色权限

---

## 🎯 路线图

### 短期计划（1-2个月）

- [ ] **用户头像上传** - 支持用户自定义头像
- [ ] **批量导出功能** - 管理员批量导出多个用户的记录
- [ ] **图表可视化** - Dashboard添加折线图、柱状图
- [ ] **导出更多格式** - 支持CSV、Excel格式导出
- [ ] **配置导入导出** - 系统配置支持导入导出

### 中期计划（3-6个月）

- [ ] **多租户支持** - 支持多个租户独立使用
- [ ] **消息搜索** - 全文检索历史消息
- [ ] **文件上传** - 支持上传文件到对话
- [ ] **语音输入** - 支持语音输入和识别
- [ ] **Webhook通知** - 合规检测结果推送

### 长期计划（6个月+）

- [ ] **移动端适配** - 响应式设计或原生App
- [ ] **插件系统** - 支持第三方插件扩展
- [ ] **数据分析报表** - 更强大的数据分析能力
- [ ] **自动化测试** - 完善的单元测试和集成测试
- [ ] **性能优化** - 缓存、CDN、分布式部署

---

## 🤝 贡献指南

欢迎贡献代码、报告Bug、提出建议！

### 如何贡献

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

### 代码规范

- 后端：遵循阿里巴巴Java开发规范
- 前端：使用ESLint + Prettier
- 提交信息：遵循 Conventional Commits 规范

### 报告Bug

请在 [GitHub Issues](https://github.com/Sprinkler126/LLMWeb-Demo/issues) 中提交Bug报告，包含：
- 问题描述
- 复现步骤
- 预期行为
- 实际行为
- 环境信息（操作系统、浏览器、版本等）

---

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

---

## 📧 联系方式

- **项目地址**: https://github.com/Sprinkler126/LLMWeb-Demo
- **问题反馈**: [GitHub Issues](https://github.com/Sprinkler126/LLMWeb-Demo/issues)
- **文档**: [docs/](docs/)

---

## 🙏 致谢

感谢以下开源项目：

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue.js](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)
- [MyBatis-Plus](https://baomidou.com/)
- [Hutool](https://hutool.cn/)

---

<div align="center">

**⭐ 如果觉得项目不错，请给个Star支持一下！⭐**

Made with ❤️ by QnA Platform Team

[返回顶部](#qna-platform---智能问答与合规检测平台)

</div>
