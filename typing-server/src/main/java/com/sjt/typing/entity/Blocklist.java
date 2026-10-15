package com.sjt.typing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 封禁名单实体，对应表 t_blocklist（对应 Monkeytype dal/blocklist.ts）。
 */
@Data
@TableName("t_blocklist")
public class Blocklist {

    /** 被封禁用户 uid（主键） */
    @TableId(type = IdType.INPUT)
    private String uid;

    private String reason;

    /** 操作管理员 uid */
    private String bannedBy;

    private Long timestamp;
}
