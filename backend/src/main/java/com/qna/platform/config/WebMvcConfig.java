package com.qna.platform.config;

import com.qna.platform.interceptor.PermissionInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * 注册拦截器
 *
 * @author QnA Platform
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    
    private final PermissionInterceptor permissionInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        System.out.println("=================================================");
        System.out.println("🔧 配置拦截器 - 排除路径：");
        System.out.println("   - /auth/**");
        System.out.println("   - /export/**");
        System.out.println("   - /error");
        System.out.println("   - /swagger-ui/**");
        System.out.println("   - /v3/api-docs/**");
        System.out.println("=================================================");
        
        // 注意：先排除路径，再添加拦截模式
        registry.addInterceptor(permissionInterceptor)
                .excludePathPatterns(
                        "/auth/**",              // 认证接口（context-path 已去除）
                        "/export/**",            // 导出接口（通过 URL token 验证）
                        "/error",                // 错误页面
                        "/swagger-ui/**",        // Swagger UI
                        "/v3/api-docs/**"       // API 文档
                )
                .addPathPatterns("/**");         // 拦截所有其他路径
    }
}
