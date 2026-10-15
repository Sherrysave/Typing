package com.sjt.typing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 操作日志实体，对应表 t_log（对应 Monkeytype dal/logs.ts）。
 * 所有后台管理动作都留痕，支撑申诉与追责。
 */
@Data
@TableName("t_log")
public class OpLogRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作人 uid */
    private String uid;

    /** 动作标识，如 BAN_USER / APPROVE_QUOTE */
    private String action;

    /** 操作对象标识 */
    private String target;

    /** 附加信息（JSON 字符串） */
    private String detail;

    private String ip;

    private Long timestamp;
}
