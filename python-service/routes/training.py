"""
训练任务路由
提供模型训练相关的API接口
"""
from flask import Blueprint, request, jsonify
import logging

from services.training_service import TrainingService

# 配置日志
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# 创建蓝图
training_bp = Blueprint('training', __name__, url_prefix='/api/training')

# 训练服务实例
training_service = TrainingService()


@training_bp.route('/start', methods=['POST'])
def start_training():
    """
    启动训练任务
    """
    try:
        data = request.get_json()
        task_id = data.get('taskId')
        model_type = data.get('modelType')
        dataset_path = data.get('datasetPath')
        model_config = data.get('modelConfig')
        callback_url = data.get('callbackUrl')
        
        logger.info(f"收到训练请求 - 任务ID: {task_id}, 模型类型: {model_type}")
        
        # 启动训练（异步）
        training_service.start_training(
            task_id=task_id,
            model_type=model_type,
            dataset_path=dataset_path,
            model_config=model_config,
            callback_url=callback_url
        )
        
        return jsonify({
            'code': 200,
            'message': '训练任务已启动',
            'data': {'taskId': task_id}
        })
        
    except Exception as e:
        logger.error(f"启动训练任务失败: {str(e)}", exc_info=True)
        return jsonify({
            'code': 500,
            'message': f'启动训练失败: {str(e)}'
        }), 500


@training_bp.route('/stop/<int:task_id>', methods=['POST'])
def stop_training(task_id):
    """
    停止训练任务
    """
    try:
        logger.info(f"停止训练任务 - 任务ID: {task_id}")
        training_service.stop_training(task_id)
        
        return jsonify({
            'code': 200,
            'message': '训练任务已停止',
            'data': {'taskId': task_id}
        })
        
    except Exception as e:
        logger.error(f"停止训练任务失败: {str(e)}", exc_info=True)
        return jsonify({
            'code': 500,
            'message': f'停止训练失败: {str(e)}'
        }), 500


@training_bp.route('/status/<int:task_id>', methods=['GET'])
def get_training_status(task_id):
    """
    获取训练状态
    """
    try:
        status = training_service.get_task_status(task_id)
        
        if status is None:
            return jsonify({
                'code': 404,
                'message': '任务不存在'
            }), 404
        
        return jsonify({
            'code': 200,
            'message': '获取状态成功',
            'data': status
        })
        
    except Exception as e:
        logger.error(f"获取训练状态失败: {str(e)}", exc_info=True)
        return jsonify({
            'code': 500,
            'message': f'获取状态失败: {str(e)}'
        }), 500


@training_bp.route('/health', methods=['GET'])
def health_check():
    """
    健康检查
    """
    return jsonify({
        'code': 200,
        'message': '训练服务运行正常',
        'data': {
            'service': 'training',
            'status': 'healthy',
            'active_tasks': len(training_service.running_tasks)
        }
    })
