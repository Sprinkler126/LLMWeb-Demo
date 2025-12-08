package com.qna.platform.annotation;

import java.lang.annotation.*;

/**
 * 权限校验注解
 * 用于Controller方法，标记需要的权限
 *
 * @author QnA Platform
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {
    
    /**
     * 需要的权限代码
     */
    String[] value() default {};
    
    /**
     * 是否需要所有权限（AND），默认false表示只需要其中一个权限（OR）
     */
    boolean requireAll() default false;
}
