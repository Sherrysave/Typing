package com.sjt.typing.dto.vo;

import lombok.Data;

/**
 * 封禁名单响应体。
 */
@Data
public class BlocklistVO {

    private String uid;
    private String name;
    private String reason;
    private String bannedBy;
    private Long timestamp;
}
