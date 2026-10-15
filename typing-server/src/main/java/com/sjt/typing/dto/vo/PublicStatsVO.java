package com.sjt.typing.dto.vo;

import lombok.Data;

/**
 * 公开统计响应体（对标 dal/public.ts，不鉴权）。
 */
@Data
public class PublicStatsVO {

    /** 总测试次数 */
    private Long totalTests;

    /** 总测试时长（秒） */
    private Long totalDurationSeconds;

    /** 注册用户数 */
    private Long totalUsers;

    /** 名言库条数 */
    private Long totalQuotes;

    /** 统计生成时间 epoch 毫秒 */
    private Long updatedAt;
}
