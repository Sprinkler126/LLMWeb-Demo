import re
import logging
import requests
import json
from config import config

logger = logging.getLogger(__name__)

def validate_id_card_checksum(id_card):
    """
    验证18位身份证号码的校验码是否正确
    """
    if len(id_card) != 18:
        return False
    
    # 权重系数
    weights = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2]
    # 校验码对应关系
    checksum_map = ['1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2']
    
    # 计算前17位与权重系数的乘积之和
    sum_value = sum(int(id_card[i]) * weights[i] for i in range(17))
    
    # 计算校验码
    mod = sum_value % 11
    expected_checksum = checksum_map[mod]
    
    # 比较校验码
    return id_card[17].upper() == expected_checksum

def scan_with_llm(content, llm_config=None):
    """
    使用大模型进行个人信息扫描
    
    参数:
        content: 待扫描的内容
        llm_config: 大模型配置字典，包含以下键:
            - url: API地址 (可选，默认使用config中的配置)
            - key: API密钥 (可选，默认使用config中的配置)
            - model: 模型名称 (可选，默认使用config中的配置)
            - timeout: 超时时间 (可选，默认使用config中的配置)
    
    返回:
        包含扫描结果的字典
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
                "id_cards_found": 0,
                "phones_found": 0,
                "id_cards": [],
                "phones": [],
                "has_sensitive_info": False,
                "error": "LLM服务未配置，请设置API URL"
            }
        
        if not key:
            logger.error("未配置LLM API Key")
            return {
                "id_cards_found": 0,
                "phones_found": 0,
                "id_cards": [],
                "phones": [],
                "has_sensitive_info": False,
                "error": "LLM服务未配置，请设置API Key"
            }
        
        # 构建Prompt
        prompt = f"""
        请扫描以下文本内容，识别其中包含的个人敏感信息，包括：
        1. 身份证号码（18位，最后一位可能是X）
        2. 手机号码（中国大陆格式）
        
        请严格遵守以下JSON格式返回，不要包含任何额外文本：
        {{
            "id_cards": ["身份证号码1", "身份证号码2", ...],
            "phones": ["手机号码1", "手机号码2", ...]
        }}
        
        如果没有找到任何敏感信息，请返回空数组。
        
        待扫描内容：
        {content}
        """
        
        # 构建请求数据
        request_data = {
            "model": model,
            "messages": [
                {
                    "role": "system",
                    "content": "你是一个专业的个人信息扫描助手，负责识别文本中的身份证号码和手机号码。请严格按照JSON格式返回结果，不要包含任何额外说明。"
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
        
        logger.info(f"调用LLM API进行个人信息扫描 - URL: {url}, Model: {model}")
        
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
                "id_cards_found": 0,
                "phones_found": 0,
                "id_cards": [],
                "phones": [],
                "has_sensitive_info": False,
                "error": f"LLM服务调用失败: {response.status_code}"
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
                
                # 提取身份证和手机号
                id_cards = result_data.get('id_cards', [])
                phones = result_data.get('phones', [])
                
                # 对身份证号码进行校验码验证
                valid_id_cards = []
                for id_card in id_cards:
                    if validate_id_card_checksum(id_card):
                        valid_id_cards.append(id_card)
                
                # 去重
                unique_id_cards = list(set(valid_id_cards))
                unique_phones = list(set(phones))
                
                result = {
                    "id_cards_found": len(unique_id_cards),
                    "phones_found": len(unique_phones),
                    "id_cards": unique_id_cards,
                    "phones": unique_phones,
                    "has_sensitive_info": len(unique_id_cards) > 0 or len(unique_phones) > 0
                }
                
                logger.info(f"LLM扫描完成 - 身份证号: {len(unique_id_cards)}个, 手机号: {len(unique_phones)}个")
                return result
                
            except json.JSONDecodeError as e:
                logger.error(f"解析LLM响应JSON失败: {e}, 原始内容: {content_text}")
                return {
                    "id_cards_found": 0,
                    "phones_found": 0,
                    "id_cards": [],
                    "phones": [],
                    "has_sensitive_info": False,
                    "error": f"解析LLM响应失败: {str(e)}"
                }
        else:
            logger.error(f"LLM响应格式异常: {response_data}")
            return {
                "id_cards_found": 0,
                "phones_found": 0,
                "id_cards": [],
                "phones": [],
                "has_sensitive_info": False,
                "error": "LLM响应格式异常"
            }
            
    except requests.exceptions.Timeout:
        logger.error("LLM API调用超时")
        return {
            "id_cards_found": 0,
            "phones_found": 0,
            "id_cards": [],
            "phones": [],
            "has_sensitive_info": False,
            "error": "LLM服务调用超时"
        }
        
    except requests.exceptions.ConnectionError:
        logger.error("LLM API连接失败")
        return {
            "id_cards_found": 0,
            "phones_found": 0,
            "id_cards": [],
            "phones": [],
            "has_sensitive_info": False,
            "error": "无法连接到LLM服务，请检查网络或服务地址"
        }
        
    except Exception as e:
        logger.error(f"LLM扫描发生未知错误: {str(e)}", exc_info=True)
        return {
            "id_cards_found": 0,
            "phones_found": 0,
            "id_cards": [],
            "phones": [],
            "has_sensitive_info": False,
            "error": f"LLM扫描发生错误: {str(e)}"
        }

def scan_personal_info(content, scan_mode='rules', llm_config=None):
    """
    扫描文本中的个人敏感信息（身份证号和手机号）
    
    参数:
        content: 待扫描的内容
        scan_mode: 扫描模式
            - 'rules': 只使用正则规则扫描（默认）
            - 'llm': 只使用大模型扫描
            - 'both': 同时使用两种方式，结果合并
        llm_config: 大模型配置字典（当scan_mode为'llm'或'both'时使用）
    
    返回:
        包含扫描结果的字典
    """
    try:
        if not content:
            raise ValueError("内容不能为空")
        
        logger.info(f"开始扫描个人信息，内容长度: {len(content)}, 扫描模式: {scan_mode}")
        
        if scan_mode == 'llm':
            # 只使用大模型扫描
            return scan_with_llm(content, llm_config)
        
        elif scan_mode == 'both':
            # 同时使用两种方式，结果合并
            rules_result = _scan_with_rules(content)
            llm_result = scan_with_llm(content, llm_config)
            
            # 合并结果，去重
            all_id_cards = list(set(rules_result['id_cards'] + llm_result.get('id_cards', [])))
            all_phones = list(set(rules_result['phones'] + llm_result.get('phones', [])))
            
            # 对身份证号码进行校验码验证
            valid_id_cards = []
            for id_card in all_id_cards:
                if validate_id_card_checksum(id_card):
                    valid_id_cards.append(id_card)
            
            return {
                "id_cards_found": len(valid_id_cards),
                "phones_found": len(all_phones),
                "id_cards": valid_id_cards,
                "phones": all_phones,
                "has_sensitive_info": len(valid_id_cards) > 0 or len(all_phones) > 0,
                "scan_methods": ["rules", "llm"],
                "rules_result": rules_result,
                "llm_result": llm_result
            }
        
        else:  # 'rules' or default
            # 只使用正则规则扫描
            return _scan_with_rules(content)
    
    except Exception as e:
        logger.error(f"扫描失败: {str(e)}")
        raise e

def _scan_with_rules(content):
    """
    使用正则规则扫描文本中的个人敏感信息
    """
    logger.info(f"使用正则规则扫描个人信息")
    
    # 定义身份证号和手机号的正则表达式
    # 身份证号（18位，最后一位可能是X）
    # 区号(6位) + 出生年份(4位) + 月份(2位) + 日期(2位) + 顺序码(3位) + 校验码(1位)
    id_card_pattern = r'[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]'
    # 手机号（中国大陆）
    phone_pattern = r'(?:\+?86[-\s]?)?(?:13\d|14[579]|15[0-35-9]|16[2567]|17[01235-8]|18\d|19[189])\d{8}'
    
    # 查找所有匹配的身份证号和手机号
    id_cards = re.findall(id_card_pattern, content)
    phones = re.findall(phone_pattern, content)
    
    # 去重
    unique_id_cards = list(set(id_cards))
    unique_phones = list(set(phones))
    
    # 对于身份证号码，我们需要验证校验码
    valid_id_cards = []
    for id_card in unique_id_cards:
        if validate_id_card_checksum(id_card):
            valid_id_cards.append(id_card)
    
    result = {
        "id_cards_found": len(valid_id_cards),
        "phones_found": len(unique_phones),
        "id_cards": valid_id_cards,
        "phones": unique_phones,
        "has_sensitive_info": len(valid_id_cards) > 0 or len(unique_phones) > 0
    }
    
    logger.info(f"正则规则扫描完成，发现身份证号: {len(valid_id_cards)}个, 手机号: {len(unique_phones)}个")
    
    return result
