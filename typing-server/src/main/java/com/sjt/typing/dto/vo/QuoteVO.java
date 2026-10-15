package com.sjt.typing.dto.vo;

import lombok.Data;

/**
 * 名言响应体（含评分聚合）。
 */
@Data
public class QuoteVO {

    private String id;
    private String language;
    private String text;
    private String source;
    private Integer length;

    /** 平均评分，无评分为 null */
    private Double averageRating;

    /** 评分人数 */
    private Integer ratingCount;
}
