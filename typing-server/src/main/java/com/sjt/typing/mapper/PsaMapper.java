package com.sjt.typing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sjt.typing.entity.Psa;
import org.apache.ibatis.annotations.Mapper;

/**
 * 站内公告数据访问（对应 dal/psa.ts）。
 */
@Mapper
public interface PsaMapper extends BaseMapper<Psa> {
}
