package com.sjt.typing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sjt.typing.entity.AdminUid;
import org.apache.ibatis.annotations.Mapper;

/**
 * 管理员白名单数据访问（对应 dal/admin-uids.ts）。
 */
@Mapper
public interface AdminUidMapper extends BaseMapper<AdminUid> {
}
