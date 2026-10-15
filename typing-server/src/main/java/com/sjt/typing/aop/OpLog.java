package com.sjt.typing.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 后台操作留痕注解：被标注的方法成功执行后，往 t_log 写一条操作记录。
 * 对标方案 10.2 的「所有管理动作写 t_log」。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OpLog {

    /** 动作标识，如 BAN_USER / APPROVE_QUOTE */
    String action();

    /**
     * 作为操作对象（t_log.target）的方法参数名，如 {@code "uid"}。
     * 留空则 target 记为空——仅用于不需要定位对象的动作（如查询类）。
     */
    String targetParam() default "";
}
