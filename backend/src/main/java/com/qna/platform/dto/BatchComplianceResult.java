package com.qna.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量合规检测结果
 *
 * @author QnA Platform
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchComplianceResult {
    
    /**
     * 检测项结果列表
     */
    private List<ComplianceItem> items;
    
    /**
     * 总数
     */
    private Integer total;
    
    /**
     * 通过数
     */
    private Integer passedCount;
    
    /**
     * 失败数
     */
    private Integer failedCount;
    
    /**
     * 未检测数
     */
    private Integer uncheckedCount;
    
    /**
     * 检测项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplianceItem {
        /**
         * 序号
         */
        private Integer index;
        
        /**
         * 原始内容（用户消息）
         */
        private String userContent;
        
        /**
         * AI响应内容
         */
        private String assistantContent;
        
        /**
         * 用户消息检测结果
         */
        private String userResult;
        
        /**
         * AI响应检测结果
         */
        private String assistantResult;
        
        /**
         * 用户消息风险等级
         */
        private String userRiskLevel;
        
        /**
         * AI响应风险等级
         */
        private String assistantRiskLevel;
        
        /**
         * 用户消息风险类别
         */
        private String userRiskCategories;
        
        /**
         * AI响应风险类别
         */
        private String assistantRiskCategories;
        
        /**
         * 检测时间戳
         */
        private Long timestamp;
        
        /**
         * 原始会话ID（如果从导出文件解析）
         */
        private Long sessionId;
        
        /**
         * 原始消息ID（如果从导出文件解析）
         */
        private Long messageId;
    }
}
