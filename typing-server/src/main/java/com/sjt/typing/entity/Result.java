package com.sjt.typing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 打字成绩实体，对应表 t_result（核心表）。
 * 所有 JSON 列以 String 承载，由应用层用 Jackson 序列化/反序列化。
 */
@Data
@TableName("t_result")
public class Result {

    /** 主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    private String uid;

    /** 前端计算的 WPM */
    private BigDecimal wpm;

    /** 含错误击键的原始 WPM */
    private BigDecimal rawWpm;

    /** 准确率 % */
    private BigDecimal acc;

    /** 一致性 % */
    private BigDecimal consistency;

    /** 玩法：time/words/quote/code/weak */
    private String mode;

    /** 玩法参数：15/30/60/120 或词数 */
    private String mode2;

    /** 语言 */
    private String language;

    /** 是否含标点 */
    private Boolean punctuation;

    /** 是否含数字 */
    private Boolean numbers;

    /** 是否懒人模式 */
    private Boolean lazyMode;

    /** funbox 模式名 */
    private String funbox;

    /** 实际用时（秒） */
    private BigDecimal testDuration;

    /** 重启次数 */
    private Integer restartCount;

    /** 未完成测试次数 */
    private Integer incompleteTests;

    /** 挂机毫秒数 */
    private Integer afkDuration;

    /** 是否中途放弃 */
    private Boolean bailedOut;

    /** 字符统计 JSON：{correct,incorrect,extra,missed} */
    private String charStats;

    /** 相邻击键间隔分布 JSON */
    private String keySpacingStats;

    /** 按键按下时长分布 JSON */
    private String keyDurationStats;

    /** 每秒采样 JSON，用于复现图表 */
    private String chartData;

    /** 反作弊命中项 JSON */
    private String flags;

    /** 标签 JSON */
    private String tags;

    /** 是否个人最好成绩 */
    private Boolean isPb;

    /** 有效性：1有效 0待审 -1拒绝 */
    private Integer validity;

    /** 客户端完成时间，用于排序与防重放 */
    private Long clientTimestamp;

    /** 服务端落库时间 */
    private Long serverTimestamp;
}
