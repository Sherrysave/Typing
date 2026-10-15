package com.sjt.typing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sjt.typing.entity.Preset;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户预设数据访问（对应 dal/preset.ts）。
 */
@Mapper
public interface PresetMapper extends BaseMapper<Preset> {
}
