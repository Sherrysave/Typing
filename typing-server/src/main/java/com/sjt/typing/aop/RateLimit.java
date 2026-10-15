package com.sjt.typing.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口限流注解，对标方案 12 章非功能设计中的「注解 + Redis 计数」。
 *
 * <p>限流维度：登录用户按其 uid，未登录退化到客户端 IP——这样同一个人换 IP 也刷不动，
 * 而游客也不会因为共用出口 IP 相互误伤（各自按自己的 IP 计）。</p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /** 自定义限流 key 前缀，留空则使用「类名.方法名」 */
    String key() default "";

    /** 时间窗口内允许的最大请求数 */
    int limit() default 20;

    /** 时间窗口（秒） */
    int windowSeconds() default 60;
}
