"""
Python 合规检测服务
此服务作为独立的微服务运行，提供内容合规性检测功能
"""

from flask import Flask
from flask_cors import CORS
import logging
from config import config
from dotenv import load_dotenv
import os

# 加载.env文件中的环境变量
load_dotenv()

# 导入路由蓝图
from routes.compliance import compliance_bp
from routes.health import health_bp
from routes.scaninfo import scaninfo_bp
from routes.training import training_bp

app = Flask(__name__)
CORS(app)

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# 注册蓝图
app.register_blueprint(compliance_bp, url_prefix='/api/compliance')
app.register_blueprint(health_bp)
app.register_blueprint(scaninfo_bp, url_prefix='/api')
app.register_blueprint(training_bp)

if __name__ == '__main__':
    # 获取配置
    config_name = os.environ.get('FLASK_ENV', 'default')
    current_config = config[config_name]
    
    print("=" * 60)
    print("Python服务启动中...")
    print(f"合规检测接口地址: http://{current_config.HOST}:{current_config.PORT}/api/compliance/check")
    print(f"信息扫描接口地址: http://{current_config.HOST}:{current_config.PORT}/api/scaninfo")
    print(f"模型训练接口地址: http://{current_config.HOST}:{current_config.PORT}/api/training")
    print("=" * 60)
    app.run(host=current_config.HOST, port=current_config.PORT, debug=current_config.DEBUG)