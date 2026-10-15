package com.sjt.typing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sjt.typing.common.BusinessException;
import com.sjt.typing.common.ResultCode;
import com.sjt.typing.dto.req.ConnectionReq;
import com.sjt.typing.entity.Connection;
import com.sjt.typing.entity.User;
import com.sjt.typing.mapper.ConnectionMapper;
import com.sjt.typing.mapper.UserMapper;
import com.sjt.typing.service.ConnectionService;
import org.springframework.stereotype.Service;

/**
 * 关注关系业务实现。
 * <p>t_connection 是 (uid, target_uid) 复合主键，重复插入会触发唯一键冲突，
 * 因此这里先查存量再决定 insert/delete，保证接口可重复调用（幂等）。</p>
 */
@Service
public class ConnectionServiceImpl implements ConnectionService {

    private final ConnectionMapper connectionMapper;
    private final UserMapper userMapper;

    public ConnectionServiceImpl(ConnectionMapper connectionMapper, UserMapper userMapper) {
        this.connectionMapper = connectionMapper;
        this.userMapper = userMapper;
    }

    @Override
    public void follow(ConnectionReq req, String uid) {
        String targetUid = req.getTargetUid();
        if (uid.equals(targetUid)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "不能关注自己");
        }
        User target = userMapper.selectById(targetUid);
        if (target == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }

        LambdaQueryWrapper<Connection> keyWrapper = new LambdaQueryWrapper<Connection>()
                .eq(Connection::getUid, uid)
                .eq(Connection::getTargetUid, targetUid);
        Connection existing = connectionMapper.selectOne(keyWrapper);

        if (Boolean.TRUE.equals(req.getFollow())) {
            if (existing == null) {
                Connection entity = new Connection();
                entity.setUid(uid);
                entity.setTargetUid(targetUid);
                entity.setFollowedAt(System.currentTimeMillis());
                connectionMapper.insert(entity);
            }
        } else if (existing != null) {
            connectionMapper.delete(keyWrapper);
        }
    }
}
