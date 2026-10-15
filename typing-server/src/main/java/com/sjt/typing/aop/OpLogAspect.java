package com.sjt.typing.aop;

import com.sjt.typing.common.JsonUtil;
import com.sjt.typing.common.RequestUtil;
import com.sjt.typing.entity.OpLogRecord;
import com.sjt.typing.mapper.OpLogMapper;
import com.sjt.typing.security.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * 操作日志切面：{@link OpLog} 标注的方法成功返回后写一条 t_log。
 *
 * <p>只记成功动作而不记失败：t_log 用于「谁对谁做了什么」的追责与申诉，失败请求没有产生实际变更，
 * 记进去只会淹没真正有效的操作记录。失败请求本身已由全局异常处理器打日志。</p>
 */
@Aspect
@Component
public class OpLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OpLogAspect.class);

    /** detail 列是 JSON，截断长度避免超大报文写穿列 */
    private static final int DETAIL_MAX_LENGTH = 2000;

    private final OpLogMapper opLogMapper;
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    public OpLogAspect(OpLogMapper opLogMapper) {
        this.opLogMapper = opLogMapper;
    }

    @AfterReturning(pointcut = "@annotation(opLog)")
    public void afterReturning(JoinPoint joinPoint, OpLog opLog) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();

            OpLogRecord record = new OpLogRecord();
            record.setUid(UserContext.getUid());
            record.setAction(opLog.action());
            record.setTarget(resolveTarget(method, joinPoint.getArgs(), opLog.targetParam()));
            record.setDetail(buildDetail(signature.getParameterNames(), method, joinPoint.getArgs()));
            record.setIp(currentIp());
            record.setTimestamp(System.currentTimeMillis());
            opLogMapper.insert(record);
        } catch (Exception e) {
            // 留痕失败绝不能影响主流程
            log.warn("写操作日志失败 action={}", opLog.action(), e);
        }
    }

    /** 按参数名取 target；找不到该参数则退化为 null */
    private String resolveTarget(Method method, Object[] args, String targetParam) {
        if (targetParam == null || targetParam.isBlank()) {
            return null;
        }
        String[] names = parameterNameDiscoverer.getParameterNames(method);
        if (names == null) {
            return null;
        }
        for (int i = 0; i < names.length; i++) {
            if (targetParam.equals(names[i]) && i < args.length && args[i] != null) {
                return String.valueOf(args[i]);
            }
        }
        return null;
    }

    /** 把除 HttpServletRequest 外的入参序列化为 JSON，作为留痕详情 */
    private String buildDetail(String[] names, Method method, Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        String[] discovered = names == null ? parameterNameDiscoverer.getParameterNames(method) : names;
        java.util.Map<String, Object> map = new java.util.LinkedHashMap<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof HttpServletRequest) {
                continue;
            }
            String name = (discovered != null && i < discovered.length) ? discovered[i] : "arg" + i;
            map.put(name, args[i]);
        }
        if (map.isEmpty()) {
            return null;
        }
        String json = JsonUtil.toJson(map);
        if (json != null && json.length() > DETAIL_MAX_LENGTH) {
            return json.substring(0, DETAIL_MAX_LENGTH);
        }
        return json;
    }

    private String currentIp() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : RequestUtil.getClientIp(attributes.getRequest());
    }
}
