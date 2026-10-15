package com.sjt.typing.dto.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 排行榜条目响应。
 */
@Data
public class LeaderboardVO {

    /** 名次（从 1 开始） */
    private Integer rank;

    private String uid;

    private String name;

    private BigDecimal wpm;

    private BigDecimal rawWpm;

    private BigDecimal acc;

    private BigDecimal consistency;

    /** 成绩时间 epoch 毫秒 */
    private Long timestamp;
}
