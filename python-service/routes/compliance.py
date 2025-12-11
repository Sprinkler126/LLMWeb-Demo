from flask import Blueprint, request, jsonify
import logging
import sys
import os
# Add the parent directory to the Python path
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from services.compliance_service import check_compliance

logger = logging.getLogger(__name__)

# 创建蓝图
compliance_bp = Blueprint('compliance', __name__)

@compliance_bp.route('/check', methods=['POST'])
def check_compliance_route():
    """
    合规检测接口
    支持传入mode参数设置检查形式:
    - loose (宽松): 只检查敏感词
    - strict (严格): 先检查敏感词，再用大模型检查
    - moderate (默认): 先检查敏感词，如果有敏感词再用大模型二次检查
    """
    try:
        data = request.get_json()
        content = data.get('content', '')
        mode = data.get('mode', 'moderate')  # 默认为适度检查
        
        if not content:
            return jsonify({
                "error": "内容不能为空"
            }), 400
        
        logger.info(f"收到检测请求，内容长度: {len(content)}, 检查模式: {mode}")
        
        # 调用服务层函数执行检查
        result = check_compliance(content, mode)
        
        logger.info(f"检测完成，结果: {result['result']}")
        
        return jsonify(result)
    
    except Exception as e:
        logger.error(f"检测失败: {str(e)}")
        return jsonify({
            "error": str(e)
        }), 500