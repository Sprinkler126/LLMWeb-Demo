import re
import logging

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

def scan_personal_info(content):
    """
    扫描文本中的个人敏感信息（身份证号和手机号）
    """
    try:
        if not content:
            raise ValueError("内容不能为空")
        
        logger.info(f"开始扫描个人信息，内容长度: {len(content)}")
        
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
        
        logger.info(f"扫描完成，发现身份证号: {len(valid_id_cards)}个, 手机号: {len(unique_phones)}个")
        
        return result
    
    except Exception as e:
        logger.error(f"扫描失败: {str(e)}")
        raise e