package com.qna.platform.enums;

/**
 * 合规检测状态枚举
 *
 * @author QnA Platform
 */
public enum ComplianceStatus {
    /**
     * 未检测
     */
    UNCHECKED("未检测"),
    
    /**
     * 检测通过
     */
    PASS("通过"),
    
    /**
     * 检测不通过
     */
    FAIL("不通过");

    private final String description;

    ComplianceStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
