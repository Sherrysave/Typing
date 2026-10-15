package com.sjt.typing.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sjt.typing.common.BusinessException;
import com.sjt.typing.common.ResultCode;
import com.sjt.typing.entity.AdminUid;
import com.sjt.typing.mapper.AdminUidMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 后台鉴权拦截器：拦截 {@code /api/admin/**}，对标方案 10.2 的 AdminAuthFilter。
 *
 * <p><b>Deny by default</b>：不依赖任何「默认管理员」或配置项，必须同时满足
 * 「已登录」且「uid 命中 t_admin_uid 白名单」才放行；白名单为空则所有后台请求一律 403。
 * 这样即使配置文件被泄露或被随意修改，也无法提权。</p>
 */
public class AdminAuthInterceptor implements HandlerInterceptor {

    private final AdminUidMapper adminUidMapper;

    public AdminAuthInterceptor(AdminUidMapper adminUidMapper) {
        this.adminUidMapper = adminUidMapper;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        // 1. 必须先登录（AuthInterceptor 已在更早的注册顺序中解析并注入 UserContext）
        LoginUser loginUser = UserContext.get();
        if (loginUser == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        // 2. 再校验管理员白名单，命中才放行
        Long count = adminUidMapper.selectCount(
                new LambdaQueryWrapper<AdminUid>().eq(AdminUid::getUid, loginUser.getUid()));
        if (count == null || count == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "需要管理员权限");
        }
        return true;
    }
}
