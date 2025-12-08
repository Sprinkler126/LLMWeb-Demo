package com.qna.platform.annotation;

import java.lang.annotation.*;

/**
 * 角色校验注解
 * 用于Controller方法，标记需要的角色
 *
 * @author QnA Platform
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {
    
    /**
     * 需要的角色代码
     */
    String[] value() default {};
    
    /**
     * 是否需要所有角色（AND），默认false表示只需要其中一个角色（OR）
     */
    boolean requireAll() default false;
}
