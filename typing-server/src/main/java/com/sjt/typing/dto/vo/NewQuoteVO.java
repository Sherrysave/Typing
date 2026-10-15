package com.sjt.typing.dto.vo;

import lombok.Data;

/**
 * 待审名言响应体。
 */
@Data
public class NewQuoteVO {

    private String id;
    private String uid;
    private String language;
    private String text;
    private String source;

    /** 0 待审 / 1 通过 / 2 驳回 */
    private Integer status;

    private Long timestamp;
}
