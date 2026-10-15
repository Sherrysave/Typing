package com.sjt.typing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户预设实体，对应表 t_preset（对应 Monkeytype dal/preset.ts）。
 */
@Data
@TableName("t_preset")
public class Preset {

    @TableId(type = IdType.INPUT)
    private String id;

    private String uid;

    private String name;

    /** 预设配置（JSON 字符串） */
    private String config;

    private Long createdAt;

    private Long updatedAt;
}
