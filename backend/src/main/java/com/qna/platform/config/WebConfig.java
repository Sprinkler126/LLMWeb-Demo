package com.qna.platform.config;

import com.qna.platform.interceptor.JwtInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置
 *
 * @author QnA Platform
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    public WebConfig(JwtInterceptor jwtInterceptor) {
        this.jwtInterceptor = jwtInterceptor;
    }

    /**
     * 配置跨域
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 配置拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        System.out.println("=================================================");
        System.out.println("🔧 配置JWT拦截器 - 排除路径：");
        System.out.println("   - /auth/**");
        System.out.println("   - /export/session/**");
        System.out.println("   - /export/all/**");
        System.out.println("   - /error");
        System.out.println("   - /swagger-ui/**");
        System.out.println("   - /v3/api-docs/**");
        System.out.println("=================================================");
        
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/**",              // 认证接口（登录、注册）
                        "/export/session/**",    // 文件下载接口（通过 URL token 验证）
                        "/export/all/**",        // 批量导出接口（通过 URL token 验证）
                        "/export/test",          // 测试接口
                        "/error",
                        "/swagger-ui/**",
                        "/v3/api-docs/**"
                );
    }
}
