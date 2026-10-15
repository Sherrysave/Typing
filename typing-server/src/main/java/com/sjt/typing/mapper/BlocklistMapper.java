package com.sjt.typing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sjt.typing.entity.Blocklist;
import org.apache.ibatis.annotations.Mapper;

/**
 * 封禁名单数据访问（对应 dal/blocklist.ts）。
 */
@Mapper
public interface BlocklistMapper extends BaseMapper<Blocklist> {
}
