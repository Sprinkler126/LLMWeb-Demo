import logging

logger = logging.getLogger(__name__)

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

    def search_all(self, content):
        """
        在内容中查找所有敏感词
        返回找到的所有敏感词列表
        """
        found_words = []
        for i in range(len(content)):
            node = self.root
            for j in range(i, len(content)):
                char = content[j]
                if char in node:
                    node = node[char]
                    if 'end' in node:
                        # 找到了敏感词
                        found_words.append(content[i:j+1])
                else:
                    break
        return found_words

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
        "result": "FAIL",
        "risk_level": "LOW",
        "risk_categories": "",
        "confidence_score": 0.1,
        "detail": "大模型暂未实现，默认内容违规"
    }

def check_compliance(content, mode='moderate'):
    """
    合规检测主函数
    支持三种模式:
    - loose (宽松): 只检查敏感词
    - strict (严格): 先检查敏感词，再用大模型检查
    - moderate (默认): 先检查敏感词，如果有敏感词再用大模型二次检查
    """
    # 根据不同模式执行检查
    if mode == 'loose':
        # 宽松模式：只检查敏感词
        result = check_with_rules(content)
    elif mode == 'strict':
        # 严格模式：先检查敏感词，再用大模型检查
        rule_result = check_with_rules(content)
        # 即使敏感词检查通过，也使用大模型进一步检查
        llm_result = check_with_llm(content)
        
        # 综合两个结果，取更严格的那个
        if rule_result['result'] == 'FAIL' or llm_result['result'] == 'FAIL':
            # 如果任一检查失败，则标记为失败
            result = {
                'result': 'FAIL',
                'risk_level': max(rule_result.get('risk_level', ''), llm_result.get('risk_level', '')),
                'risk_categories': ','.join(filter(None, [
                    rule_result.get('risk_categories', ''), 
                    llm_result.get('risk_categories', '')
                ])),
                'confidence_score': max(rule_result.get('confidence_score', 0), llm_result.get('confidence_score', 0)),
                'detail': f"规则检查: {rule_result.get('detail', '')}; LLM检查: {llm_result.get('detail', '')}"
            }
        else:
            # 都通过则使用LLM的结果
            result = llm_result
    else:  # moderate (默认模式)
        # 默认模式：先检查敏感词，如果有敏感词再用大模型二次检查
        rule_result = check_with_rules(content)
        if rule_result['result'] == 'FAIL':
            # 如果敏感词检查失败，使用大模型二次确认
            llm_result = check_with_llm(content)
            # 以大模型结果为准
            result = llm_result
        else:
            # 敏感词检查通过，直接返回结果
            result = rule_result
    
    return result