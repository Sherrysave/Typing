package com.sjt.typing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户实体，对应表 t_user。
 */
@Data
@TableName("t_user")
public class User {

    /** 用户ID（22 位随机串，对标 Monkeytype 的 uid） */
    @TableId(type = IdType.INPUT)
    private String uid;

    /** 用户名，唯一 */
    private String name;

    /** 邮箱，唯一 */
    private String email;

    /** BCrypt 哈希后的密码 */
    private String passwordHash;

    /** 个人简介 */
    private String bio;

    /** 常用键盘 */
    private String keyboard;

    /** 自定义个人页（JSON 列，映射为 String，应用层序列化） */
    private String profileDetails;

    /** 徽章等道具（JSON 列，映射为 String） */
    private String inventory;

    /** 是否不参与排行榜（tinyint(1) → Boolean） */
    private Boolean lbOptOut;

    /** 是否已封禁（tinyint(1) → Boolean） */
    private Boolean banned;

    /** 创建时间 epoch 毫秒 */
    private Long createdAt;

    /** 上次登录时间 epoch 毫秒 */
    private Long lastLoginAt;
}
