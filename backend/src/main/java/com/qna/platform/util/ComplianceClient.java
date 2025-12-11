package com.qna.platform.util;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

/**
 * 合规检测客户端
 *
 * @author QnA Platform
 */
@Slf4j
@Component
public class ComplianceClient {
    
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    
    @Value("${app.compliance.service-url:http://localhost:5000/api/compliance/check}")
    private String checkUrl;
    
    @Value("${app.compliance.timeout:30000}")
    private int timeout;
    
    private OkHttpClient client;
    
    /**
     * 获取HTTP客户端
     */
    private OkHttpClient getClient() {
        if (client == null) {
            client = new OkHttpClient.Builder()
                    .connectTimeout(Duration.ofMillis(timeout))
                    .readTimeout(Duration.ofMillis(timeout))
                    .writeTimeout(Duration.ofMillis(timeout))
                    .build();
        }
        return client;
    }
    
    /**
     * 调用合规检测服务
     * 
     * @param content 待检测内容
     * @return 检测结果，如果服务不可用则返回 null
     */
    public JSONObject checkContent(String content) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.set("content", content);
            
            RequestBody body = RequestBody.create(requestBody.toString(), JSON);
            Request request = new Request.Builder()
                    .url(checkUrl)
                    .post(body)
                    .build();
            
            try (Response response = getClient().newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("合规检测失败: HTTP {}", response.code());
                    // 返回 null 表示检测服务不可用
                    return null;
                }
                
                String responseBody = response.body().string();
                JSONObject result = JSONUtil.parseObj(responseBody);
                
                log.info("合规检测完成: content length={}, result={}", 
                        content.length(), result.getStr("result"));
                
                return result;
            }
        } catch (IOException e) {
            log.error("合规检测异常: {}", e.getMessage());
            // 检测失败时，返回 null 表示服务不可用
            return null;
        }
    }
    
    /**
     * 获取检测服务不可用时的结果（用于前端显示）
     * 
     * @deprecated 不再使用默认通过结果，改为返回 null 并保持 UNCHECKED 状态
     */
    @Deprecated
    private JSONObject getDefaultPassResult() {
        JSONObject result = new JSONObject();
        result.set("result", "UNCHECKED");
        result.set("risk_level", "UNKNOWN");
        result.set("risk_categories", "");
        result.set("confidence_score", 0.0);
        result.set("detail", "检测服务不可用");
        return result;
    }
    

}
