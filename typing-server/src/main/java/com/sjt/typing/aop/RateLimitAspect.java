package com.sjt.typing.aop;

import com.sjt.typing.common.BusinessException;
import com.sjt.typing.common.RequestUtil;
import com.sjt.typing.common.ResultCode;
import com.sjt.typing.security.UserContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;

/**
 * 限流切面：{@code INCR} + {@code EXPIRE} 的固定窗口计数，对标 Monkeytype 的 rate-limiter-flexible。
 *
 * <p>用 Redis 而非本地计数，是为了在将来多实例部署时仍然全局生效；
 * 计数 key 的 TTL 只设在窗口内的第一次请求上，避免每次 INCR 都把窗口往后推（否则永不淘汰）。</p>
 */
@Aspect
@Component
public class RateLimitAspect {

    private static final String KEY_PREFIX = "rl:";

    private final StringRedisTemplate redisTemplate;

    public RateLimitAspect(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String scope = rateLimit.key().isBlank()
                ? signature.getDeclaringType().getSimpleName() + "." + signature.getName()
                : rateLimit.key();

        String key = KEY_PREFIX + scope + ":" + resolveSubject();

        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, Duration.ofSeconds(rateLimit.windowSeconds()));
        }
        if (count != null && count > rateLimit.limit()) {
            throw new BusinessException(ResultCode.RATE_LIMITED,
                    "操作过于频繁，请 " + rateLimit.windowSeconds() + " 秒后再试");
        }
        return joinPoint.proceed();
    }

    /**
     * 限流维度：已登录按 uid，未登录按客户端 IP。
     * <p>注意未登录时**不能**从方法参数里找 HttpServletRequest——像 /api/users/login 这类接口
     * 的参数只有请求体，拿不到 request，会退化成一个全局共享的 {@code ip:anonymous} 桶，
     * 导致「一分钟内全世界只能登录 5 次」。因此这里统一从 RequestContextHolder 取当前请求。</p>
     */
    private String resolveSubject() {
        String uid = UserContext.getUid();
        if (uid != null) {
            return "u:" + uid;
        }
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String ip = attributes == null ? null : RequestUtil.getClientIp(attributes.getRequest());
        return "ip:" + (ip == null ? "unknown" : ip);
    }
}
