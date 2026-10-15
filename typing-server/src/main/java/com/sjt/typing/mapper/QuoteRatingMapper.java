package com.sjt.typing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sjt.typing.entity.QuoteRating;
import org.apache.ibatis.annotations.Mapper;

/**
 * 名言评分数据访问（对应 dal/quote-ratings.ts）。
 */
@Mapper
public interface QuoteRatingMapper extends BaseMapper<QuoteRating> {
}
