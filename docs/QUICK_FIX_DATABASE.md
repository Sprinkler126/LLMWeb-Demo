# 快速修复数据库连接问题

## 🚨 当前问题

你在 IDEA 中运行 Spring Boot，但遇到错误：
```
Access denied for user 'root'@'172.21.14.111' (using password: YES)
```

## ⚡ 快速解决方案（3选1）

---

### 方案 A：连接到已有的 MySQL（推荐）

如果你电脑上已经安装了 MySQL：

#### 1. 找到你的 MySQL 密码

打开 IDEA Terminal，尝试连接：

**Windows:**
```cmd
mysql -u root -p
输入你的MySQL密码
```

**macOS/Linux:**
```bash
mysql -u root -p
输入你的MySQL密码
```

#### 2. 创建数据库

在 MySQL 命令行中执行：

```sql
CREATE DATABASE qna_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE qna_platform;
```

#### 3. 导入数据库脚本

**方法 1 - 在 MySQL 命令行中：**
```sql
SOURCE D:/JavaBank/LLMWeb-Demo/sql/schema.sql;
-- 替换为你的实际路径
```

**方法 2 - 使用命令行：**
```bash
mysql -u root -p qna_platform < D:/JavaBank/LLMWeb-Demo/sql/schema.sql
```

#### 4. 修改 application.yml

修改 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/qna_platform?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: 你的实际MySQL密码  # ← 改这里！
```

#### 5. 重启 IDEA 中的应用

点击停止按钮，然后重新运行 `QnaPlatformApplication`。

---

### 方案 B：安装 MySQL（如果没有安装）

#### Windows 用户：

1. **下载 MySQL**
   - 访问：https://dev.mysql.com/downloads/installer/
   - 下载 `mysql-installer-community-8.0.x.msi`

2. **安装步骤**
   - 双击安装程序
   - 选择 "Server only" 或 "Developer Default"
   - 设置 root 密码为：`root123456`（或记住你设置的密码）
   - 端口保持：`3306`
   - 完成安装

3. **验证安装**
   ```cmd
   mysql -u root -proot123456
   ```

4. **执行方案 A 的步骤 2-5**

#### macOS 用户：

```bash
# 使用 Homebrew 安装
brew install mysql
brew services start mysql

# 设置密码（如果需要）
mysql_secure_installation
# 按提示设置密码为 root123456

# 验证
mysql -u root -proot123456
```

然后执行方案 A 的步骤 2-5。

---

### 方案 C：使用 Docker 启动 MySQL（最简单）

#### 前提：已安装 Docker Desktop

**1. 启动 MySQL 容器**

在 IDEA Terminal 中执行：

```bash
# Windows PowerShell 或 Git Bash
cd D:\JavaBank\LLMWeb-Demo

# 启动 MySQL
docker run -d ^
  --name qna-mysql ^
  -e MYSQL_ROOT_PASSWORD=root123456 ^
  -e MYSQL_DATABASE=qna_platform ^
  -p 3306:3306 ^
  -v "%cd%/sql/schema.sql:/docker-entrypoint-initdb.d/schema.sql" ^
  mysql:8.0

# Linux/macOS
docker run -d \
  --name qna-mysql \
  -e MYSQL_ROOT_PASSWORD=root123456 \
  -e MYSQL_DATABASE=qna_platform \
  -p 3306:3306 \
  -v "$(pwd)/sql/schema.sql:/docker-entrypoint-initdb.d/schema.sql" \
  mysql:8.0
```

**2. 等待 MySQL 启动**

```bash
# 查看日志，等待 "ready for connections" 出现
docker logs -f qna-mysql
```

**3. 验证数据库**

```bash
docker exec -it qna-mysql mysql -uroot -proot123456 -e "SHOW DATABASES;"
```

**4. 重启 IDEA 应用**

---

## 🔍 如何判断哪个方案适合我？

### 检查 MySQL 是否已安装

在 IDEA Terminal 中执行：

```bash
# Windows
mysql --version

# 如果显示版本号，说明已安装 → 选方案 A
# 如果提示 "不是内部或外部命令"，说明未安装 → 选方案 B 或 C
```

### 检查 Docker 是否可用

```bash
docker --version

# 如果显示版本号 → 可以选方案 C
# 如果提示错误 → 选方案 A 或 B
```

---

## ✅ 验证步骤

执行完上述任一方案后，验证是否成功：

### 1. 测试 MySQL 连接

```bash
mysql -h localhost -P 3306 -u root -proot123456 -e "USE qna_platform; SHOW TABLES;"
```

**预期输出：**
```
+------------------------+
| Tables_in_qna_platform |
+------------------------+
| api_call_log           |
| api_config             |
| chat_message           |
| chat_session           |
| compliance_result      |
| compliance_task        |
| sys_user               |
| system_config          |
+------------------------+
```

### 2. 检查数据

```bash
mysql -h localhost -P 3306 -u root -proot123456 -e "SELECT username, role FROM qna_platform.sys_user;"
```

**预期输出：**
```
+----------+-------+
| username | role  |
+----------+-------+
| admin    | ADMIN |
| testuser | USER  |
+----------+-------+
```

### 3. 启动应用

在 IDEA 中：
1. 打开 `QnaPlatformApplication.java`
2. 点击绿色运行按钮
3. 查看控制台日志

**成功标志：**
```
HikariPool-1 - Starting...
HikariPool-1 - Start completed.
Started QnaPlatformApplication in 3.456 seconds
```

**访问测试：**
- 后端 API：http://localhost:8080/api
- Swagger 文档：http://localhost:8080/api/swagger-ui.html

---

## 🐛 常见问题排查

### 问题 1：端口 3306 被占用

**错误信息：**
```
Bind for 0.0.0.0:3306 failed: port is already allocated
```

**解决方法：**

```bash
# 查看占用 3306 的进程
netstat -ano | findstr :3306  # Windows
lsof -i :3306                  # macOS/Linux

# 停止冲突的 MySQL
net stop MySQL80              # Windows
brew services stop mysql      # macOS
sudo systemctl stop mysql     # Linux

# 或者修改端口
# 在 application.yml 中改为：
spring:
  datasource:
    url: jdbc:mysql://localhost:3307/qna_platform...
```

### 问题 2：找不到数据库

**错误信息：**
```
Unknown database 'qna_platform'
```

**解决方法：**

```sql
-- 手动创建数据库
mysql -u root -p
CREATE DATABASE qna_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE qna_platform;
SOURCE /path/to/sql/schema.sql;
EXIT;
```

### 问题 3：表不存在

**错误信息：**
```
Table 'qna_platform.sys_user' doesn't exist
```

**解决方法：**

```bash
# 重新导入 SQL 脚本
mysql -u root -proot123456 qna_platform < D:/JavaBank/LLMWeb-Demo/sql/schema.sql
```

---

## 📞 需要更多帮助？

### 提供以下信息：

1. **操作系统**
   ```bash
   # Windows
   systeminfo | findstr /B /C:"OS Name"
   
   # macOS
   sw_vers
   
   # Linux
   cat /etc/os-release
   ```

2. **MySQL 状态**
   ```bash
   mysql --version
   mysql -u root -p -e "SELECT VERSION();"
   ```

3. **完整错误日志**
   复制 IDEA 控制台的完整堆栈跟踪。

4. **application.yml 配置**
   ```yaml
   spring:
     datasource:
       url: xxx
       username: xxx
       password: [已隐藏]
   ```

---

## 🎯 推荐方案总结

| 情况 | 推荐方案 | 时间 | 难度 |
|------|----------|------|------|
| **已安装 MySQL** | 方案 A | 5分钟 | ⭐ |
| **未安装 MySQL，有 Docker** | 方案 C | 3分钟 | ⭐ |
| **未安装 MySQL，无 Docker** | 方案 B | 10分钟 | ⭐⭐ |

---

**祝你顺利解决问题！如果还有疑问，随时提问。** 🚀
