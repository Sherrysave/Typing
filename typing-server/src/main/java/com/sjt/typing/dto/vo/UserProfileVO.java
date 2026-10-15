package com.sjt.typing.dto.vo;

import lombok.Data;

import java.util.List;

/**
 * 用户主页响应体（对标 api/routes/users 的 profile）：基础资料 + 各维度最好成绩 + 关注统计。
 */
@Data
public class UserProfileVO {

    private String uid;
    private String name;
    private String bio;
    private String keyboard;
    private Long createdAt;

    /** 测试总次数 */
    private Long totalTests;

    /** 关注我的人数 */
    private Long followerCount;

    /** 我关注的人数 */
    private Long followingCount;

    /** 各维度最好成绩（来自 t_leaderboard_entry） */
    private List<LeaderboardVO> bestResults;
}
