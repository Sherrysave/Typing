package com.sjt.typing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sjt.typing.common.BusinessException;
import com.sjt.typing.common.IdUtil;
import com.sjt.typing.common.JsonUtil;
import com.sjt.typing.common.ResultCode;
import com.sjt.typing.dto.req.PsaReq;
import com.sjt.typing.dto.vo.AdminUidVO;
import com.sjt.typing.dto.vo.BlocklistVO;
import com.sjt.typing.dto.vo.LogVO;
import com.sjt.typing.dto.vo.PsaVO;
import com.sjt.typing.entity.AdminUid;
import com.sjt.typing.entity.Blocklist;
import com.sjt.typing.entity.OpLogRecord;
import com.sjt.typing.entity.Psa;
import com.sjt.typing.entity.User;
import com.sjt.typing.mapper.AdminUidMapper;
import com.sjt.typing.mapper.BlocklistMapper;
import com.sjt.typing.mapper.OpLogMapper;
import com.sjt.typing.mapper.PsaMapper;
import com.sjt.typing.mapper.UserMapper;
import com.sjt.typing.service.AdminService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台管理业务实现。
 */
@Service
public class AdminServiceImpl implements AdminService {

    /** 公告缓存 Key，与 ConfigServiceImpl 保持一致 */
    private static final String KEY_PSA = "psa:active";

    private final BlocklistMapper blocklistMapper;
    private final AdminUidMapper adminUidMapper;
    private final PsaMapper psaMapper;
    private final OpLogMapper opLogMapper;
    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;

    public AdminServiceImpl(BlocklistMapper blocklistMapper,
                            AdminUidMapper adminUidMapper,
                            PsaMapper psaMapper,
                            OpLogMapper opLogMapper,
                            UserMapper userMapper,
                            StringRedisTemplate redisTemplate) {
        this.blocklistMapper = blocklistMapper;
        this.adminUidMapper = adminUidMapper;
        this.psaMapper = psaMapper;
        this.opLogMapper = opLogMapper;
        this.userMapper = userMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ban(String uid, String reason, String adminUid) {
        User user = userMapper.selectById(uid);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        long now = System.currentTimeMillis();

        Blocklist existing = blocklistMapper.selectById(uid);
        if (existing == null) {
            Blocklist entity = new Blocklist();
            entity.setUid(uid);
            entity.setReason(reason);
            entity.setBannedBy(adminUid);
            entity.setTimestamp(now);
            blocklistMapper.insert(entity);
        } else {
            // 已有记录则只刷新原因与时间，避免主键冲突
            Blocklist update = new Blocklist();
            update.setUid(uid);
            update.setReason(reason);
            update.setBannedBy(adminUid);
            update.setTimestamp(now);
            blocklistMapper.updateById(update);
        }

        User updateUser = new User();
        updateUser.setUid(uid);
        updateUser.setBanned(true);
        userMapper.updateById(updateUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unban(String uid) {
        if (blocklistMapper.selectById(uid) != null) {
            blocklistMapper.deleteById(uid);
        }
        User updateUser = new User();
        updateUser.setUid(uid);
        updateUser.setBanned(false);
        userMapper.updateById(updateUser);
    }

    @Override
    public Page<BlocklistVO> blocklist(int page, int size) {
        Page<Blocklist> result = blocklistMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Blocklist>().orderByDesc(Blocklist::getTimestamp));
        Map<String, String> names = loadNames(result.getRecords().stream().map(Blocklist::getUid).toList());

        Page<BlocklistVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(row -> {
            BlocklistVO vo = new BlocklistVO();
            vo.setUid(row.getUid());
            vo.setName(names.get(row.getUid()));
            vo.setReason(row.getReason());
            vo.setBannedBy(row.getBannedBy());
            vo.setTimestamp(row.getTimestamp());
            return vo;
        }).toList());
        return voPage;
    }

    @Override
    public PsaVO createPsa(PsaReq req) {
        long now = System.currentTimeMillis();
        Psa entity = new Psa();
        entity.setId(IdUtil.randomId());
        entity.setMessage(req.getMessage());
        entity.setSticky(Boolean.TRUE.equals(req.getSticky()));
        entity.setCreatedAt(now);
        entity.setExpiresAt(req.getExpiresAt());
        psaMapper.insert(entity);

        // 主动删缓存（Cache-Aside），否则新公告最长要等一个 TTL 才能被读到
        redisTemplate.delete(KEY_PSA);

        PsaVO vo = new PsaVO();
        vo.setId(entity.getId());
        vo.setMessage(entity.getMessage());
        vo.setSticky(entity.getSticky());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setExpiresAt(entity.getExpiresAt());
        return vo;
    }

    @Override
    public Page<LogVO> logs(int page, int size) {
        Page<OpLogRecord> result = opLogMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<OpLogRecord>().orderByDesc(OpLogRecord::getTimestamp));
        Page<LogVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(row -> {
            LogVO vo = new LogVO();
            vo.setId(row.getId());
            vo.setUid(row.getUid());
            vo.setAction(row.getAction());
            vo.setTarget(row.getTarget());
            vo.setDetail(JsonUtil.parse(row.getDetail(), Object.class));
            vo.setIp(row.getIp());
            vo.setTimestamp(row.getTimestamp());
            return vo;
        }).toList());
        return voPage;
    }

    @Override
    public List<AdminUidVO> admins() {
        List<AdminUid> rows = adminUidMapper.selectList(
                new LambdaQueryWrapper<AdminUid>().orderByAsc(AdminUid::getTimestamp));
        Map<String, String> names = loadNames(rows.stream().map(AdminUid::getUid).toList());
        return rows.stream().map(row -> {
            AdminUidVO vo = new AdminUidVO();
            vo.setUid(row.getUid());
            vo.setName(names.get(row.getUid()));
            vo.setAddedBy(row.getAddedBy());
            vo.setTimestamp(row.getTimestamp());
            return vo;
        }).toList();
    }

    @Override
    public void addAdmin(String uid, String addedBy) {
        User user = userMapper.selectById(uid);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        if (adminUidMapper.selectById(uid) != null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "该用户已是管理员");
        }
        AdminUid entity = new AdminUid();
        entity.setUid(uid);
        entity.setAddedBy(addedBy);
        entity.setTimestamp(System.currentTimeMillis());
        adminUidMapper.insert(entity);
    }

    @Override
    public void removeAdmin(String uid, String operator) {
        if (uid.equals(operator)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "不能移除自己的管理员身份");
        }
        if (adminUidMapper.selectById(uid) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "该用户不是管理员");
        }
        adminUidMapper.deleteById(uid);
    }

    /** 批量查用户名，返回 uid → name */
    private Map<String, String> loadNames(List<String> uids) {
        if (uids == null || uids.isEmpty()) {
            return Collections.emptyMap();
        }
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>().in(User::getUid, uids));
        Map<String, String> map = new HashMap<>(users.size());
        for (User user : users) {
            map.put(user.getUid(), user.getName());
        }
        return map;
    }
}
