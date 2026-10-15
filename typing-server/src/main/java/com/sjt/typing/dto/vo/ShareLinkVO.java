package com.sjt.typing.dto.vo;

import lombok.Data;

/**
 * 分享短链响应体。
 */
@Data
public class ShareLinkVO {

    /** 成绩主键 */
    private Long resultId;

    /** 短码，对应 Redis share:code:{code} */
    private String code;

    /** 完整可访问链接，由 typing.share.base-url 拼接短码得到 */
    private String url;
}
