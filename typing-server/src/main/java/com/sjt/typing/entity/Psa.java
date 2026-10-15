package com.sjt.typing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 站内公告实体，对应表 t_psa（对应 Monkeytype dal/psa.ts）。
 */
@Data
@TableName("t_psa")
public class Psa {

    @TableId(type = IdType.INPUT)
    private String id;

    private String message;

    /** 是否置顶 */
    private Boolean sticky;

    private Long createdAt;

    /** 过期时间，null 表示永不过期 */
    private Long expiresAt;
}
