"""
模型训练服务
负责执行实际的模型训练任务
"""
import json
import logging
import os
import threading
import time
from datetime import datetime
import requests

logger = logging.getLogger(__name__)


class TrainingService:
    """训练服务类"""
    
    def __init__(self):
        self.running_tasks = {}  # 存储运行中的任务
        self.stop_flags = {}     # 存储停止标志
        
    def start_training(self, task_id, model_type, dataset_path, model_config, callback_url):
        """
        启动训练任务（异步）
        """
        if task_id in self.running_tasks:
            raise Exception(f"任务 {task_id} 已经在运行中")
        
        # 标记任务为运行中
        self.running_tasks[task_id] = {
            'status': 'RUNNING',
            'start_time': datetime.now()
        }
        self.stop_flags[task_id] = False
        
        # 在新线程中执行训练
        thread = threading.Thread(
            target=self._run_training,
            args=(task_id, model_type, dataset_path, model_config, callback_url),
            daemon=True
        )
        thread.start()
        
        logger.info(f"训练任务 {task_id} 已在后台启动")
    
    def stop_training(self, task_id):
        """
        停止训练任务
        """
        if task_id not in self.running_tasks:
            raise Exception(f"任务 {task_id} 不存在或未运行")
        
        logger.info(f"设置停止标志 - 任务ID: {task_id}")
        self.stop_flags[task_id] = True
    
    def get_task_status(self, task_id):
        """
        获取任务状态
        """
        return self.running_tasks.get(task_id)
    
    def _run_training(self, task_id, model_type, dataset_path, model_config, callback_url):
        """
        执行训练任务的核心逻辑
        """
        try:
            logger.info(f"开始训练任务 {task_id} - 模型类型: {model_type}")
            
            # 解析配置
            config = json.loads(model_config) if isinstance(model_config, str) else model_config
            epochs = config.get('epochs', 10)
            batch_size = config.get('batchSize', 32)
            learning_rate = config.get('learningRate', 0.001)
            
            # 选择并加载模型
            if model_type == 'SENTIMENT_ANALYZER':
                from models.sentiment_analyzer import SentimentAnalyzer
                model = SentimentAnalyzer(learning_rate=learning_rate)
            elif model_type == 'TEXT_CLASSIFIER':
                # 这里可以添加其他模型类型
                from models.text_classifier import TextClassifier
                model = TextClassifier(learning_rate=learning_rate)
            else:
                raise Exception(f"不支持的模型类型: {model_type}")
            
            # 加载数据集
            logger.info(f"加载数据集: {dataset_path if dataset_path else '使用默认数据集'}")
            train_data, val_data = model.load_dataset(dataset_path)
            
            # 训练循环
            for epoch in range(1, epochs + 1):
                # 检查停止标志
                if self.stop_flags.get(task_id, False):
                    logger.info(f"任务 {task_id} 被停止")
                    self._send_progress(callback_url, {
                        'taskId': task_id,
                        'taskStatus': 'STOPPED',
                        'progress': int((epoch / epochs) * 100),
                        'currentEpoch': epoch,
                        'totalEpochs': epochs,
                        'latestLog': f'训练在第 {epoch} 轮被停止'
                    })
                    break
                
                logger.info(f"任务 {task_id} - 训练轮次 {epoch}/{epochs}")
                
                # 训练一个epoch
                train_loss, train_acc = model.train_epoch(train_data, batch_size)
                
                # 验证
                val_loss, val_acc = model.validate(val_data, batch_size)
                
                # 计算进度
                progress = int((epoch / epochs) * 100)
                
                # 发送进度更新
                self._send_progress(callback_url, {
                    'taskId': task_id,
                    'taskStatus': 'RUNNING',
                    'progress': progress,
                    'currentEpoch': epoch,
                    'totalEpochs': epochs,
                    'trainLoss': round(train_loss, 6),
                    'trainAccuracy': round(train_acc, 4),
                    'valLoss': round(val_loss, 6),
                    'valAccuracy': round(val_acc, 4),
                    'latestLog': f'Epoch {epoch}/{epochs} - '
                                f'Train Loss: {train_loss:.4f}, Train Acc: {train_acc:.4f}, '
                                f'Val Loss: {val_loss:.4f}, Val Acc: {val_acc:.4f}'
                })
                
                # 模拟训练时间（实际训练中可能需要更多时间）
                time.sleep(2)
            
            # 训练完成，保存模型
            if not self.stop_flags.get(task_id, False):
                model_save_path = f"models/saved/{model_type}_{task_id}.pkl"
                os.makedirs(os.path.dirname(model_save_path), exist_ok=True)
                model.save_model(model_save_path)
                
                logger.info(f"任务 {task_id} 训练完成")
                
                # 发送完成通知
                self._send_progress(callback_url, {
                    'taskId': task_id,
                    'taskStatus': 'COMPLETED',
                    'progress': 100,
                    'currentEpoch': epochs,
                    'totalEpochs': epochs,
                    'latestLog': f'训练完成！模型已保存到: {model_save_path}'
                })
            
        except Exception as e:
            logger.error(f"训练任务 {task_id} 失败: {str(e)}", exc_info=True)
            
            # 发送失败通知
            self._send_progress(callback_url, {
                'taskId': task_id,
                'taskStatus': 'FAILED',
                'errorMessage': str(e),
                'latestLog': f'训练失败: {str(e)}'
            })
        
        finally:
            # 清理任务记录
            if task_id in self.running_tasks:
                del self.running_tasks[task_id]
            if task_id in self.stop_flags:
                del self.stop_flags[task_id]
    
    def _send_progress(self, callback_url, progress_data):
        """
        向后端发送进度更新
        """
        if not callback_url:
            logger.warning("未配置回调URL，跳过进度更新")
            return
        
        try:
            response = requests.post(
                callback_url,
                json=progress_data,
                timeout=5
            )
            
            if response.status_code == 200:
                logger.debug(f"进度更新成功 - 任务ID: {progress_data.get('taskId')}")
            else:
                logger.warning(f"进度更新失败 - 状态码: {response.status_code}")
                
        except Exception as e:
            logger.error(f"发送进度更新失败: {str(e)}")
