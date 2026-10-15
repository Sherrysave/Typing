package com.sjt.typing.dto.vo;

import lombok.Data;

/**
 * 站内公告响应体。
 */
@Data
public class PsaVO {

    private String id;
    private String message;
    private Boolean sticky;
    private Long createdAt;
    private Long expiresAt;
}
