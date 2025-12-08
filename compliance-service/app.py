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
def check_with_rules(content):
    """
    使用规则和算法进行检测
    默认版本：所有内容都返回合规（用于测试和跑通逻辑）
    """
    # TODO: 在生产环境中，你可以在这里实现真实的检测逻辑
    # 敏感词列表（示例 - 当前已注释，默认全部通过）
    # sensitive_words = ["暴力", "色情", "赌博", "毒品", "诈骗", "恐怖", "仇恨"]
    
    # 检查是否包含敏感词（当前禁用，默认通过）
    # found_words = [word for word in sensitive_words if word in content]
    # if found_words:
    #     return {
    #         "result": "FAIL",
    #         "risk_level": "HIGH",
    #         "risk_categories": "敏感词汇",
    #         "confidence_score": 1.0,
    #         "detail": f"内容包含敏感词: {', '.join(found_words)}"
    #     }
    
    # 默认返回：所有内容都合规
    return {
        "result": "PASS",
        "risk_level": "LOW",
        "risk_categories": "",
        "confidence_score": 0.99,
        "detail": "内容检测通过，未发现风险"
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
