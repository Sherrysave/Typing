package com.sjt.typing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sjt.typing.entity.OpLogRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志数据访问（对应 dal/logs.ts）。
 */
@Mapper
public interface OpLogMapper extends BaseMapper<OpLogRecord> {
}
