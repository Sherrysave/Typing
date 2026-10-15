package com.sjt.typing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 名言实体，对应表 t_quote（对应 Monkeytype dal/quote.ts）。
 * 仅存放「已通过审核」的名言，用户提交的先落在 t_new_quote。
 */
@Data
@TableName("t_quote")
public class Quote {

    /** 22 位随机 id */
    @TableId(type = IdType.INPUT)
    private String id;

    private String language;

    /** 名言正文 */
    private String text;

    /** 出处 */
    private String source;

    /** 字符数（冗余列，便于按长度筛选） */
    private Integer length;

    /** 审核管理员 uid */
    private String approvedBy;

    /** 创建时间 epoch 毫秒 */
    private Long timestamp;
}
