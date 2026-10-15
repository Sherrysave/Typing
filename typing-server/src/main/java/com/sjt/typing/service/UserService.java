package com.sjt.typing.service;

import com.sjt.typing.dto.req.LoginReq;
import com.sjt.typing.dto.req.RefreshReq;
import com.sjt.typing.dto.req.SignupReq;
import com.sjt.typing.dto.req.UpdateProfileReq;
import com.sjt.typing.dto.vo.LoginVO;
import com.sjt.typing.dto.vo.UserProfileVO;
import com.sjt.typing.dto.vo.UserVO;
import com.sjt.typing.entity.User;

/**
 * 用户业务：注册、登录、Token 刷新/登出、资料与个人主页。
 */
public interface UserService {

    /** 注册并返回登录态（含双 Token） */
    LoginVO signup(SignupReq req);

    /** 登录并签发双 Token */
    LoginVO login(LoginReq req);

    /** 用 refreshToken 换取新的双 Token（旧的 refreshToken 立即失效，防重放） */
    LoginVO refresh(RefreshReq req);

    /** 登出：吊销该 refreshToken 对应的 Redis 白名单条目 */
    void logout(String refreshToken);

    /** 按 uid 查询用户，不存在返回 null */
    User getById(String uid);

    /** 实体转对外脱敏 VO */
    UserVO toVO(User user);

    /** 修改个人资料（字段为空表示不变） */
    void updateProfile(String uid, UpdateProfileReq req);

    /** 个人主页：基础资料 + 最好成绩 + 关注统计 */
    UserProfileVO profile(String uid);
}
