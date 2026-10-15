package com.sjt.typing.common;

/**
 * 统一响应码。
 */
public enum ResultCode {

    /** 成功 */
    SUCCESS(0, "ok", 200),
    /** 参数错误 */
    PARAM_ERROR(400, "参数错误", 400),
    /** 未登录 / Token 无效 */
    UNAUTHORIZED(401, "未登录或登录已过期", 401),
    /** 无权限 */
    FORBIDDEN(403, "无权访问", 403),
    /** 资源不存在 */
    NOT_FOUND(404, "资源不存在", 404),
    /** 请求过于频繁 */
    RATE_LIMITED(429, "请求过于频繁", 429),
    /**
     * 重复提交：同一 clientTimestamp 已提交过，属 HTTP 409 冲突语义。
     * <p>
     * 约定：成绩的「有效 / 待审 / 拒绝」不走错误码，而由响应体 {@code data.validity}（1/0/-1）表达——
     * 因为这类请求本身是被正常处理的，且成绩会落库留证。因此这里不设「成绩被拒」类错误码。
     */
    DUP_SUBMIT(1001, "成绩已提交，请勿重复提交", 409),
    /** 业务错误（兜底） */
    BIZ_ERROR(500, "服务器内部错误", 500);

    /** 业务码：给前端判断业务结果用，与 HTTP 状态码解耦 */
    private final int code;
    private final String msg;
    /** HTTP 状态码：给代理、浏览器、前端拦截器判断用，必须与语义一致 */
    private final int httpStatus;

    ResultCode(int code, String msg, int httpStatus) {
        this.code = code;
        this.msg = msg;
        this.httpStatus = httpStatus;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
