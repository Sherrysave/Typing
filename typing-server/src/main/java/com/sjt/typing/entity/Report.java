package com.sjt.typing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 举报实体，对应表 t_report（对应 Monkeytype dal/report.ts）。
 */
@Data
@TableName("t_report")
public class Report {

    @TableId(type = IdType.INPUT)
    private String id;

    /** 举报人 uid */
    private String uid;

    /** 被举报用户 uid */
    private String targetUid;

    /** 被举报成绩 id */
    private Long resultId;

    private String reason;

    private String description;

    /** 0 待处理 / 1 已处理 / 2 驳回 */
    private Integer status;

    private Long timestamp;
}
