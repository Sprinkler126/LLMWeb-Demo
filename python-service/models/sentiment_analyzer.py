"""
情感分析模型
使用简单的神经网络进行情感分类
"""
import numpy as np
import pickle
import logging
from typing import Tuple, List

logger = logging.getLogger(__name__)


class SentimentAnalyzer:
    """
    情感分析器
    用于演示模型训练流程
    """
    
    def __init__(self, learning_rate=0.001, vocab_size=1000, embedding_dim=50, hidden_dim=100):
        self.learning_rate = learning_rate
        self.vocab_size = vocab_size
        self.embedding_dim = embedding_dim
        self.hidden_dim = hidden_dim
        
        # 初始化模型参数（简化版）
        np.random.seed(42)
        self.weights = {
            'embedding': np.random.randn(vocab_size, embedding_dim) * 0.01,
            'hidden': np.random.randn(embedding_dim, hidden_dim) * 0.01,
            'output': np.random.randn(hidden_dim, 2) * 0.01  # 二分类：正面/负面
        }
        
        logger.info(f"情感分析模型初始化完成 - 学习率: {learning_rate}")
    
    def load_dataset(self, dataset_path=None):
        """
        加载数据集
        如果未指定路径，使用模拟数据
        """
        if dataset_path:
            logger.info(f"从文件加载数据集: {dataset_path}")
            # 实际项目中在这里加载真实数据
            # 为演示目的，仍使用模拟数据
        
        logger.info("生成模拟训练数据")
        
        # 模拟数据：200个训练样本，50个验证样本
        train_size = 200
        val_size = 50
        
        # 生成随机token序列和标签
        train_data = {
            'texts': np.random.randint(0, self.vocab_size, (train_size, 20)),  # 20个token的序列
            'labels': np.random.randint(0, 2, train_size)  # 0: 负面, 1: 正面
        }
        
        val_data = {
            'texts': np.random.randint(0, self.vocab_size, (val_size, 20)),
            'labels': np.random.randint(0, 2, val_size)
        }
        
        logger.info(f"数据集加载完成 - 训练集: {train_size}, 验证集: {val_size}")
        return train_data, val_data
    
    def train_epoch(self, train_data, batch_size=32) -> Tuple[float, float]:
        """
        训练一个epoch
        返回: (loss, accuracy)
        """
        texts = train_data['texts']
        labels = train_data['labels']
        n_samples = len(texts)
        
        total_loss = 0
        correct = 0
        
        # 打乱数据
        indices = np.random.permutation(n_samples)
        
        for i in range(0, n_samples, batch_size):
            batch_indices = indices[i:min(i + batch_size, n_samples)]
            batch_texts = texts[batch_indices]
            batch_labels = labels[batch_indices]
            
            # 前向传播（简化版）
            embeddings = self._get_embeddings(batch_texts)
            hidden = np.tanh(embeddings @ self.weights['hidden'])
            logits = hidden @ self.weights['output']
            
            # 计算损失和准确率
            predictions = np.argmax(logits, axis=1)
            batch_loss = np.mean((predictions - batch_labels) ** 2)  # 简化的损失函数
            batch_correct = np.sum(predictions == batch_labels)
            
            total_loss += batch_loss * len(batch_texts)
            correct += batch_correct
            
            # 反向传播和参数更新（简化版 - 实际应用中需要完整的梯度计算）
            self._update_weights(embeddings, hidden, logits, batch_labels)
        
        avg_loss = total_loss / n_samples
        accuracy = correct / n_samples
        
        logger.debug(f"训练完成 - Loss: {avg_loss:.4f}, Accuracy: {accuracy:.4f}")
        return avg_loss, accuracy
    
    def validate(self, val_data, batch_size=32) -> Tuple[float, float]:
        """
        验证模型
        返回: (loss, accuracy)
        """
        texts = val_data['texts']
        labels = val_data['labels']
        n_samples = len(texts)
        
        total_loss = 0
        correct = 0
        
        for i in range(0, n_samples, batch_size):
            batch_texts = texts[i:min(i + batch_size, n_samples)]
            batch_labels = labels[i:min(i + batch_size, n_samples)]
            
            # 前向传播
            embeddings = self._get_embeddings(batch_texts)
            hidden = np.tanh(embeddings @ self.weights['hidden'])
            logits = hidden @ self.weights['output']
            
            # 计算损失和准确率
            predictions = np.argmax(logits, axis=1)
            batch_loss = np.mean((predictions - batch_labels) ** 2)
            batch_correct = np.sum(predictions == batch_labels)
            
            total_loss += batch_loss * len(batch_texts)
            correct += batch_correct
        
        avg_loss = total_loss / n_samples
        accuracy = correct / n_samples
        
        logger.debug(f"验证完成 - Loss: {avg_loss:.4f}, Accuracy: {accuracy:.4f}")
        return avg_loss, accuracy
    
    def _get_embeddings(self, texts):
        """
        获取文本的嵌入表示
        """
        # 简单平均池化
        embeddings = []
        for text in texts:
            text_embedding = np.mean(self.weights['embedding'][text], axis=0)
            embeddings.append(text_embedding)
        return np.array(embeddings)
    
    def _update_weights(self, embeddings, hidden, logits, labels):
        """
        更新模型权重（简化版）
        """
        # 这里是简化的权重更新，实际应用中需要完整的梯度下降
        # 为演示目的，添加小的随机扰动
        for key in self.weights:
            noise = np.random.randn(*self.weights[key].shape) * self.learning_rate * 0.01
            self.weights[key] += noise
    
    def save_model(self, save_path):
        """
        保存模型
        """
        model_data = {
            'weights': self.weights,
            'config': {
                'learning_rate': self.learning_rate,
                'vocab_size': self.vocab_size,
                'embedding_dim': self.embedding_dim,
                'hidden_dim': self.hidden_dim
            }
        }
        
        with open(save_path, 'wb') as f:
            pickle.dump(model_data, f)
        
        logger.info(f"模型已保存到: {save_path}")
    
    def load_model(self, model_path):
        """
        加载模型
        """
        with open(model_path, 'rb') as f:
            model_data = pickle.load(f)
        
        self.weights = model_data['weights']
        config = model_data['config']
        self.learning_rate = config['learning_rate']
        self.vocab_size = config['vocab_size']
        self.embedding_dim = config['embedding_dim']
        self.hidden_dim = config['hidden_dim']
        
        logger.info(f"模型已从 {model_path} 加载")
