package com.sjt.typing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sjt.typing.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户数据访问，对应 dal/user.ts。
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
