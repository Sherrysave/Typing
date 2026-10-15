package com.sjt.typing.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 关注关系实体，对应表 t_connection（对应 Monkeytype dal/connections.ts）。
 * 同样是 (uid, target_uid) 复合主键，读写走 Wrapper 条件。
 */
@Data
@TableName("t_connection")
public class Connection {

    /** 关注者 uid */
    private String uid;

    /** 被关注者 uid */
    private String targetUid;

    private Long followedAt;

    private Long lastInteraction;
}
