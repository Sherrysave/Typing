package com.sjt.typing.dto.vo;

import lombok.Data;

/**
 * 管理员名单响应体。name 为用户名，便于后台页面展示。
 */
@Data
public class AdminUidVO {

    private String uid;
    private String name;
    private String addedBy;
    private Long timestamp;
}
