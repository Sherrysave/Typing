package com.sjt.typing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 待审名言实体，对应表 t_new_quote（对应 Monkeytype dal/new-quotes.ts）。
 */
@Data
@TableName("t_new_quote")
public class NewQuote {

    @TableId(type = IdType.INPUT)
    private String id;

    /** 提交者 uid */
    private String uid;

    private String language;

    private String text;

    private String source;

    /** 0 待审 / 1 通过 / 2 驳回 */
    private Integer status;

    private Long timestamp;
}
