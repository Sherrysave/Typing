package com.sjt.typing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sjt.typing.entity.LeaderboardEntry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 排行榜冷备数据访问，对应 dal/leaderboards.ts。
 */
@Mapper
public interface LeaderboardEntryMapper extends BaseMapper<LeaderboardEntry> {

    /**
     * 按维度查询冷备榜前 N 名（wpm 降序）。
     * limit 为程序内部常量，非用户输入。
     */
    List<LeaderboardEntry> selectTopByScope(@Param("mode") String mode,
                                            @Param("mode2") String mode2,
                                            @Param("language") String language,
                                            @Param("limit") int limit);
}
