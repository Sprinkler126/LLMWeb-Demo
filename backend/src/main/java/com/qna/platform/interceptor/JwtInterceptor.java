package com.qna.platform.interceptor;

import com.qna.platform.config.JwtConfig;
import com.qna.platform.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT拦截器
 *
 * @author QnA Platform
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final JwtConfig jwtConfig;

    public JwtInterceptor(JwtUtil jwtUtil, JwtConfig jwtConfig) {
        this.jwtUtil = jwtUtil;
        this.jwtConfig = jwtConfig;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String uri = request.getRequestURI();
        System.out.println("🔑 JWT拦截器检查路径: " + uri);
        
        // 处理OPTIONS请求
        if ("OPTIONS".equals(request.getMethod())) {
            System.out.println("   ✅ OPTIONS请求，直接放行");
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }

        // 获取Token
        String token = request.getHeader(jwtConfig.getHeader());
        System.out.println("   Authorization Header: " + (token != null ? "存在" : "不存在"));
        
        if (token != null && token.startsWith(jwtConfig.getPrefix())) {
            token = token.substring(jwtConfig.getPrefix().length()).trim();

            // 验证Token
            if (jwtUtil.validateToken(token)) {
                // 将用户信息设置到请求属性中
                Long userId = jwtUtil.getUserIdFromToken(token);
                String username = jwtUtil.getUsernameFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);

                request.setAttribute("userId", userId);
                request.setAttribute("username", username);
                request.setAttribute("role", role);
                
                System.out.println("   ✅ Token有效，用户: " + username);
                return true;
            } else {
                System.out.println("   ❌ Token验证失败");
            }
        } else {
            System.out.println("   ❌ Token格式错误或不存在");
        }

        // Token无效
        System.out.println("   ⛔ 返回401 - JWT拦截器拦截");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }
}
