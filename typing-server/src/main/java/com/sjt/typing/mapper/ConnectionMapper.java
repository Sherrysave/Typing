package com.sjt.typing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sjt.typing.entity.Connection;
import org.apache.ibatis.annotations.Mapper;

/**
 * 关注关系数据访问（对应 dal/connections.ts）。
 */
@Mapper
public interface ConnectionMapper extends BaseMapper<Connection> {
}
