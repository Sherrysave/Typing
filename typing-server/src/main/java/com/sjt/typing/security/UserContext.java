package com.sjt.typing.security;

import com.sjt.typing.common.BusinessException;
import com.sjt.typing.common.ResultCode;

/**
 * 基于 ThreadLocal 的请求级登录用户上下文。
 * 由 AuthInterceptor 在 preHandle 写入、afterCompletion 清理（防线程池复用串号）。
 */
public final class UserContext {

    private UserContext() {
    }

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }

    /** 取当前用户 uid，未登录返回 null */
    public static String getUid() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getUid();
    }

    /** 取当前用户 uid，未登录抛 UNAUTHORIZED */
    public static String getUidOrThrow() {
        String uid = getUid();
        if (uid == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return uid;
    }
}
