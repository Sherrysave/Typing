package com.sjt.typing.security;

import com.sjt.typing.common.BusinessException;
import com.sjt.typing.common.ResultCode;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 鉴权拦截器：解析 Bearer Token 注入 UserContext，并校验 @RequireLogin。
 */
public class AuthInterceptor implements HandlerInterceptor {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    public AuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        // 1. 尝试解析 Authorization 头，解析失败静默不设置（放行由 @RequireLogin 决定）
        String header = request.getHeader(AUTH_HEADER);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length()).trim();
            try {
                Claims claims = jwtUtil.parse(token);
                String uid = jwtUtil.getUid(claims);
                Object name = claims.get("name");
                Object admin = claims.get("admin");
                LoginUser loginUser = new LoginUser(
                        uid,
                        name == null ? null : name.toString(),
                        Boolean.TRUE.equals(admin));
                UserContext.set(loginUser);
            } catch (Exception ignored) {
                // 令牌无效/过期：不设登录态，交由后续 @RequireLogin 判定
            }
        }

        // 2. 检查是否需要登录
        if (handler instanceof HandlerMethod handlerMethod) {
            boolean requireLogin = handlerMethod.hasMethodAnnotation(RequireLogin.class)
                    || handlerMethod.getBeanType().isAnnotationPresent(RequireLogin.class);
            if (requireLogin && UserContext.get() == null) {
                throw new BusinessException(ResultCode.UNAUTHORIZED);
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                Exception ex) {
        // 必须清理，防止线程池复用时上下文串号
        UserContext.clear();
    }
}
