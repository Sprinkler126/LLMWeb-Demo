package com.qna.platform.enums;

/**
 * API提供商枚举
 *
 * @author QnA Platform
 */
public enum ApiProvider {
    /**
     * OpenAI
     */
    OPENAI("OpenAI"),
    
    /**
     * Anthropic (Claude)
     */
    ANTHROPIC("Anthropic"),
    
    /**
     * Google (Gemini)
     */
    GOOGLE("Google"),
    
    /**
     * 阿里云通义千问
     */
    ALIYUN("Aliyun"),
    
    /**
     * 百度文心一言
     */
    BAIDU("Baidu"),
    
    /**
     * DeepSeek
     */
    DEEPSEEK("DeepSeek"),
    
    /**
     * 本地模型
     */
    LOCAL("Local"),
    
    /**
     * 其他
     */
    OTHER("Other");

    private final String description;

    ApiProvider(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
