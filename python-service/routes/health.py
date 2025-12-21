"""
健康检查路由
"""
from flask import Blueprint, jsonify

health_bp = Blueprint('health', __name__)


@health_bp.route('/health', methods=['GET'])
def health_check():
    """
    健康检查接口
    """
    return jsonify({
        'code': 200,
        'message': 'Python服务运行正常',
        'data': {
            'status': 'healthy',
            'services': ['compliance', 'scaninfo', 'training']
        }
    })
