# 数据库配置指南

## 问题诊断

如果遇到以下错误：
```
java.sql.SQLException: Access denied for user 'root'@'172.21.14.111' (using password: YES)
```

说明 Spring Boot 应用无法连接到 MySQL 数据库。

## 解决方案

### 方案 1：使用 Docker Compose（推荐，最简单）

#### 前提条件
确保已安装 Docker 和 Docker Compose。

#### 步骤

1. **启动 MySQL 服务**
   ```bash
   cd /home/user/webapp
   docker-compose up -d mysql
   ```

2. **等待 MySQL 初始化**
   ```bash
   # 查看日志，等待 MySQL 准备就绪
   docker-compose logs -f mysql
   
   # 看到以下信息表示成功：
   # "ready for connections. Version: '8.0.x'"
   ```

3. **验证数据库**
   ```bash
   # 连接到 MySQL 容器
   docker exec -it qna-mysql mysql -uroot -proot123456
   
   # 在 MySQL 命令行中执行：
   SHOW DATABASES;
   USE qna_platform;
   SHOW TABLES;
   ```

4. **启动后端应用**
   ```bash
   cd backend
   mvn spring-boot:run
   ```

#### 完整服务启动
```bash
# 启动所有服务（MySQL + 后端 + 前端）
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

---

### 方案 2：本地安装 MySQL

#### Windows

1. **下载 MySQL**
   - 访问：https://dev.mysql.com/downloads/mysql/
   - 下载 MySQL 8.0 Community Server

2. **安装 MySQL**
   - 运行安装程序
   - 设置 root 密码为：`root123456`
   - 端口保持默认：`3306`

3. **创建数据库**
   ```sql
   -- 打开 MySQL Workbench 或命令行
   CREATE DATABASE qna_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   
   -- 导入初始化脚本
   USE qna_platform;
   SOURCE D:/path/to/LLMWeb-Demo/sql/schema.sql;
   ```

#### macOS

1. **使用 Homebrew 安装**
   ```bash
   brew install mysql@8.0
   brew services start mysql@8.0
   ```

2. **设置 root 密码**
   ```bash
   mysql_secure_installation
   # 设置密码为：root123456
   ```

3. **创建数据库**
   ```bash
   mysql -uroot -proot123456
   ```
   ```sql
   CREATE DATABASE qna_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   USE qna_platform;
   SOURCE /path/to/LLMWeb-Demo/sql/schema.sql;
   ```

#### Linux (Ubuntu/Debian)

1. **安装 MySQL**
   ```bash
   sudo apt update
   sudo apt install mysql-server
   sudo systemctl start mysql
   sudo systemctl enable mysql
   ```

2. **设置 root 密码**
   ```bash
   sudo mysql
   ```
   ```sql
   ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'root123456';
   FLUSH PRIVILEGES;
   EXIT;
   ```

3. **创建数据库**
   ```bash
   mysql -uroot -proot123456 < /home/user/webapp/sql/schema.sql
   ```

---

### 方案 3：使用已有的 MySQL 实例

如果你已经有运行中的 MySQL 实例：

#### 步骤 1：检查 MySQL 连接

```bash
# 测试连接（替换为你的实际信息）
mysql -h localhost -P 3306 -u root -p
# 输入密码
```

#### 步骤 2：创建数据库和表

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS qna_platform 
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE qna_platform;

-- 导入 schema.sql
SOURCE /path/to/webapp/sql/schema.sql;

-- 或者直接复制粘贴 schema.sql 的内容执行
```

#### 步骤 3：配置用户权限

```sql
-- 如果需要从其他机器访问（远程连接）
CREATE USER 'root'@'%' IDENTIFIED BY 'root123456';
GRANT ALL PRIVILEGES ON qna_platform.* TO 'root'@'%';
FLUSH PRIVILEGES;

-- 查看用户权限
SELECT user, host FROM mysql.user;
```

#### 步骤 4：修改 application.yml

如果你的 MySQL 配置不同，修改 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://your-host:your-port/qna_platform?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: your-username
    password: your-password
```

---

## 常见问题

### Q1: 密码错误

**错误信息：**
```
Access denied for user 'root'@'localhost' (using password: YES)
```

**解决方法：**
1. 确认密码是否正确
2. 重置 MySQL root 密码：
   ```bash
   # Windows
   mysqladmin -u root -p password "root123456"
   
   # Linux/macOS
   sudo mysqladmin -u root -p password "root123456"
   ```

### Q2: 数据库不存在

**错误信息：**
```
Unknown database 'qna_platform'
```

**解决方法：**
```sql
CREATE DATABASE qna_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Q3: 远程连接被拒绝

**错误信息：**
```
Access denied for user 'root'@'172.x.x.x'
```

**解决方法：**
```sql
-- 允许 root 从任何主机连接
CREATE USER 'root'@'%' IDENTIFIED BY 'root123456';
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;
```

**或者编辑 MySQL 配置文件：**
```bash
# Linux: /etc/mysql/mysql.conf.d/mysqld.cnf
# Windows: C:\ProgramData\MySQL\MySQL Server 8.0\my.ini

# 注释掉 bind-address
# bind-address = 127.0.0.1

# 重启 MySQL
sudo systemctl restart mysql  # Linux
net stop MySQL80 && net start MySQL80  # Windows (管理员)
```

### Q4: 端口被占用

**错误信息：**
```
Address already in use: 3306
```

**解决方法：**
```bash
# 查看占用 3306 端口的进程
# Windows
netstat -ano | findstr :3306

# Linux/macOS
lsof -i :3306

# 修改 application.yml 使用不同端口
spring:
  datasource:
    url: jdbc:mysql://localhost:3307/qna_platform...
```

---

## 验证步骤

### 1. 检查 MySQL 服务状态

```bash
# Docker
docker ps | grep mysql

# Linux
sudo systemctl status mysql

# Windows
net start | findstr MySQL

# macOS
brew services list | grep mysql
```

### 2. 测试数据库连接

```bash
mysql -h localhost -P 3306 -u root -proot123456 -e "SHOW DATABASES;"
```

### 3. 验证表结构

```bash
mysql -h localhost -P 3306 -u root -proot123456 qna_platform -e "SHOW TABLES;"
```

应该看到以下表：
- `sys_user`
- `api_config`
- `chat_session`
- `chat_message`
- `compliance_task`
- `compliance_result`
- `api_call_log`
- `system_config`

### 4. 测试后端连接

启动后端后，查看日志：
```bash
cd backend
mvn spring-boot:run
```

成功的日志应该包含：
```
HikariPool-1 - Starting...
HikariPool-1 - Start completed.
Started QnaPlatformApplication in X.XXX seconds
```

---

## 快速诊断脚本

创建一个快速诊断脚本：

```bash
#!/bin/bash
# mysql-check.sh

echo "=== MySQL 连接诊断 ==="

# 1. 检查 MySQL 服务
echo "1. 检查 MySQL 服务..."
if command -v mysql &> /dev/null; then
    echo "✓ MySQL 客户端已安装"
else
    echo "✗ MySQL 客户端未安装"
fi

# 2. 测试连接
echo "2. 测试数据库连接..."
mysql -h localhost -P 3306 -u root -proot123456 -e "SELECT 1" &> /dev/null
if [ $? -eq 0 ]; then
    echo "✓ 数据库连接成功"
else
    echo "✗ 数据库连接失败"
fi

# 3. 检查数据库
echo "3. 检查数据库..."
mysql -h localhost -P 3306 -u root -proot123456 -e "USE qna_platform; SHOW TABLES;" &> /dev/null
if [ $? -eq 0 ]; then
    echo "✓ 数据库 qna_platform 存在"
else
    echo "✗ 数据库 qna_platform 不存在"
fi

echo "=== 诊断完成 ==="
```

运行：
```bash
chmod +x mysql-check.sh
./mysql-check.sh
```

---

## 推荐配置

### 开发环境
- **方案**：Docker Compose
- **优点**：快速启动、环境隔离、无需安装
- **命令**：`docker-compose up -d`

### 生产环境
- **方案**：独立 MySQL 服务器
- **优点**：性能更好、便于管理、数据持久化
- **配置**：使用专用数据库服务器

---

## 需要帮助？

如果以上方案都无法解决问题，请提供以下信息：

1. 操作系统版本
2. MySQL 安装方式（Docker/本地）
3. 完整的错误日志
4. `application.yml` 配置（隐藏密码）
5. MySQL 版本：`mysql --version`

---

**更新时间**：2025-12-08  
**文档版本**：v1.0
