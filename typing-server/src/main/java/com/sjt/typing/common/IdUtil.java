package com.sjt.typing.common;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * 短随机 id 生成器。
 * <p>16 字节随机数 → Base64 URL-safe 去填充后恰好 22 个字符，与 Monkeytype 的 id/uid 长度一致；
 * 各表主键均为 VARCHAR(22)，因此统一走这里生成，避免各处各写一份。</p>
 */
public final class IdUtil {

    private IdUtil() {
    }

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /** Base62 字母表：分享短码用它生成，避免 BIGINT 主键被顺序枚举 */
    private static final char[] BASE62 =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

    /** 生成 22 位 URL-safe 随机 id */
    public static String randomId() {
        byte[] bytes = new byte[16];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** 生成指定长度的 Base62 短码（用于分享短链） */
    public static String shortCode(int length) {
        char[] chars = new char[length];
        for (int i = 0; i < length; i++) {
            chars[i] = BASE62[SECURE_RANDOM.nextInt(BASE62.length)];
        }
        return new String(chars);
    }
}
