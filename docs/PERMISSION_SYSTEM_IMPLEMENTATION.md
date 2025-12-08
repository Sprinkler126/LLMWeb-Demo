# 权限管理系统实现指南

## 📋 概述

本文档详细说明如何实现完整的权限管理系统，包括：
1. 三级角色系统（超级管理员、管理员、普通用户）
2. 可配置的权限系统
3. 用户管理和角色分配页面
4. Python 合规检测服务
5. 自动合规检测和显示

---

## 🗄️ 步骤 1：数据库升级

### 1.1 执行升级脚本

在 MySQL 中执行 `sql/upgrade_permission_system.sql`：

```bash
# 方法 1：命令行
mysql -u root -p qna_platform < sql/upgrade_permission_system.sql

# 方法 2：MySQL Workbench
# 打开 sql/upgrade_permission_system.sql 文件，点击执行

# 方法 3：IDEA Database 工具
# 右键数据库 -> Run SQL Script -> 选择 upgrade_permission_system.sql
```

### 1.2 验证数据库

```sql
-- 查看新增的表
SHOW TABLES;
-- 应该看到: sys_role, sys_permission, sys_role_permission, sys_operation_log

-- 查看角色数据
SELECT * FROM sys_role;
-- 应该有 3 个角色: SUPER_ADMIN, ADMIN, USER

-- 查看权限数据
SELECT COUNT(*) FROM sys_permission;
-- 应该有 26 个权限

-- 查看用户角色更新
SELECT username, role, role_id FROM sys_user;
-- admin 应该是 SUPER_ADMIN, testuser 应该是 USER
```

---

## 💻 步骤 2：后端实现（已完成部分）

### 2.1 实体类（已创建）

- `SysRole.java` - 角色实体
- `SysPermission.java` - 权限实体
- `SysRolePermission.java` - 角色权限关联实体

### 2.2 需要创建的文件

#### Mapper 层

创建以下 Mapper 接口：

**`RoleMapper.java`**
```java
package com.qna.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qna.platform.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<SysRole> {
    
    /**
     * 根据用户ID查询用户角色
     */
    @Select("SELECT r.* FROM sys_role r " +
            "JOIN sys_user u ON u.role_id = r.id " +
            "WHERE u.id = #{userId}")
    SysRole selectByUserId(Long userId);
    
    /**
     * 查询所有可用角色
     */
    @Select("SELECT * FROM sys_role WHERE status = 1 ORDER BY role_level")
    List<SysRole> selectEnabledRoles();
}
```

**`PermissionMapper.java`**
```java
package com.qna.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qna.platform.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PermissionMapper extends BaseMapper<SysPermission> {
    
    /**
     * 根据角色ID查询权限列表
     */
    @Select("SELECT p.* FROM sys_permission p " +
            "JOIN sys_role_permission rp ON rp.permission_id = p.id " +
            "WHERE rp.role_id = #{roleId} AND p.status = 1")
    List<SysPermission> selectByRoleId(Long roleId);
    
    /**
     * 根据用户ID查询用户所有权限
     */
    @Select("SELECT p.* FROM sys_permission p " +
            "JOIN sys_role_permission rp ON rp.permission_id = p.id " +
            "JOIN sys_user u ON u.role_id = rp.role_id " +
            "WHERE u.id = #{userId} AND p.status = 1")
    List<SysPermission> selectByUserId(Long userId);
    
    /**
     * 查询所有权限（树形结构）
     */
    @Select("SELECT * FROM sys_permission WHERE status = 1 ORDER BY parent_id, sort_order")
    List<SysPermission> selectAllTree();
}
```

**`RolePermissionMapper.java`**
```java
package com.qna.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qna.platform.entity.SysRolePermission;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RolePermissionMapper extends BaseMapper<SysRolePermission> {
    
    /**
     * 删除角色的所有权限
     */
    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId}")
    int deleteByRoleId(Long roleId);
}
```

#### Service 层

**`UserManagementService.java`** - 用户管理服务
```java
package com.qna.platform.service;

import com.qna.platform.entity.SysUser;
import java.util.List;

public interface UserManagementService {
    /**
     * 获取所有用户列表
     */
    List<SysUser> getAllUsers();
    
    /**
     * 为用户分配角色
     */
    void assignRole(Long userId, Long roleId);
    
    /**
     * 启用/禁用用户
     */
    void updateUserStatus(Long userId, Integer status);
    
    /**
     * 更新用户配额
     */
    void updateApiQuota(Long userId, Integer quota);
}
```

**`RoleService.java`** - 角色管理服务
```java
package com.qna.platform.service;

import com.qna.platform.entity.SysPermission;
import com.qna.platform.entity.SysRole;
import java.util.List;

public interface RoleService {
    /**
     * 获取所有角色
     */
    List<SysRole> getAllRoles();
    
    /**
     * 获取角色的权限列表
     */
    List<SysPermission> getRolePermissions(Long roleId);
    
    /**
     * 为角色分配权限
     */
    void assignPermissions(Long roleId, List<Long> permissionIds);
}
```

**`PermissionService.java`** - 权限管理服务
```java
package com.qna.platform.service;

import com.qna.platform.entity.SysPermission;
import java.util.List;

public interface PermissionService {
    /**
     * 获取所有权限（树形结构）
     */
    List<SysPermission> getAllPermissionsTree();
    
    /**
     * 获取用户权限列表
     */
    List<SysPermission> getUserPermissions(Long userId);
    
    /**
     * 检查用户是否有某个权限
     */
    boolean hasPermission(Long userId, String permissionCode);
}
```

---

## 🐍 步骤 3：Python 合规检测服务

### 3.1 服务已实现（默认全部通过）

当前实现在 `compliance-service/app.py`，默认返回所有内容合规。

### 3.2 启动服务

```bash
# 进入目录
cd compliance-service

# 安装依赖
pip install flask flask-cors

# 启动服务
python app.py

# 服务将运行在 http://localhost:5000
```

### 3.3 测试接口

```bash
# 测试合规检测
curl -X POST http://localhost:5000/api/compliance/check \
  -H "Content-Type: application/json" \
  -d '{"content":"这是一条测试消息"}'

# 预期返回
{
  "result": "PASS",
  "risk_level": "LOW",
  "risk_categories": "",
  "confidence_score": 0.99,
  "detail": "内容检测通过，未发现风险"
}
```

### 3.4 生产环境增强

在生产环境中，修改 `check_with_rules()` 函数：

```python
def check_with_rules(content):
    """实际的检测逻辑"""
    # 1. 敏感词检测
    sensitive_words = ["暴力", "色情", "赌博", "毒品"]
    found_words = [word for word in sensitive_words if word in content]
    
    if found_words:
        return {
            "result": "FAIL",
            "risk_level": "HIGH",
            "risk_categories": "敏感词汇",
            "confidence_score": 1.0,
            "detail": f"包含敏感词: {', '.join(found_words)}"
        }
    
    # 2. 可以添加更多检测逻辑
    # - 文本长度检查
    # - 特殊字符检查
    # - 调用第三方API
    # - 使用机器学习模型
    
    return {
        "result": "PASS",
        "risk_level": "LOW",
        "risk_categories": "",
        "confidence_score": 0.99,
        "detail": "内容检测通过"
    }
```

---

## 🔄 步骤 4：后端自动合规检测

### 4.1 修改 ChatService

需要在发送消息时自动调用合规检测：

**位置**: `ChatServiceImpl.java`

**修改 `sendMessage()` 方法**，添加合规检测逻辑：

```java
@Override
public ChatMessageDTO sendMessage(ChatRequestDTO request) {
    // ... 现有代码 ...
    
    // 1. 保存用户消息
    ChatMessage userMessage = new ChatMessage();
    // ... 设置消息属性 ...
    userMessage.setComplianceStatus("UNCHECKED"); // 初始状态
    chatMessageMapper.insert(userMessage);
    
    // 2. 对用户消息进行合规检测
    checkMessageCompliance(userMessage);
    
    // 3. 调用 AI API
    String aiResponse = aiApiClient.callAiApi(apiConfig, messages);
    
    // 4. 保存 AI 回复
    ChatMessage assistantMessage = new ChatMessage();
    // ... 设置消息属性 ...
    assistantMessage.setContent(aiResponse);
    assistantMessage.setComplianceStatus("UNCHECKED");
    chatMessageMapper.insert(assistantMessage);
    
    // 5. 对 AI 回复进行合规检测
    checkMessageCompliance(assistantMessage);
    
    // ... 返回结果 ...
}

/**
 * 检测消息合规性
 */
private void checkMessageCompliance(ChatMessage message) {
    try {
        // 调用 Python 合规检测服务
        ComplianceCheckDTO checkDTO = new ComplianceCheckDTO();
        checkDTO.setContent(message.getContent());
        
        // 发送 HTTP 请求到 Python 服务
        String checkUrl = "http://localhost:5000/api/compliance/check";
        // 使用 RestTemplate 或 OkHttp 发送请求
        
        // 解析响应并更新消息
        // result: {result: "PASS/FAIL", risk_level: "LOW/MEDIUM/HIGH", ...}
        
        message.setComplianceStatus(result.get("result")); // PASS 或 FAIL
        message.setComplianceResult(JSONUtil.toJsonStr(result));
        chatMessageMapper.updateById(message);
        
    } catch (Exception e) {
        // 检测失败时，标记为未检测
        message.setComplianceStatus("UNCHECKED");
        chatMessageMapper.updateById(message);
        log.error("合规检测失败", e);
    }
}
```

### 4.2 添加 HTTP 客户端工具

创建 `ComplianceClient.java`：

```java
package com.qna.platform.util;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ComplianceClient {
    
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final OkHttpClient client = new OkHttpClient();
    private final String checkUrl = "http://localhost:5000/api/compliance/check";
    
    /**
     * 调用合规检测服务
     */
    public JSONObject checkContent(String content) throws IOException {
        JSONObject requestBody = new JSONObject();
        requestBody.set("content", content);
        
        RequestBody body = RequestBody.create(requestBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(checkUrl)
                .post(body)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("检测失败: " + response.code());
            }
            
            String responseBody = response.body().string();
            return JSONUtil.parseObj(responseBody);
        }
    }
}
```

---

## 🎨 步骤 5：前端实现

### 5.1 聊天界面显示合规状态

修改 `Chat.vue`，在消息气泡旁显示合规标签：

```vue
<template>
  <!-- 消息气泡 -->
  <div :class="['message-bubble', message.role]">
    <div class="message-role">
      {{ message.role === 'user' ? '你' : 'AI助手' }}
      
      <!-- 合规状态标签 -->
      <el-tag 
        v-if="message.complianceStatus === 'PASS'" 
        type="success" 
        size="small"
        class="compliance-tag">
        ✓ 合规
      </el-tag>
      
      <el-tag 
        v-else-if="message.complianceStatus === 'FAIL'" 
        type="danger" 
        size="small"
        class="compliance-tag">
        ⚠ 风险
      </el-tag>
      
      <el-tag 
        v-else 
        type="info" 
        size="small"
        class="compliance-tag">
        ○ 未检测
      </el-tag>
    </div>
    
    <div class="message-content" v-html="renderMarkdown(message.content)"></div>
    
    <!-- 显示风险详情（如果有） -->
    <div v-if="message.complianceResult && message.complianceStatus === 'FAIL'" 
         class="compliance-detail">
      <el-alert 
        type="warning" 
        :closable="false"
        show-icon>
        <template #title>
          风险提示：{{ JSON.parse(message.complianceResult).detail }}
        </template>
      </el-alert>
    </div>
  </div>
</template>

<style scoped>
.compliance-tag {
  margin-left: 10px;
  font-size: 11px;
}

.compliance-detail {
  margin-top: 10px;
  padding: 8px;
  background: rgba(255, 193, 7, 0.1);
  border-radius: 4px;
}
</style>
```

### 5.2 用户管理页面

创建 `UserManagement.vue`：

```vue
<template>
  <div class="user-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            新增用户
          </el-button>
        </div>
      </template>
      
      <el-table :data="users" stripe>
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="nickname" label="昵称" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column label="角色">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)">
              {{ getRoleLabel(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="apiQuota" label="API配额" />
        <el-table-column label="状态">
          <template #default="{ row }">
            <el-switch 
              v-model="row.status" 
              :active-value="1" 
              :inactive-value="0"
              @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300">
          <template #default="{ row }">
            <el-button 
              size="small" 
              @click="handleAssignRole(row)">
              分配角色
            </el-button>
            <el-button 
              size="small" 
              type="primary"
              @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button 
              size="small" 
              type="danger"
              @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 角色分配对话框 -->
    <el-dialog v-model="roleDialogVisible" title="分配角色">
      <el-form>
        <el-form-item label="选择角色">
          <el-select v-model="selectedRoleId" placeholder="请选择角色">
            <el-option
              v-for="role in roles"
              :key="role.id"
              :label="role.roleName"
              :value="role.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAssignRole">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
// import { getAllUsers, assignRole, updateUserStatus } from '@/api/admin'

const users = ref([])
const roles = ref([])
const roleDialogVisible = ref(false)
const selectedUser = ref(null)
const selectedRoleId = ref(null)

// ... 实现各种方法 ...
</script>
```

---

## 📝 完整文件清单

### 需要创建的文件（后端）

1. ✅ `SysRole.java`
2. ✅ `SysPermission.java`
3. ✅ `SysRolePermission.java`
4. ⏳ `RoleMapper.java`
5. ⏳ `PermissionMapper.java`
6. ⏳ `RolePermissionMapper.java`
7. ⏳ `UserManagementService.java`
8. ⏳ `RoleService.java`
9. ⏳ `PermissionService.java`
10. ⏳ `UserManagementController.java`
11. ⏳ `RoleController.java`
12. ⏳ `ComplianceClient.java`

### 需要创建的文件（前端）

1. ⏳ `UserManagement.vue`
2. ⏳ `RolePermission.vue`
3. ⏳ 修改 `Chat.vue`（添加合规标签）
4. ⏳ `admin.js`（管理 API）

---

## 🚀 快速开始

### 1. 数据库升级
```bash
mysql -u root -p qna_platform < sql/upgrade_permission_system.sql
```

### 2. 启动 Python 服务
```bash
cd compliance-service
python app.py
```

### 3. 启动后端（IDEA）
运行 `QnaPlatformApplication.java`

### 4. 启动前端
```bash
cd frontend
npm run dev
```

### 5. 测试
- 访问：http://localhost:3000
- 登录：admin / admin123
- 测试对话，查看合规标签

---

**由于篇幅限制，完整代码将在后续回复中提供。**

继续？
