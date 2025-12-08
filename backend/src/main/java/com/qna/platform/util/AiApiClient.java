package com.qna.platform.util;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.qna.platform.entity.ApiConfig;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI API客户端 - 统一调用各种大模型API
 *
 * @author QnA Platform
 */
@Component
public class AiApiClient {

    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    /**
     * 调用AI API
     *
     * @param apiConfig API配置
     * @param messages 消息列表
     * @return AI响应内容
     */
    public String callAiApi(ApiConfig apiConfig, List<Map<String, String>> messages) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(apiConfig.getTimeout()))
                .readTimeout(Duration.ofSeconds(apiConfig.getTimeout()))
                .writeTimeout(Duration.ofSeconds(apiConfig.getTimeout()))
                .build();

        // 根据不同的provider构建请求
        String requestBody = buildRequestBody(apiConfig, messages);
        
        Request.Builder requestBuilder = new Request.Builder()
                .url(apiConfig.getApiEndpoint())
                .post(RequestBody.create(requestBody, JSON_MEDIA_TYPE));

        // 添加认证头
        if (apiConfig.getApiKey() != null && !apiConfig.getApiKey().isEmpty()) {
            switch (apiConfig.getProvider().toUpperCase()) {
                case "OPENAI":
                    requestBuilder.addHeader("Authorization", "Bearer " + apiConfig.getApiKey());
                    break;
                case "ANTHROPIC":
                    requestBuilder.addHeader("x-api-key", apiConfig.getApiKey());
                    requestBuilder.addHeader("anthropic-version", "2023-06-01");
                    break;
                default:
                    requestBuilder.addHeader("Authorization", "Bearer " + apiConfig.getApiKey());
            }
        }

        Request request = requestBuilder.build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("API调用失败: " + response.code() + " - " + response.message());
            }

            String responseBody = response.body().string();
            return parseResponse(apiConfig.getProvider(), responseBody);
        }
    }

    /**
     * 构建请求体
     */
    private String buildRequestBody(ApiConfig apiConfig, List<Map<String, String>> messages) {
        Map<String, Object> requestBody = new HashMap<>();

        switch (apiConfig.getProvider().toUpperCase()) {
            case "OPENAI":
            case "LOCAL":
                requestBody.put("model", apiConfig.getModelName());
                requestBody.put("messages", messages);
                requestBody.put("max_tokens", apiConfig.getMaxTokens());
                requestBody.put("temperature", apiConfig.getTemperature());
                break;

            case "ANTHROPIC":
                requestBody.put("model", apiConfig.getModelName());
                requestBody.put("max_tokens", apiConfig.getMaxTokens());
                requestBody.put("temperature", apiConfig.getTemperature());
                
                // Claude使用不同的消息格式
                List<Map<String, String>> claudeMessages = new ArrayList<>();
                String systemMessage = null;
                
                for (Map<String, String> msg : messages) {
                    if ("system".equals(msg.get("role"))) {
                        systemMessage = msg.get("content");
                    } else {
                        claudeMessages.add(msg);
                    }
                }
                
                requestBody.put("messages", claudeMessages);
                if (systemMessage != null) {
                    requestBody.put("system", systemMessage);
                }
                break;

            default:
                // 默认使用OpenAI格式
                requestBody.put("model", apiConfig.getModelName());
                requestBody.put("messages", messages);
                requestBody.put("max_tokens", apiConfig.getMaxTokens());
                requestBody.put("temperature", apiConfig.getTemperature());
        }

        return JSONUtil.toJsonStr(requestBody);
    }

    /**
     * 解析响应
     */
    private String parseResponse(String provider, String responseBody) {
        JSONObject jsonResponse = JSONUtil.parseObj(responseBody);

        switch (provider.toUpperCase()) {
            case "OPENAI":
            case "LOCAL":
                return jsonResponse.getByPath("choices[0].message.content", String.class);

            case "ANTHROPIC":
                return jsonResponse.getByPath("content[0].text", String.class);

            default:
                // 尝试多种可能的路径
                String content = jsonResponse.getByPath("choices[0].message.content", String.class);
                if (content == null) {
                    content = jsonResponse.getByPath("content[0].text", String.class);
                }
                if (content == null) {
                    content = jsonResponse.getStr("content");
                }
                return content;
        }
    }
}
