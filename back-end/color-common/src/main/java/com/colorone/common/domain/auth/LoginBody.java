package com.colorone.common.domain.auth;

import lombok.Data;

/**
 * @author： lee
 * @email：miracleren@gmail.com
 * @date：2023/7/3
 * @备注：登录请求信息
 */
@Data
public class LoginBody {
    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户密码
     */
    private String password;

    /**
     * 验证码ID
     */
    private String code;

    /**
     * 验证码
     */
    private String captcha;
    
    /**
     * 用户昵称
     */
    private String nickName;
    
    /**
     * 用户邮箱
     */
    private String email;
    
    /**
     * 手机号码
     */
    private String phone;
}
