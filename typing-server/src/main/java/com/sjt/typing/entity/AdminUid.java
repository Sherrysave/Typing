package com.sjt.typing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 管理员白名单实体，对应表 t_admin_uid（对应 Monkeytype dal/admin-uids.ts）。
 * <p>权限来源必须独立成表，不写死在代码或配置里，避免提权漏洞。</p>
 */
@Data
@TableName("t_admin_uid")
public class AdminUid {

    @TableId(type = IdType.INPUT)
    private String uid;

    /** 授权人 uid */
    private String addedBy;

    private Long timestamp;
}
