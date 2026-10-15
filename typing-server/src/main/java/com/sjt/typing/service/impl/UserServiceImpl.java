package com.sjt.typing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sjt.typing.common.BusinessException;
import com.sjt.typing.common.IdUtil;
import com.sjt.typing.common.ResultCode;
import com.sjt.typing.dto.req.LoginReq;
import com.sjt.typing.dto.req.RefreshReq;
import com.sjt.typing.dto.req.SignupReq;
import com.sjt.typing.dto.req.UpdateProfileReq;
import com.sjt.typing.dto.vo.LoginVO;
import com.sjt.typing.dto.vo.UserProfileVO;
import com.sjt.typing.dto.vo.UserVO;
import com.sjt.typing.entity.AdminUid;
import com.sjt.typing.entity.Connection;
import com.sjt.typing.entity.User;
import com.sjt.typing.mapper.AdminUidMapper;
import com.sjt.typing.mapper.ConnectionMapper;
import com.sjt.typing.mapper.UserMapper;
import com.sjt.typing.security.JwtUtil;
import com.sjt.typing.security.RefreshTokenStore;
import com.sjt.typing.service.LeaderboardService;
import com.sjt.typing.service.ResultService;
import com.sjt.typing.service.UserService;
import io.jsonwebtoken.Claims;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户业务实现。
 */
@Service
public class UserServiceImpl implements UserService {

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final RefreshTokenStore refreshTokenStore;
    private final AdminUidMapper adminUidMapper;
    private final ConnectionMapper connectionMapper;
    private final LeaderboardService leaderboardService;
    private final ResultService resultService;

    public UserServiceImpl(UserMapper userMapper,
                           JwtUtil jwtUtil,
                           RefreshTokenStore refreshTokenStore,
                           AdminUidMapper adminUidMapper,
                           ConnectionMapper connectionMapper,
                           LeaderboardService leaderboardService,
                           ResultService resultService) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
        this.refreshTokenStore = refreshTokenStore;
        this.adminUidMapper = adminUidMapper;
        this.connectionMapper = connectionMapper;
        this.leaderboardService = leaderboardService;
        this.resultService = resultService;
    }

    @Override
    public LoginVO signup(SignupReq req) {
        // 用户名唯一性
        Long nameCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getName, req.getName()));
        if (nameCount != null && nameCount > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户名已被占用");
        }
        // 邮箱唯一性
        Long emailCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getEmail, req.getEmail()));
        if (emailCount != null && emailCount > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "邮箱已被注册");
        }

        long now = System.currentTimeMillis();
        User user = new User();
        user.setUid(IdUtil.randomId());
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPasswordHash(PASSWORD_ENCODER.encode(req.getPassword()));
        user.setLbOptOut(false);
        user.setBanned(false);
        user.setCreatedAt(now);
        user.setLastLoginAt(now);
        userMapper.insert(user);

        return buildLoginVO(user);
    }

    @Override
    public LoginVO login(LoginReq req) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getName, req.getAccount())
                .or()
                .eq(User::getEmail, req.getAccount())
                .last("LIMIT 1"));
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "账号或密码错误");
        }
        if (!PASSWORD_ENCODER.matches(req.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "账号或密码错误");
        }
        if (Boolean.TRUE.equals(user.getBanned())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已被封禁");
        }

        // 刷新最近登录时间
        User update = new User();
        update.setUid(user.getUid());
        update.setLastLoginAt(System.currentTimeMillis());
        userMapper.updateById(update);

        return buildLoginVO(user);
    }

    @Override
    public LoginVO refresh(RefreshReq req) {
        Claims claims;
        try {
            claims = jwtUtil.parse(req.getRefreshToken());
        } catch (Exception e) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "刷新令牌无效或已过期");
        }
        // 必须是 refresh 类型，防止拿 accessToken 当刷新令牌无限续期
        if (!JwtUtil.TYPE_REFRESH.equals(jwtUtil.getType(claims))) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "令牌类型错误");
        }
        String uid = jwtUtil.getUid(claims);
        String jti = jwtUtil.getJti(claims);
        // 白名单校验：登出或已被刷新置换过的旧令牌一律拒绝
        if (!refreshTokenStore.isValid(uid, jti)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "刷新令牌已失效，请重新登录");
        }
        User user = userMapper.selectById(uid);
        if (user == null || Boolean.TRUE.equals(user.getBanned())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号不可用");
        }
        // 旋转：先吊销旧令牌，再签发新的（同一 refresh 令牌只能用一次）
        refreshTokenStore.revoke(uid, jti);
        return buildLoginVO(user);
    }

    @Override
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        try {
            Claims claims = jwtUtil.parse(refreshToken);
            refreshTokenStore.revoke(jwtUtil.getUid(claims), jwtUtil.getJti(claims));
        } catch (Exception ignored) {
            // 令牌已损坏/已过期，等价于已失效，静默返回即可
        }
    }

    @Override
    public User getById(String uid) {
        if (uid == null) {
            return null;
        }
        return userMapper.selectById(uid);
    }

    @Override
    public UserVO toVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO vo = new UserVO();
        vo.setUid(user.getUid());
        vo.setName(user.getName());
        vo.setBio(user.getBio());
        vo.setKeyboard(user.getKeyboard());
        vo.setLbOptOut(user.getLbOptOut());
        vo.setCreatedAt(user.getCreatedAt());
        return vo;
    }

    @Override
    public void updateProfile(String uid, UpdateProfileReq req) {
        User existing = userMapper.selectById(uid);
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        User update = new User();
        update.setUid(uid);
        // MyBatis-Plus 默认忽略 null 字段，因此这里的 null 即「不修改」
        update.setBio(req.getBio());
        update.setKeyboard(req.getKeyboard());
        update.setLbOptOut(req.getLbOptOut());
        userMapper.updateById(update);
    }

    @Override
    public UserProfileVO profile(String uid) {
        User user = userMapper.selectById(uid);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        UserProfileVO vo = new UserProfileVO();
        vo.setUid(user.getUid());
        vo.setName(user.getName());
        vo.setBio(user.getBio());
        vo.setKeyboard(user.getKeyboard());
        vo.setCreatedAt(user.getCreatedAt());
        vo.setTotalTests(resultService.countByUid(uid));
        vo.setFollowerCount(count(connectionMapper.selectCount(
                new LambdaQueryWrapper<Connection>().eq(Connection::getTargetUid, uid))));
        vo.setFollowingCount(count(connectionMapper.selectCount(
                new LambdaQueryWrapper<Connection>().eq(Connection::getUid, uid))));
        vo.setBestResults(leaderboardService.bestOf(uid));
        return vo;
    }

    /**
     * 组装登录响应（双 Token）。
     * <p>admin 声明取自 t_admin_uid 白名单；该声明仅用于前端展示入口，
     * 真正的后台鉴权仍由 AdminAuthInterceptor 每次查表判定（见其类注释）。</p>
     */
    private LoginVO buildLoginVO(User user) {
        boolean admin = isAdmin(user.getUid());

        // 先生成 jti，再签令牌，最后登记 Redis 白名单——顺序不能颠倒，否则令牌签出来就不可吊销
        String jti = jwtUtil.newJti();
        LoginVO vo = new LoginVO();
        vo.setUid(user.getUid());
        vo.setName(user.getName());
        vo.setAccessToken(jwtUtil.createAccessToken(user.getUid(), user.getName(), admin));
        vo.setRefreshToken(jwtUtil.createRefreshToken(user.getUid(), jti));
        refreshTokenStore.save(user.getUid(), jti, jwtUtil.getRefreshTokenSeconds());
        return vo;
    }

    private boolean isAdmin(String uid) {
        Long count = adminUidMapper.selectCount(
                new LambdaQueryWrapper<AdminUid>().eq(AdminUid::getUid, uid));
        return count != null && count > 0;
    }

    private long count(Long value) {
        return value == null ? 0L : value;
    }
}
