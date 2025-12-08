package com.qna.platform.interceptor;

import com.qna.platform.annotation.RequirePermission;
import com.qna.platform.annotation.RequireRole;
import com.qna.platform.entity.SysPermission;
import com.qna.platform.entity.SysRole;
import com.qna.platform.mapper.PermissionMapper;
import com.qna.platform.mapper.RoleMapper;
import com.qna.platform.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

/**
 * 权限拦截器
 * 基于注解的权限校验
 *
 * @author QnA Platform
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionInterceptor implements HandlerInterceptor {
    
    private final JwtUtil jwtUtil;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        System.out.println("🛡️ 拦截器检查路径: " + uri);
        System.out.println("   Handler 类型: " + handler.getClass().getName());
        
        // 明确排除导出路径（防御性编程）
        if (uri.startsWith("/export/") || uri.equals("/export")) {
            System.out.println("   ⚠️ 导出路径，应该被 WebMvcConfig 排除，但依然进入拦截器");
            System.out.println("   ✅ 手动放行导出路径");
            return true;
        }
        
        // 如果不是方法处理器，直接放行
        if (!(handler instanceof HandlerMethod)) {
            System.out.println("   ✅ 非方法处理器，直接放行");
            return true;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        System.out.println("   方法: " + handlerMethod.getMethod().getName());
        System.out.println("   Controller: " + handlerMethod.getBeanType().getSimpleName());
        
        // 检查方法上的权限注解
        RequirePermission methodPermission = handlerMethod.getMethodAnnotation(RequirePermission.class);
        RequireRole methodRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        
        // 检查类上的权限注解
        RequirePermission classPermission = handlerMethod.getBeanType().getAnnotation(RequirePermission.class);
        RequireRole classRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        
        System.out.println("   方法权限注解: " + (methodPermission != null));
        System.out.println("   方法角色注解: " + (methodRole != null));
        System.out.println("   类权限注解: " + (classPermission != null));
        System.out.println("   类角色注解: " + (classRole != null));
        
        // 如果没有权限注解，直接放行
        if (methodPermission == null && classPermission == null && methodRole == null && classRole == null) {
            System.out.println("   ✅ 无权限注解，直接放行");
            return true;
        }
        
        // 获取用户ID
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已过期\"}");
            return false;
        }
        
        token = token.substring(7);
        Long userId;
        try {
            userId = jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\":401,\"message\":\"Token无效\"}");
            return false;
        }
        
        // 检查角色权限
        if (methodRole != null && !checkRole(userId, methodRole)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"code\":403,\"message\":\"权限不足：需要角色 " + Arrays.toString(methodRole.value()) + "\"}");
            return false;
        }
        
        if (classRole != null && !checkRole(userId, classRole)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"code\":403,\"message\":\"权限不足：需要角色 " + Arrays.toString(classRole.value()) + "\"}");
            return false;
        }
        
        // 检查操作权限
        if (methodPermission != null && !checkPermission(userId, methodPermission)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"code\":403,\"message\":\"权限不足：需要权限 " + Arrays.toString(methodPermission.value()) + "\"}");
            return false;
        }
        
        if (classPermission != null && !checkPermission(userId, classPermission)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"code\":403,\"message\":\"权限不足：需要权限 " + Arrays.toString(classPermission.value()) + "\"}");
            return false;
        }
        
        return true;
    }
    
    /**
     * 检查角色
     */
    private boolean checkRole(Long userId, RequireRole requireRole) {
        String[] requiredRoles = requireRole.value();
        if (requiredRoles.length == 0) {
            return true;
        }
        
        // 获取用户角色
        SysRole userRole = roleMapper.selectByUserId(userId);
        if (userRole == null) {
            return false;
        }
        
        // 超级管理员拥有所有权限
        if ("SUPER_ADMIN".equals(userRole.getRoleCode())) {
            return true;
        }
        
        boolean hasRole = Arrays.asList(requiredRoles).contains(userRole.getRoleCode());
        
        if (requireRole.requireAll()) {
            // 需要所有角色（对于单个用户来说，这种情况较少）
            return hasRole;
        } else {
            // 需要其中一个角色
            return hasRole;
        }
    }
    
    /**
     * 检查权限
     */
    private boolean checkPermission(Long userId, RequirePermission requirePermission) {
        String[] requiredPermissions = requirePermission.value();
        if (requiredPermissions.length == 0) {
            return true;
        }
        
        // 获取用户所有权限
        List<SysPermission> userPermissions = permissionMapper.selectByUserId(userId);
        List<String> permissionCodes = userPermissions.stream()
                .map(SysPermission::getPermissionCode)
                .toList();
        
        // 检查是否是超级管理员（通过是否有PERMISSION_CONFIG权限判断）
        if (permissionCodes.contains("PERMISSION_CONFIG")) {
            return true;
        }
        
        if (requirePermission.requireAll()) {
            // 需要所有权限
            return permissionCodes.containsAll(Arrays.asList(requiredPermissions));
        } else {
            // 需要其中一个权限
            return Arrays.stream(requiredPermissions).anyMatch(permissionCodes::contains);
        }
    }
}
