# 部署指南

## 快速部署（推荐）

### 使用启动脚本

```bash
./start.sh
```

脚本会自动：
1. 检查Docker环境
2. 询问是否启动合规检测服务
3. 启动所有必要的服务
4. 显示访问地址和默认账号

### 手动Docker部署

```bash
# 不包含合规检测服务
docker-compose up -d

# 包含合规检测服务
docker-compose --profile with-compliance up -d
```

## 本地开发部署

### 1. 数据库准备

安装MySQL 8.0并创建数据库：

```bash
mysql -u root -p

CREATE DATABASE qna_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE qna_platform;
SOURCE sql/schema.sql;
```

### 2. 后端启动

```bash
cd backend

# 修改application.yml中的数据库配置
# 然后启动
mvn spring-boot:run
```

后端将在 http://localhost:8080 启动

### 3. 前端启动

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端将在 http://localhost:3000 启动

### 4. 合规检测服务启动（可选）

```bash
cd compliance-service

# 创建虚拟环境
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate

# 安装依赖
pip install -r requirements.txt

# 启动服务
python app.py
```

服务将在 http://localhost:5000 启动

## 生产环境部署

### 1. 环境准备

- 服务器: Linux (Ubuntu 20.04+ 推荐)
- Docker: 20.10+
- Docker Compose: 2.0+
- 内存: 至少 4GB
- 硬盘: 至少 20GB

### 2. 配置修改

#### 修改数据库密码

编辑 `docker-compose.yml`:

```yaml
services:
  mysql:
    environment:
      MYSQL_ROOT_PASSWORD: your_secure_password
```

编辑 `backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    password: your_secure_password
```

#### 修改JWT密钥

编辑 `backend/src/main/resources/application.yml`:

```yaml
jwt:
  secret: your_very_long_and_secure_secret_key_here
  expiration: 86400000
```

#### 配置域名和反向代理

如果使用域名，建议配置Nginx反向代理：

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端
    location / {
        proxy_pass http://localhost:3000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # 后端API
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### 3. 启动服务

```bash
# 拉取代码
git clone <your-repo-url>
cd qna-platform

# 启动服务
docker-compose up -d

# 查看日志
docker-compose logs -f
```

### 4. 服务管理

```bash
# 查看状态
docker-compose ps

# 停止服务
docker-compose stop

# 启动服务
docker-compose start

# 重启服务
docker-compose restart

# 删除服务（保留数据）
docker-compose down

# 删除服务和数据
docker-compose down -v
```

## 数据备份

### 备份数据库

```bash
# 导出数据
docker exec qna-mysql mysqldump -u root -proot123456 qna_platform > backup_$(date +%Y%m%d).sql

# 恢复数据
docker exec -i qna-mysql mysql -u root -proot123456 qna_platform < backup_20231208.sql
```

### 备份文件

```bash
# 备份整个项目
tar -czf qna-platform-backup-$(date +%Y%m%d).tar.gz /path/to/qna-platform
```

## 性能优化

### 1. 数据库优化

```sql
-- 为常用查询添加索引
CREATE INDEX idx_message_created ON chat_message(created_time);
CREATE INDEX idx_session_user ON chat_session(user_id, created_time);

-- 定期清理旧数据
DELETE FROM chat_message WHERE created_time < DATE_SUB(NOW(), INTERVAL 90 DAY);
```

### 2. 应用优化

在 `application.yml` 中调整连接池大小：

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
```

### 3. 前端优化

构建生产版本时启用压缩：

```bash
cd frontend
npm run build
```

## 监控和日志

### 查看应用日志

```bash
# 查看后端日志
docker-compose logs -f backend

# 查看前端日志
docker-compose logs -f frontend

# 查看数据库日志
docker-compose logs -f mysql
```

### 日志文件位置

- 后端日志: `backend/logs/qna-platform.log`
- 数据库日志: Docker容器内 `/var/log/mysql/`

## 故障排查

### 问题1: 数据库连接失败

检查：
1. MySQL容器是否正常运行: `docker ps`
2. 数据库密码是否正确
3. 网络连接是否正常: `docker network ls`

### 问题2: 前端无法访问后端

检查：
1. 后端是否正常运行: `curl http://localhost:8080/api`
2. 跨域配置是否正确
3. Nginx代理配置是否正确

### 问题3: API调用失败

检查：
1. API密钥是否正确
2. 网络是否可以访问外部API
3. API配额是否用完
4. 查看后端日志获取详细错误信息

## 安全建议

1. **修改默认密码**: 部署后立即修改admin账号密码
2. **使用HTTPS**: 生产环境务必使用HTTPS
3. **限制数据库访问**: 不要暴露3306端口到公网
4. **定期更新**: 及时更新依赖包和Docker镜像
5. **备份数据**: 定期备份数据库和重要文件
6. **监控日志**: 定期检查日志文件，发现异常及时处理

## 升级指南

### 升级步骤

```bash
# 1. 备份数据
docker exec qna-mysql mysqldump -u root -proot123456 qna_platform > backup.sql

# 2. 拉取最新代码
git pull origin main

# 3. 重新构建并启动
docker-compose down
docker-compose build --no-cache
docker-compose up -d

# 4. 检查服务状态
docker-compose ps
docker-compose logs -f
```

## 联系支持

如遇到问题，请：
1. 查看日志文件
2. 检查配置文件
3. 参考README.md文档
4. 提交Issue到GitHub仓库

---

**祝部署顺利！** 🚀
