import logging
import requests
import json
from config import config

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

def check_with_llm(content, llm_config=None):
    """
    使用大模型进行合规检测
    
    参数:
        content: 待检测的内容
        llm_config: 大模型配置字典，包含以下键:
            - url: API地址 (可选，默认使用config中的配置)
            - key: API密钥 (可选，默认使用config中的配置)
            - model: 模型名称 (可选，默认使用config中的配置)
            - timeout: 超时时间 (可选，默认使用config中的配置)
    
    返回:
        包含检测结果的字典
    """
    try:
        # 获取配置
        if llm_config is None:
            llm_config = {}
        
        # 从配置中获取默认值
        default_config = config.get('default', config['development'])
        
        url = llm_config.get('url', default_config.LLM_DEFAULT_URL)
        key = llm_config.get('key', default_config.LLM_DEFAULT_KEY)
        model = llm_config.get('model', default_config.LLM_DEFAULT_MODEL)
        timeout = llm_config.get('timeout', default_config.LLM_DEFAULT_TIMEOUT)
        
        # 检查必要配置
        if not url:
            logger.error("未配置LLM API URL")
            return {
                "result": "FAIL",
                "risk_level": "HIGH",
                "risk_categories": "系统错误，LLM服务未配置，请设置API URL",
                "confidence_score": 0.0,
                "detail": "LLM服务未配置，请设置API URL"
            }
        
        if not key:
            logger.error("未配置LLM API Key")
            return {
                "result": "FAIL",
                "risk_level": "HIGH",
                "risk_categories": "系统错误，LLM服务未配置，请设置API Key",
                "confidence_score": 0.0,
                "detail": "LLM服务未配置，请设置API Key"
            }
        
        # 构建Prompt
        prompt = f"""
        请判断以下文本内容是否合规。评估维度包括：
        1. 是否包含违法违规信息
        2. 是否包含色情、暴力、恐怖内容
        3. 是否包含歧视、仇恨言论
        4. 是否包含虚假信息或诈骗内容
        5. 是否包含侵犯隐私的信息
        
        请严格遵守以下JSON格式返回，不要包含任何额外文本：
        {{
            "result": "PASS" or "FAIL",
            "risk_level": "LOW" or "MEDIUM" or "HIGH",
            "risk_categories": "违规类别（逗号分隔）",
            "detail": "详细说明"
        }}
        
        待检测内容：
        {content}
        """
        
        # 构建请求数据
        request_data = {
            "model": model,
            "messages": [
                {
                    "role": "system",
                    "content": "你是一个专业的合规检测助手，负责判断文本内容是否符合安全规范。请严格按照JSON格式返回结果。"
                },
                {
                    "role": "user",
                    "content": prompt
                }
            ],
            "temperature": 0.1,
            "max_tokens": 500
        }
        
        # 设置请求头
        headers = {
            "Content-Type": "application/json",
            "Authorization": f"Bearer {key}"
        }
        
        logger.info(f"调用LLM API - URL: {url}, Model: {model}")
        
        # 发送请求
        response = requests.post(
            url,
            json=request_data,
            headers=headers,
            timeout=timeout
        )
        
        # 检查响应状态
        if response.status_code != 200:
            logger.error(f"LLM API返回错误状态码: {response.status_code}, 响应: {response.text}")
            return {
                "result": "FAIL",
                "risk_level": "MEDIUM",
                "risk_categories": "系统错误",
                "confidence_score": 0.0,
                "detail": f"LLM服务调用失败: {response.status_code}"
            }
        
        # 解析响应
        response_data = response.json()
        
        # 提取模型返回的内容
        if 'choices' in response_data and len(response_data['choices']) > 0:
            content_text = response_data['choices'][0]['message']['content']
            
            # 尝试解析JSON
            try:
                # 清理响应内容，移除可能的Markdown代码块标记
                content_text = content_text.strip()
                if content_text.startswith('```json'):
                    content_text = content_text[7:]
                if content_text.endswith('```'):
                    content_text = content_text[:-3]
                content_text = content_text.strip()
                
                result_data = json.loads(content_text)
                
                # 验证返回的数据结构
                required_fields = ['result', 'risk_level', 'risk_categories', 'detail']
                for field in required_fields:
                    if field not in result_data:
                        logger.warning(f"LLM返回缺少字段 {field}")
                        result_data[field] = ""
                
                # 添加置信度
                if result_data.get('result') == 'FAIL':
                    result_data['confidence_score'] = 0.85
                else:
                    result_data['confidence_score'] = 0.95
                
                logger.info(f"LLM检测完成 - 结果: {result_data.get('result')}, 风险等级: {result_data.get('risk_level')}")
                return result_data
                
            except json.JSONDecodeError as e:
                logger.error(f"解析LLM响应JSON失败: {e}, 原始内容: {content_text}")
                return {
                    "result": "FAIL",
                    "risk_level": "MEDIUM",
                    "risk_categories": "系统错误",
                    "confidence_score": 0.0,
                    "detail": f"解析LLM响应失败: {str(e)}"
                }
        else:
            logger.error(f"LLM响应格式异常: {response_data}")
            return {
                "result": "FAIL",
                "risk_level": "MEDIUM",
                "risk_categories": "系统错误",
                "confidence_score": 0.0,
                "detail": "LLM响应格式异常"
            }
            
    except requests.exceptions.Timeout:
        logger.error("LLM API调用超时")
        return {
            "result": "FAIL",
            "risk_level": "MEDIUM",
            "risk_categories": "系统错误",
            "confidence_score": 0.0,
            "detail": "LLM服务调用超时"
        }
        
    except requests.exceptions.ConnectionError:
        logger.error("LLM API连接失败")
        return {
            "result": "FAIL",
            "risk_level": "HIGH",
            "risk_categories": "系统错误",
            "confidence_score": 0.0,
            "detail": "无法连接到LLM服务，请检查网络或服务地址"
        }
        
    except Exception as e:
        logger.error(f"LLM检测发生未知错误: {str(e)}", exc_info=True)
        return {
            "result": "FAIL",
            "risk_level": "HIGH",
            "risk_categories": "系统错误",
            "confidence_score": 0.0,
            "detail": f"LLM检测发生错误: {str(e)}"
        }

def check_compliance(content, mode='moderate', llm_config=None):
    """
    合规检测主函数
    支持三种模式:
    - loose (宽松): 只检查敏感词
    - strict (严格): 先检查敏感词，再用大模型检查
    - moderate (默认): 先检查敏感词，如果有敏感词再用大模型二次检查
    
    参数:
        content: 待检测的内容
        mode: 检测模式 ('loose', 'strict', 'moderate')
        llm_config: 大模型配置字典，用于传递给check_with_llm函数
    """
    # 根据不同模式执行检查
    if mode == 'loose':
        # 宽松模式：只检查敏感词
        result = check_with_rules(content)
    elif mode == 'strict':
        # 严格模式：先检查敏感词，再用大模型检查
        rule_result = check_with_rules(content)
        # 即使敏感词检查通过，也使用大模型进一步检查
        llm_result = check_with_llm(content, llm_config)
        
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
            llm_result = check_with_llm(content, llm_config)
            # 以大模型结果为准
            result = llm_result
        else:
            # 敏感词检查通过，直接返回结果
            result = rule_result
    
    return result
