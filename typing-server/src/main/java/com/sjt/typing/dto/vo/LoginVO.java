package com.sjt.typing.dto.vo;

import lombok.Data;

/**
 * 登录响应：双 Token。
 */
@Data
public class LoginVO {

    private String uid;
    private String name;
    private String accessToken;
    private String refreshToken;
}
