package com.sjt.typing.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求。
 */
@Data
public class SignupReq {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 32, message = "用户名长度需在 2~32 之间")
    private String name;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 64, message = "密码长度需在 8~64 之间")
    private String password;
}
