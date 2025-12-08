# 编译错误修复说明

## 问题描述

在编译 `UserManagementServiceImpl.java` 时遇到以下错误：

```
java: 找不到符号
  符号:   类 UserMapper
  位置: 程序包 com.qna.platform.mapper
```

## 问题原因

`UserManagementServiceImpl.java` 中引用了不存在的 `UserMapper` 类，但实际项目中使用的是 `SysUserMapper` 类。

## 解决方案

### 修改内容

**文件**: `backend/src/main/java/com/qna/platform/service/impl/UserManagementServiceImpl.java`

**修改前**:
```java
import com.qna.platform.mapper.UserMapper;

@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    // ...
}
```

**修改后**:
```java
import com.qna.platform.mapper.SysUserMapper;

@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {
    private final SysUserMapper userMapper;
    private final RoleMapper roleMapper;
    // ...
}
```

## 验证步骤

### 1. 拉取最新代码

```bash
cd D:\JavaBank\LLMWeb-Demo
git pull origin main
```

### 2. 编译项目

在 IDEA 中：
- 右键项目 → `Maven` → `Reload project`
- 或者执行：`mvn clean compile`

### 3. 检查编译结果

编译应该成功，不再出现 "找不到符号 UserMapper" 的错误。

## 相关文件

项目中正确的 Mapper 命名：

```
backend/src/main/java/com/qna/platform/mapper/
├── SysUserMapper.java          ✅ 系统用户
├── RoleMapper.java             ✅ 角色
├── PermissionMapper.java       ✅ 权限
├── RolePermissionMapper.java   ✅ 角色权限关联
├── ApiConfigMapper.java        ✅ API配置
├── ChatSessionMapper.java      ✅ 会话
├── ChatMessageMapper.java      ✅ 消息
├── ComplianceTaskMapper.java   ✅ 合规任务
└── ComplianceResultMapper.java ✅ 合规结果
```

## 最新提交

- **Commit ID**: `6422990`
- **提交信息**: fix: 修复UserManagementServiceImpl引用错误的Mapper
- **修改文件**: `backend/src/main/java/com/qna/platform/service/impl/UserManagementServiceImpl.java`

## 如何避免类似问题

1. **统一命名规范**：所有系统表的 Mapper 都使用 `Sys` 前缀
2. **代码审查**：提交前检查 import 语句
3. **编译验证**：提交前确保项目能成功编译

## 其他编译问题排查

如果您在其他地方遇到类似的 "找不到符号" 错误，请检查：

### 1. Import 语句检查
```bash
# 查找所有 import 语句
grep -r "import com.qna.platform.mapper" backend/src/main/java
```

### 2. 依赖检查
```bash
# 重新加载 Maven 依赖
mvn clean install -DskipTests
```

### 3. IDEA 缓存清理

如果 IDEA 显示错误但代码实际正确：
1. `File` → `Invalidate Caches...`
2. 选择 `Invalidate and Restart`

## 完整的编译和启动流程

### 1. 更新代码
```bash
cd D:\JavaBank\LLMWeb-Demo
git pull origin main
```

### 2. 初始化数据库
使用 Navicat 执行：`sql/schema_complete_v2.sql`

### 3. 编译后端
```bash
cd backend
mvn clean install -DskipTests
```

### 4. 启动后端
```bash
mvn spring-boot:run
```

### 5. 启动前端
```bash
cd ../frontend
npm install
npm run dev
```

### 6. 访问系统
- 前端: http://localhost:3000
- 后端: http://localhost:8080/api
- 默认账户: `admin` / `admin123`

## 帮助

如果仍然遇到编译问题，请检查：
1. JDK 版本是否为 17
2. Maven 版本是否为 3.6+
3. 数据库是否正确初始化
4. `application.yml` 配置是否正确

---

**修复时间**: 2025-12-08  
**最新 Commit**: 6422990  
**状态**: ✅ 已修复并推送到 GitHub
