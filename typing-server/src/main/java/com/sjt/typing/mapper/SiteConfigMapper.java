package com.sjt.typing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sjt.typing.entity.SiteConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 全站配置数据访问（对应 dal/config.ts）。
 */
@Mapper
public interface SiteConfigMapper extends BaseMapper<SiteConfig> {
}
