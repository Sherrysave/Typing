package com.sjt.typing.dto.vo;

import lombok.Data;

/**
 * 用户信息响应（对外脱敏，不含密码等敏感字段）。
 */
@Data
public class UserVO {

    private String uid;
    private String name;
    private String bio;
    private String keyboard;
    private Boolean lbOptOut;
    private Long createdAt;
}
