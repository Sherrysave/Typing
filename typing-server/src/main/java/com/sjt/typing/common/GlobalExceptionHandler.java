package com.sjt.typing.common;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理：把各类异常统一转换为 {code,msg,data} 响应体。
 * <p>
 * 注意：响应体里的业务码（code）与 HTTP 状态码是两件事，这里必须同时设置。
 * 只设业务码而 HTTP 恒为 200，会让前端拦截器、反向代理、浏览器缓存都失去判断依据。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 构造响应：业务码放 body，HTTP 状态码由 ResultCode 决定。
     * 异常自带消息为空时退回该码的默认文案，避免前端收到 msg:null。
     */
    private ResponseEntity<R<Void>> fail(ResultCode code, String msg) {
        String text = (msg == null || msg.isBlank()) ? code.getMsg() : msg;
        return ResponseEntity.status(code.getHttpStatus()).body(R.fail(code, text));
    }

    /** 业务异常：透传其 ResultCode 与消息 */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<R<Void>> handleBusiness(BusinessException e) {
        return fail(e.getResultCode(), e.getMessage());
    }

    /** @Valid 校验失败：取第一个字段错误，拼成「字段: 原因」 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<R<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError == null
                ? ResultCode.PARAM_ERROR.getMsg()
                : fieldError.getField() + ": " + fieldError.getDefaultMessage();
        return fail(ResultCode.PARAM_ERROR, msg);
    }

    /** @Validated 约束校验失败（方法参数级） */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<R<Void>> handleConstraintViolation(ConstraintViolationException e) {
        String msg = ResultCode.PARAM_ERROR.getMsg();
        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
            msg = violation.getPropertyPath() + ": " + violation.getMessage();
            break;
        }
        return fail(ResultCode.PARAM_ERROR, msg);
    }

    /** 请求体解析失败（如 JSON 格式错误） */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<R<Void>> handleNotReadable(HttpMessageNotReadableException e) {
        return fail(ResultCode.PARAM_ERROR, "请求体格式错误");
    }

    /**
     * 静态资源/路径不存在（如浏览器自动请求的 /favicon.ico）。
     * <p>
     * Spring Boot 3.2 对未命中的静态资源抛 NoResourceFoundException，属正常 404 语义，
     * 必须在此单独处理：既避免落到下面的兜底分支被当成 500 + 打印错误堆栈，
     * 也保证 HTTP 状态码为 404。
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<R<Void>> handleNoResourceFound(NoResourceFoundException e) {
        return fail(ResultCode.NOT_FOUND, null);
    }

    /** 兜底异常：必须打日志，否则线上 500 无从排查 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<R<Void>> handleException(Exception e) {
        log.error("未捕获异常", e);
        return fail(ResultCode.BIZ_ERROR, e.getMessage());
    }
}
