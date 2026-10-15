package com.sjt.typing.dto.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 分享页数据：一条成绩的完整自包含快照，供分享视图渲染成绩面板与复现图表。
 * <p>JSON 列在实体中是字符串，对外一律解析为结构化对象，前端拿到即可直接用。</p>
 */
@Data
public class ShareResultVO {

    /** 成绩主键 */
    private Long id;

    /** 分享者 uid */
    private String uid;

    /** 分享者用户名 */
    private String name;

    /** 净速度 WPM */
    private BigDecimal wpm;

    /** 原始速度 rawWpm */
    private BigDecimal rawWpm;

    /** 准确率 % */
    private BigDecimal acc;

    /** 一致性 % */
    private BigDecimal consistency;

    /** 玩法：time/words/quote/code/weak */
    private String mode;

    /** 玩法参数 */
    private String mode2;

    /** 语言 */
    private String language;

    /** 是否含标点 */
    private Boolean punctuation;

    /** 是否含数字 */
    private Boolean numbers;

    /** 实际用时（秒） */
    private BigDecimal testDuration;

    /** 是否个人最好成绩 */
    private Boolean isPb;

    /** 当前排行榜名次（无则 null） */
    private Integer rank;

    /** 字符统计 {correct,incorrect,extra,missed} */
    private Map<String, Object> charStats;

    /** 相邻击键间隔分布 */
    private Map<String, Object> keySpacingStats;

    /** 按键按下时长分布 */
    private Map<String, Object> keyDurationStats;

    /** 每秒采样 [{wpm,raw,err}]，用于复现图表 */
    private List<Map<String, Object>> chartData;

    /** 标签 */
    private List<String> tags;

    /** 客户端完成时间 epoch 毫秒 */
    private Long clientTimestamp;
}
