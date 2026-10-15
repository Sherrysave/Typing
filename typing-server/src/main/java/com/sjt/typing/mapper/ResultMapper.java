package com.sjt.typing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sjt.typing.entity.Result;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * 成绩数据访问，对应 dal/result.ts。
 */
@Mapper
public interface ResultMapper extends BaseMapper<Result> {

    /**
     * 查询某用户在指定维度（mode/mode2/language）下的历史最高 WPM（仅统计有效成绩）。
     * 无记录时返回 null。
     */
    BigDecimal selectMaxWpm(@Param("uid") String uid,
                            @Param("mode") String mode,
                            @Param("mode2") String mode2,
                            @Param("language") String language);
}
