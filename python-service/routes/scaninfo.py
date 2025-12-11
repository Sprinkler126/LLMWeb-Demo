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
    """
    try:
        data = request.get_json()
        content = data.get('content', '')
        
        if not content:
            return jsonify({
                "error": "内容不能为空"
            }), 400
        
        result = scan_personal_info(content)
        
        return jsonify(result)
    
    except Exception as e:
        logger.error(f"扫描失败: {str(e)}")
        return jsonify({
            "error": str(e)
        }), 500
