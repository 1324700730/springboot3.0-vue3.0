package com.colorone.system.controller;

import com.colorone.common.constant.Constants;
import com.colorone.common.constant.LoginLogInfo;
import com.colorone.common.constant.RedisPrefix;
import com.colorone.common.domain.auth.LoginBody;
import com.colorone.common.domain.core.RequestResult;
import com.colorone.common.frame.aspect.annotation.ApiExtension;
import com.colorone.common.frame.aspect.enums.PermitType;
import com.colorone.common.frame.asyncTask.AsyncFactory;
import com.colorone.common.frame.asyncTask.AsyncTaskManager;
import com.colorone.common.frame.exception.CaptchaException;
import com.colorone.common.frame.redis.RedisHelper;
import com.colorone.common.utils.SecurityUtils;
import com.colorone.system.domain.entity.BaseUser;
import com.colorone.system.service.BaseUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.regex.Pattern;

/**
 * 用户注册控制器
 */
@RestController
@RequestMapping("/register")
public class RegisterController {

    @Autowired
    private BaseUserService baseUserService;

    @Autowired
    RedisHelper redisHelper;

    /**
     * 验证码是否开启
     */
    @Value("${captcha.enabled}")
    private String captchaEnabled;

    /**
     * 验证密码强度
     * 要求：
     * 1. 至少包含一个数字
     * 2. 至少包含一个小写字母
     * 3. 至少包含一个大写字母
     * 4. 至少包含一个特殊字符
     * 5. 长度在8-20位之间
     *
     * @param password 密码
     * @return 是否符合要求
     */
    private boolean isPasswordValid(String password) {
        // 密码长度在8-20位之间
        if (password == null || password.length() < 8 || password.length() > 20) {
            return false;
        }

        // 包含至少一个数字
        if (!Pattern.compile(".*\\d+.*").matcher(password).matches()) {
            return false;
        }

        // 包含至少一个小写字母
        if (!Pattern.compile(".*[a-z]+.*").matcher(password).matches()) {
            return false;
        }

        // 包含至少一个大写字母
        if (!Pattern.compile(".*[A-Z]+.*").matcher(password).matches()) {
            return false;
        }

        // 包含至少一个特殊字符
        if (!Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]+.*").matcher(password).matches()) {
            return false;
        }

        return true;
    }

    /**
     * 用户注册接口
     *
     * @param registerBody 注册信息
     * @return 注册结果
     */
    @PostMapping("/user")
    @ApiExtension(name = "用户注册接口", permitType = PermitType.ANONYMOUS)
    public RequestResult register(@RequestBody LoginBody registerBody) throws CaptchaException {
        // 验证码是否有效
        if ("true".equals(captchaEnabled)) {
            String codeValue = redisHelper.getObject(RedisPrefix.REDIS_CAPTCHA_CODE + registerBody.getCode());
            if (codeValue == null || !codeValue.equals(registerBody.getCaptcha())) {
                AsyncTaskManager.getInstance().execute(AsyncFactory.setLogging(registerBody.getUserName(), Constants.FAIL, LoginLogInfo.CAPTCHA_INVALID));
                throw new CaptchaException(LoginLogInfo.CAPTCHA_INVALID);
            }
            // 验证完成清除缓存的验证码
            redisHelper.clearObject(RedisPrefix.REDIS_CAPTCHA_CODE + registerBody.getCode());
        }

        // 验证密码强度
        if (!isPasswordValid(registerBody.getPassword())) {
            return RequestResult.error("密码必须包含数字、大小写字母和特殊字符，长度在8-20位之间");
        }

        // 检查用户名是否已存在
        BaseUser user = new BaseUser();
        user.setUserName(registerBody.getUserName());
        if (baseUserService.checkUserName(user)) {
            return RequestResult.error("注册失败，该用户名已被使用");
        }

        // 检查邮箱是否已存在
        user.setEmail(registerBody.getEmail());
        if (registerBody.getEmail() != null && !registerBody.getEmail().isEmpty() && baseUserService.checkUserEmail(user)) {
            return RequestResult.error("注册失败，该邮箱已被使用");
        }

        // 检查手机号是否已存在
        user.setPhone(registerBody.getPhone());
        if (registerBody.getPhone() != null && !registerBody.getPhone().isEmpty() && baseUserService.checkUserPhone(user)) {
            return RequestResult.error("注册失败，该手机号已被使用");
        }

        // 设置用户信息
        // 使用前端提供的昵称，如果没有提供则使用用户名
        user.setNickName(registerBody.getNickName() != null && !registerBody.getNickName().isEmpty() 
                ? registerBody.getNickName() : registerBody.getUserName());
        
        // 设置密码（服务层会进行加密）
        user.setPassword(registerBody.getPassword());
        user.setStatus(0); // 默认状态为正常
        
        // 设置默认角色（普通用户角色）
        Long[] roles = {2L}; // 2L是普通用户角色ID
        user.setRoles(roles);
        
        // 手动设置创建者和更新者，避免自动填充时出现异常
        user.setCreateBy("system");
        user.setUpdateBy("system");
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());

        // 添加用户
        int result = baseUserService.addBaseUser(user);
        if (result > 0) {
            AsyncTaskManager.getInstance().execute(AsyncFactory.setLogging(user.getUserName(), Constants.SUCCESS, "用户注册成功"));
            // 输出原始密码和加密后的密码，方便调试
            System.out.println("注册成功 - 用户名: " + user.getUserName() + ", 原始密码: " + registerBody.getPassword());
            System.out.println("加密后的密码: " + user.getPassword());
            return RequestResult.success("注册成功");
        } else {
            return RequestResult.error("注册失败，请稍后重试");
        }
    }
} 