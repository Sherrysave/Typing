package com.sjt.typing.service;

import com.sjt.typing.dto.vo.LeaderboardVO;
import com.sjt.typing.entity.Result;

import java.util.List;

/**
 * 排行榜业务：Redis ZSET 为一级存储，MySQL 冷备回填。
 */
public interface LeaderboardService {

    /**
     * 更新排行榜（先比后写，仅当新成绩更高才写入），并 upsert 冷备表。
     */
    void update(String uid, Result result);

    /** 查询某维度前 N 名 */
    List<LeaderboardVO> getTop(String mode, String mode2, String language, int limit);

    /** 查询某用户在某维度的名次（从 1 开始），无则返回 null */
    Integer getRank(String uid, String mode, String mode2, String language);

    /** 查询某用户在各维度上的最好成绩（直接读冷备表），用于个人主页展示 */
    List<LeaderboardVO> bestOf(String uid);
}
