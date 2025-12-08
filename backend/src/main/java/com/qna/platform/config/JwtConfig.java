package com.qna.platform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT配置
 *
 * @author QnA Platform
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    /**
     * 密钥
     */
    private String secret;

    /**
     * 过期时间（毫秒）
     */
    private Long expiration;

    /**
     * 请求头
     */
    private String header;

    /**
     * 前缀
     */
    private String prefix;
}
