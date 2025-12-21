"""
文本分类器模型
用于多分类任务的通用分类器
"""
import numpy as np
import pickle
import logging
from typing import Tuple

logger = logging.getLogger(__name__)


class TextClassifier:
    """
    通用文本分类器
    支持多分类任务
    """
    
    def __init__(self, learning_rate=0.001, vocab_size=1000, embedding_dim=50, hidden_dim=100, num_classes=3):
        self.learning_rate = learning_rate
        self.vocab_size = vocab_size
        self.embedding_dim = embedding_dim
        self.hidden_dim = hidden_dim
        self.num_classes = num_classes
        
        # 初始化模型参数
        np.random.seed(42)
        self.weights = {
            'embedding': np.random.randn(vocab_size, embedding_dim) * 0.01,
            'hidden': np.random.randn(embedding_dim, hidden_dim) * 0.01,
            'output': np.random.randn(hidden_dim, num_classes) * 0.01
        }
        
        logger.info(f"文本分类器初始化完成 - 类别数: {num_classes}, 学习率: {learning_rate}")
    
    def load_dataset(self, dataset_path=None):
        """
        加载数据集
        """
        logger.info("生成模拟训练数据")
        
        train_size = 200
        val_size = 50
        
        train_data = {
            'texts': np.random.randint(0, self.vocab_size, (train_size, 20)),
            'labels': np.random.randint(0, self.num_classes, train_size)
        }
        
        val_data = {
            'texts': np.random.randint(0, self.vocab_size, (val_size, 20)),
            'labels': np.random.randint(0, self.num_classes, val_size)
        }
        
        logger.info(f"数据集加载完成 - 训练集: {train_size}, 验证集: {val_size}")
        return train_data, val_data
    
    def train_epoch(self, train_data, batch_size=32) -> Tuple[float, float]:
        """训练一个epoch"""
        texts = train_data['texts']
        labels = train_data['labels']
        n_samples = len(texts)
        
        total_loss = 0
        correct = 0
        
        indices = np.random.permutation(n_samples)
        
        for i in range(0, n_samples, batch_size):
            batch_indices = indices[i:min(i + batch_size, n_samples)]
            batch_texts = texts[batch_indices]
            batch_labels = labels[batch_indices]
            
            embeddings = self._get_embeddings(batch_texts)
            hidden = np.tanh(embeddings @ self.weights['hidden'])
            logits = hidden @ self.weights['output']
            
            predictions = np.argmax(logits, axis=1)
            batch_loss = np.mean((predictions - batch_labels) ** 2)
            batch_correct = np.sum(predictions == batch_labels)
            
            total_loss += batch_loss * len(batch_texts)
            correct += batch_correct
            
            self._update_weights()
        
        avg_loss = total_loss / n_samples
        accuracy = correct / n_samples
        
        return avg_loss, accuracy
    
    def validate(self, val_data, batch_size=32) -> Tuple[float, float]:
        """验证模型"""
        texts = val_data['texts']
        labels = val_data['labels']
        n_samples = len(texts)
        
        total_loss = 0
        correct = 0
        
        for i in range(0, n_samples, batch_size):
            batch_texts = texts[i:min(i + batch_size, n_samples)]
            batch_labels = labels[i:min(i + batch_size, n_samples)]
            
            embeddings = self._get_embeddings(batch_texts)
            hidden = np.tanh(embeddings @ self.weights['hidden'])
            logits = hidden @ self.weights['output']
            
            predictions = np.argmax(logits, axis=1)
            batch_loss = np.mean((predictions - batch_labels) ** 2)
            batch_correct = np.sum(predictions == batch_labels)
            
            total_loss += batch_loss * len(batch_texts)
            correct += batch_correct
        
        avg_loss = total_loss / n_samples
        accuracy = correct / n_samples
        
        return avg_loss, accuracy
    
    def _get_embeddings(self, texts):
        """获取文本嵌入"""
        embeddings = []
        for text in texts:
            text_embedding = np.mean(self.weights['embedding'][text], axis=0)
            embeddings.append(text_embedding)
        return np.array(embeddings)
    
    def _update_weights(self):
        """更新权重（简化版）"""
        for key in self.weights:
            noise = np.random.randn(*self.weights[key].shape) * self.learning_rate * 0.01
            self.weights[key] += noise
    
    def save_model(self, save_path):
        """保存模型"""
        model_data = {
            'weights': self.weights,
            'config': {
                'learning_rate': self.learning_rate,
                'vocab_size': self.vocab_size,
                'embedding_dim': self.embedding_dim,
                'hidden_dim': self.hidden_dim,
                'num_classes': self.num_classes
            }
        }
        
        with open(save_path, 'wb') as f:
            pickle.dump(model_data, f)
        
        logger.info(f"模型已保存到: {save_path}")
    
    def load_model(self, model_path):
        """加载模型"""
        with open(model_path, 'rb') as f:
            model_data = pickle.load(f)
        
        self.weights = model_data['weights']
        config = model_data['config']
        self.learning_rate = config['learning_rate']
        self.vocab_size = config['vocab_size']
        self.embedding_dim = config['embedding_dim']
        self.hidden_dim = config['hidden_dim']
        self.num_classes = config['num_classes']
        
        logger.info(f"模型已从 {model_path} 加载")
