package com.sjt.typing.security;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Refresh Token 白名单存储（对标方案 5 章 Redis 设计中的 {@code rt:{uid}:{jti}}）。
 *
 * <p>设计取舍：JWT 本身是无状态的、签发后无法撤销，因此把「已签发的 refresh 令牌」在 Redis 里
 * 建一份白名单。刷新与登出时校验并删除该键，就能让旧令牌立即失效——这是「有状态吊销 + 无状态校验」
 * 的常见折中。</p>
 */
@Component
public class RefreshTokenStore {

    private static final String PREFIX = "rt:";

    private final StringRedisTemplate redisTemplate;

    public RefreshTokenStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /** 登记一个已签发的 refresh 令牌，TTL 与令牌自身有效期对齐 */
    public void save(String uid, String jti, long ttlSeconds) {
        redisTemplate.opsForValue().set(PREFIX + uid + ":" + jti, "1", Duration.ofSeconds(ttlSeconds));
    }

    /** 校验令牌是否仍在白名单中（未被登出/未被刷新置换） */
    public boolean isValid(String uid, String jti) {
        if (uid == null || jti == null) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + uid + ":" + jti));
    }

    /** 吊销令牌（登出、或刷新后置换旧令牌） */
    public void revoke(String uid, String jti) {
        if (uid != null && jti != null) {
            redisTemplate.delete(PREFIX + uid + ":" + jti);
        }
    }
}
