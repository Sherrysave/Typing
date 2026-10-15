package com.sjt.typing.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求：account 可传用户名或邮箱。
 */
@Data
public class LoginReq {

    @NotBlank(message = "账号不能为空")
    private String account;

    @NotBlank(message = "密码不能为空")
    private String password;
}
