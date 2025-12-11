# 合规检测服务

这是一个基于Python Flask的合规检测微服务，用于检测文本内容是否符合规定标准。

## 功能特性

- 敏感词过滤检测
- 可扩展的大模型检测接口
- RESTful API设计
- 易于配置和部署
- 模块化架构，便于扩展更多接口
- 支持多种检测模式（宽松、默认、严格）

## 快速开始

### 环境要求

- Python 3.7+
- pip

### 安装依赖

```bash
pip install -r requirements.txt
```

### 配置服务

可以通过以下方式配置服务：

1. 复制 [.env.example](file:///d:/JavaBank/LLMWeb-Demo/compliance-service/.env.example) 文件为 `.env` 并修改其中的配置项：

```bash
cp .env.example .env
```

2. 或者直接设置环境变量：

```bash
export HOST=0.0.0.0
export PORT=5000
export DEBUG=False
```

### 启动服务

```bash
python app.py
```

## 配置选项

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| HOST | 服务监听的主机地址 | 0.0.0.0 |
| PORT | 服务监听的端口号 | 5000 |
| DEBUG | 是否开启调试模式 | False |

## API接口

### 合规检测接口

```
POST /api/compliance/check
```

**请求体：**

```json
{
  "content": "待检测的文本内容",
  "mode": "moderate"  // 可选: "loose"(宽松), "moderate"(默认), "strict"(严格)
}
```

**mode说明：**
- `loose`(宽松模式): 只进行敏感词检查
- `moderate`(默认模式): 先进行敏感词检查，如果发现敏感词则再使用大模型进行二次检查
- `strict`(严格模式): 同时进行敏感词检查和大模型检查，综合判断结果

**响应：**

```json
{
  "result": "PASS",
  "risk_level": "LOW",
  "risk_categories": "",
  "confidence_score": 0.95,
  "detail": "内容正常，未发现违规"
}
```

### 健康检查接口

```
GET /health
```

## 项目结构

```
compliance-service/
├── app.py                 # 应用主文件
├── config.py              # 配置文件
├── requirements.txt       # 依赖列表
├── .env.example           # 环境变量示例
├── Dockerfile             # Docker配置
├── README.md              # 说明文档
├── Badwords.txt           # 敏感词库
├── routes/                # 路由模块
│   ├── __init__.py
│   ├── compliance.py      # 合规检测相关接口
│   └── health.py          # 健康检查接口
└── services/              # 业务逻辑模块
    ├── __init__.py
    └── compliance_service.py  # 合规检测服务实现
```

## 添加新接口

要在项目中添加新接口，请按照以下步骤操作：

1. 在 `routes/` 目录下创建新的路由文件（如 `new_feature.py`）
2. 创建蓝图并定义路由：

```python
from flask import Blueprint, request, jsonify

new_feature_bp = Blueprint('new_feature', __name__)

@new_feature_bp.route('/path', methods=['GET', 'POST'])
def new_function():
    # 实现新功能
    return jsonify({"result": "success"})
```

3. 在 [app.py](file:///d:/JavaBank/LLMWeb-Demo/compliance-service/app.py) 中注册新蓝图：

```python
from routes.new_feature import new_feature_bp
app.register_blueprint(new_feature_bp, url_prefix='/api/new-feature')
```

4. 如果需要业务逻辑，在 `services/` 目录下创建相应的服务文件

## Docker部署

构建Docker镜像：

```bash
docker build -t compliance-service .
```

运行容器：

```bash
docker run -p 5000:5000 compliance-service
```

可以通过环境变量覆盖默认配置：

```bash
docker run -p 5000:5000 -e PORT=8080 -e HOST=0.0.0.0 compliance-service
```