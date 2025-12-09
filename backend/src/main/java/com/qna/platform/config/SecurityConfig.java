package com.qna.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 安全配置类
 * 
 * @author QnA Platform
 */
@Configuration
public class SecurityConfig {
    
    /**
     * 密码加密器
     * 使用BCrypt强哈希算法
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
