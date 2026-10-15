package com.sjt.typing.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具：签发/解析 Access 与 Refresh Token。
 * 使用 jjwt 0.12.x 的新 API（builder().subject(...).signWith(key) / parser().verifyWith(key)），
 * 旧 API（setSubject / parserBuilder）在 0.12 已废弃。
 */
@Component
public class JwtUtil {

    /** 令牌类型声明：access */
    public static final String TYPE_ACCESS = "access";
    /** 令牌类型声明：refresh */
    public static final String TYPE_REFRESH = "refresh";

    private final SecretKey key;
    private final long accessTokenSeconds;
    private final long refreshTokenSeconds;

    public JwtUtil(@Value("${typing.jwt.secret}") String secret,
                   @Value("${typing.jwt.access-token-seconds}") long accessTokenSeconds,
                   @Value("${typing.jwt.refresh-token-seconds}") long refreshTokenSeconds) {
        // HS256 要求密钥长度 >= 32 字节
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenSeconds = accessTokenSeconds;
        this.refreshTokenSeconds = refreshTokenSeconds;
    }

    /** 签发访问令牌，携带 uid(sub)、name、admin 与类型声明 */
    public String createAccessToken(String uid, String name, boolean admin) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(uid)
                .claim("name", name)
                .claim("admin", admin)
                .claim("type", TYPE_ACCESS)
                .issuedAt(new Date(now))
                .expiration(new Date(now + accessTokenSeconds * 1000L))
                .signWith(key)
                .compact();
    }

    /**
     * 签发刷新令牌，携带 uid(sub)、类型声明与 jti。
     * <p>
     * 带 jti 是为了让刷新令牌可被主动吊销：调用方需把 (uid, jti) 存入 Redis，
     * 刷新/登出时校验并删除该键（见 RefreshTokenStore）。
     */
    public String createRefreshToken(String uid) {
        return createRefreshToken(uid, newJti());
    }

    /** 用指定 jti 签发刷新令牌：调用方需先把 (uid, jti) 存入 Redis 白名单，才能保证后续可校验/吊销 */
    public String createRefreshToken(String uid, String jti) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(uid)
                .id(jti)
                .claim("type", TYPE_REFRESH)
                .issuedAt(new Date(now))
                .expiration(new Date(now + refreshTokenSeconds * 1000L))
                .signWith(key)
                .compact();
    }

    /** 生成随机 jti（32 位十六进制，去掉 UUID 的横线） */
    public String newJti() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }

    /** 从 Claims 取令牌唯一标识 jti，可能为 null */
    public String getJti(Claims claims) {
        return claims.getId();
    }

    /** 从 Claims 取令牌类型（access / refresh） */
    public String getType(Claims claims) {
        return claims.get("type", String.class);
    }

    /** Refresh Token 有效期（秒），供写入 Redis 时对齐 TTL */
    public long getRefreshTokenSeconds() {
        return refreshTokenSeconds;
    }

    /** Access Token 有效期（秒） */
    public long getAccessTokenSeconds() {
        return accessTokenSeconds;
    }

    /** 解析并验签，返回 Claims；失败抛出 jjwt 异常，由调用方处理 */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** 从 Claims 取 uid */
    public String getUid(Claims claims) {
        return claims.getSubject();
    }
}
