"""
Python 合规检测服务
此服务作为独立的微服务运行，提供内容合规性检测功能

接口规范：
POST /api/compliance/check
Request Body: {
    "content": "待检测的文本内容"
}
Response: {
    "result": "PASS" | "FAIL",
    "risk_level": "LOW" | "MEDIUM" | "HIGH",
    "risk_categories": "违规类别1,违规类别2",
    "confidence_score": 0.95,
    "detail": "详细说明"
}

实现方式建议：
1. 使用大模型+Prompt进行判别（推荐）
   - 可以使用OpenAI API
   - 或使用本地部署的开源大模型（如LLaMA、ChatGLM等）
   
2. 使用传统机器学习算法
   - 敏感词过滤
   - 文本分类模型
   - NLP特征提取+分类器

3. 混合方案
   - 先用规则过滤明显违规内容
   - 再用大模型对边界案例进行判断
"""

from flask import Flask, request, jsonify
from flask_cors import CORS
import logging

app = Flask(__name__)
CORS(app)

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# ============================================================
# 方案1: 使用大模型进行合规检测（示例代码）
# ============================================================
def check_with_llm(content):
    """
    使用大模型进行合规检测
    
    你需要：
    1. 配置你的大模型API（OpenAI/Anthropic/本地模型等）
    2. 设计合适的Prompt来判断内容合规性
    3. 解析模型返回的结果
    """
    # TODO: 实现你的大模型调用逻辑
    # 示例Prompt:
    prompt = f"""
    请判断以下文本内容是否合规。评估维度包括：
    1. 是否包含违法违规信息
    2. 是否包含色情、暴力、恐怖内容
    3. 是否包含歧视、仇恨言论
    4. 是否包含虚假信息或诈骗内容
    5. 是否包含侵犯隐私的信息
    
    请返回JSON格式：
    {{
        "result": "PASS" or "FAIL",
        "risk_level": "LOW" or "MEDIUM" or "HIGH",
        "risk_categories": "违规类别（逗号分隔）",
        "detail": "详细说明"
    }}
    
    待检测内容：
    {content}
    """
    
    # 这里是示例返回，实际需要调用你的大模型API
    return {
        "result": "PASS",
        "risk_level": "LOW",
        "risk_categories": "",
        "confidence_score": 0.95,
        "detail": "内容正常，未发现违规"
    }


# ============================================================
# 方案2: 使用规则和算法进行检测（示例代码）
# ============================================================

# 构建敏感词DFA树
class DFAFilter:
    def __init__(self):
        self.root = {}
        self.load_badwords()
    
    def load_badwords(self):
        """
        从Badwords.txt加载敏感词并构建成DFA树
        """
        try:
            with open('Badwords.txt', 'r', encoding='utf-8') as f:
                for line in f:
                    word = line.strip()
                    if word:
                        self.add_word(word)
        except Exception as e:
            logger.error(f"加载敏感词文件失败: {e}")
    
    def add_word(self, word):
        """
        添加敏感词到DFA树
        """
        node = self.root
        for char in word:
            if char not in node:
                node[char] = {}
            node = node[char]
        node['end'] = True  # 标记单词结束
    
    def search(self, content):
        """
        在内容中查找敏感词
        返回找到的第一个敏感词
        """
        for i in range(len(content)):
            node = self.root
            for j in range(i, len(content)):
                char = content[j]
                if char in node:
                    node = node[char]
                    if 'end' in node:
                        # 找到了敏感词
                        return content[i:j+1]
                else:
                    break
        return None

dfa_filter = DFAFilter()

def check_with_rules(content):
    """
    使用DFA算法检测敏感词
    """
    found_word = dfa_filter.search(content)
    
    if found_word:
        return {
            "result": "FAIL",
            "risk_level": "HIGH",
            "risk_categories": "敏感词汇",
            "confidence_score": 0.99,
            "detail": f"内容包含敏感词: {found_word}"
        }
    
    # 默认返回：所有内容都合规
    return {
        "result": "PASS",
        "risk_level": "LOW",
        "risk_categories": "",
        "confidence_score": 0.99,
        "detail": "内容检测通过，未发现敏感词"
    }


@app.route('/api/compliance/check', methods=['POST'])
def check_compliance():
    """
    合规检测接口
    """
    try:
        data = request.get_json()
        content = data.get('content', '')
        
        if not content:
            return jsonify({
                "error": "内容不能为空"
            }), 400
        
        logger.info(f"收到检测请求，内容长度: {len(content)}")
        
        # 选择检测方式
        # 方式1: 使用大模型（推荐，但需要配置）
        # result = check_with_llm(content)
        
        # 方式2: 使用规则（当前版本默认全部通过，用于跑通逻辑）
        result = check_with_rules(content)
        
        # TODO: 生产环境建议使用大模型进行检测，提供更准确的结果
        
        logger.info(f"检测完成，结果: {result['result']}")
        
        return jsonify(result)
    
    except Exception as e:
        logger.error(f"检测失败: {str(e)}")
        return jsonify({
            "error": str(e)
        }), 500


@app.route('/health', methods=['GET'])
def health_check():
    """
    健康检查接口
    """
    return jsonify({
        "status": "healthy",
        "service": "compliance-check"
    })


if __name__ == '__main__':
    print("=" * 60)
    print("Python合规检测服务启动中...")
    print("接口地址: http://localhost:5000/api/compliance/check")
    print("=" * 60)
    app.run(host='0.0.0.0', port=5000, debug=True)
