package com.sjt.typing.common;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 请求工具：提取客户端 IP。
 */
public final class RequestUtil {

    private RequestUtil() {
    }

    private static final String UNKNOWN = "unknown";

    /**
     * 取客户端真实 IP。
     * <p>依次尝试常见代理头，最后回退到 {@code getRemoteAddr}。代理头可能被伪造，
     * 因此仅用于限流与日志留痕，不作为安全判定依据。</p>
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String[] headers = {"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP"};
        for (String header : headers) {
            String value = request.getHeader(header);
            if (value != null && !value.isBlank() && !UNKNOWN.equalsIgnoreCase(value)) {
                // X-Forwarded-For 形如 "client, proxy1, proxy2"，取第一个
                int comma = value.indexOf(',');
                return (comma > 0 ? value.substring(0, comma) : value).trim();
            }
        }
        return request.getRemoteAddr();
    }
}
