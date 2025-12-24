import os

class Config:
    # Flask配置
    HOST = os.environ.get('HOST', '0.0.0.0')
    PORT = int(os.environ.get('PORT', 5000))
    DEBUG = os.environ.get('DEBUG', 'False').lower() == 'true'
    
    # 其他可能的配置项
    SECRET_KEY = os.environ.get('SECRET_KEY', 'your-secret-key')
    
    # 大模型默认配置
    LLM_DEFAULT_URL = os.environ.get('LLM_DEFAULT_URL', 'https://api.example.com/v1/chat/completions')
    LLM_DEFAULT_KEY = os.environ.get('LLM_DEFAULT_KEY', '')
    LLM_DEFAULT_MODEL = os.environ.get('LLM_DEFAULT_MODEL', 'gpt-3.5-turbo')
    LLM_DEFAULT_TIMEOUT = int(os.environ.get('LLM_DEFAULT_TIMEOUT', 30))
    
class DevelopmentConfig(Config):
    DEBUG = True

class ProductionConfig(Config):
    DEBUG = False

# 配置字典
config = {
    'development': DevelopmentConfig,
    'production': ProductionConfig,
    'default': DevelopmentConfig
}
