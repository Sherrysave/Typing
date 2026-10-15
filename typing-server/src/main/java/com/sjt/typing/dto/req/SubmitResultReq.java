package com.sjt.typing.dto.req;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 提交成绩请求，字段与方案 7.2 的报文结构一一对应。
 * <p>
 * 这里的 {@code @DecimalMax} 上限与数据库列宽对齐（见 db/schema.sql 的 DECIMAL(7,2) / DECIMAL(6,2)）。
 * 目的是把「荒谬数值」挡在入库之前：这类值即使被判为无效成绩也要落库留证，
 * 若列宽装不下会抛 SQL 异常、变成 HTTP 500，反而丢失证据。
 */
@Data
public class SubmitResultReq {

    @NotNull(message = "wpm 不能为空")
    @DecimalMin(value = "0", message = "wpm 不能为负")
    @DecimalMax(value = "99999.99", message = "wpm 超出可记录范围")
    private BigDecimal wpm;

    @NotNull(message = "rawWpm 不能为空")
    @DecimalMin(value = "0", message = "rawWpm 不能为负")
    @DecimalMax(value = "99999.99", message = "rawWpm 超出可记录范围")
    private BigDecimal rawWpm;

    @NotNull(message = "acc 不能为空")
    @DecimalMin(value = "0", message = "acc 不能为负")
    @DecimalMax(value = "100", message = "acc 不能大于 100")
    private BigDecimal acc;

    @NotNull(message = "consistency 不能为空")
    @DecimalMin(value = "0", message = "consistency 不能为负")
    @DecimalMax(value = "100", message = "consistency 不能大于 100")
    private BigDecimal consistency;

    @NotBlank(message = "mode 不能为空")
    private String mode;

    @NotBlank(message = "mode2 不能为空")
    private String mode2;

    @NotBlank(message = "language 不能为空")
    private String language;

    /** 是否含标点 */
    private Boolean punctuation;

    /** 是否含数字 */
    private Boolean numbers;

    /** 实际用时（秒）。上限对齐 t_result.test_duration 的 DECIMAL(6,2) 列宽 */
    @DecimalMin(value = "0", message = "testDuration 不能为负")
    @DecimalMax(value = "9999.99", message = "testDuration 超出可记录范围")
    private BigDecimal testDuration;

    /** 重启次数 */
    private Integer restartCount;

    /** 未完成测试次数 */
    private Integer incompleteTests;

    /** 挂机毫秒数 */
    private Integer afkDuration;

    /** 字符统计：{correct,incorrect,extra,missed} */
    private Map<String, Integer> charStats;

    /** 相邻击键间隔分布：{average,sd} */
    private Map<String, Object> keySpacingStats;

    /** 按键按下时长分布：{average,sd} */
    private Map<String, Object> keyDurationStats;

    /** 每秒采样：[{wpm,raw,err}, ...] */
    private List<Map<String, Object>> chartData;

    /** 标签 */
    private List<String> tags;

    /** 客户端完成时间 epoch 毫秒 */
    private Long clientTimestamp;
}
