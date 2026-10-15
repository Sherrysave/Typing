package com.sjt.typing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sjt.typing.entity.NewQuote;
import org.apache.ibatis.annotations.Mapper;

/**
 * 待审名言数据访问（对应 dal/new-quotes.ts）。
 */
@Mapper
public interface NewQuoteMapper extends BaseMapper<NewQuote> {
}
