package com.qna.platform.service;

import com.qna.platform.dto.LoginDTO;
import java.util.Map;

/**
 * 认证服务接口
 *
 * @author QnA Platform
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return 包含token和用户信息的Map
     */
    Map<String, Object> login(LoginDTO loginDTO);

    /**
     * 用户注册
     *
     * @param loginDTO 注册信息
     * @return 注册结果
     */
    boolean register(LoginDTO loginDTO);
}
