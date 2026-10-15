package com.sjt.typing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 全站配置实体，对应表 t_config（对应 Monkeytype dal/config.ts）。
 * <p>注意：列名 {@code key} 是 MySQL 保留字，必须用反引号包裹，
 * 否则 MyBatis-Plus 生成的 {@code INSERT INTO t_config (key, ...)} 会直接语法报错。</p>
 */
@Data
@TableName("t_config")
public class SiteConfig {

    @TableId(value = "`key`", type = IdType.INPUT)
    private String key;

    /** 配置值（JSON 字符串） */
    private String value;

    private Long updatedAt;
}
