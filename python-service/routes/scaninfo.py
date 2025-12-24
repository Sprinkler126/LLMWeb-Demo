from flask import Blueprint, request, jsonify
import logging
import sys
import os
# Add the parent directory to the Python path
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from services.scaninfo_service import scan_personal_info

logger = logging.getLogger(__name__)

# 创建蓝图
scaninfo_bp = Blueprint('scaninfo', __name__)

@scaninfo_bp.route('/scaninfo', methods=['POST'])
def scan_info():
    """
    扫描文本中的个人敏感信息（身份证号和手机号）
    
    支持传入参数:
    - content: 待扫描的内容（必需）
    - scan_mode: 扫描模式（可选）
        - 'rules': 只使用正则规则扫描（默认）
        - 'llm': 只使用大模型扫描
        - 'both': 同时使用两种方式，结果合并
    - llm_config: 大模型配置（可选）
        - url: API地址
        - key: API密钥
        - model: 模型名称
        - timeout: 超时时间
    """
    try:
        data = request.get_json()
        content = data.get('content', '')
        scan_mode = data.get('scan_mode', 'rules')  # 默认为正则规则扫描
        llm_config = data.get('llm_config', {})  # 大模型配置
        
        if not content:
            return jsonify({
                "error": "内容不能为空"
            }), 400
        
        logger.info(f"收到扫描请求，内容长度: {len(content)}, 扫描模式: {scan_mode}")
        
        result = scan_personal_info(content, scan_mode, llm_config)
        
        return jsonify(result)
    
    except Exception as e:
        logger.error(f"扫描失败: {str(e)}")
        return jsonify({
            "error": str(e)
        }), 500
