package com.sjt.typing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sjt.typing.entity.Quote;
import org.apache.ibatis.annotations.Mapper;

/**
 * 名言库数据访问（对应 dal/quote.ts）。
 */
@Mapper
public interface QuoteMapper extends BaseMapper<Quote> {
}
