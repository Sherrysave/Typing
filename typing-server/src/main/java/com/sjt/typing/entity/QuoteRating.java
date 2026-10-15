package com.sjt.typing.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 名言评分实体，对应表 t_quote_rating（对应 Monkeytype dal/quote-ratings.ts）。
 * <p>主键是 (quote_id, uid) 复合键，MyBatis-Plus 无单列主键可用，
 * 因此所有读写都走 Wrapper 条件（见 QuoteServiceImpl），不使用 selectById/updateById。</p>
 */
@Data
@TableName("t_quote_rating")
public class QuoteRating {

    private String quoteId;

    private String uid;

    /** 1~5 分 */
    private Integer rating;

    private Long timestamp;
}
