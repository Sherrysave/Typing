package com.sjt.typing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 排行榜冷备条目，对应表 t_leaderboard_entry（每人每维度最好成绩）。
 */
@Data
@TableName("t_leaderboard_entry")
public class LeaderboardEntry {

    /** 主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    private String mode;

    private String mode2;

    private String language;

    private String uid;

    /** 指向 t_result.id */
    private Long resultId;

    private BigDecimal wpm;

    private BigDecimal rawWpm;

    private BigDecimal acc;

    private BigDecimal consistency;

    /** 成绩时间 epoch 毫秒 */
    private Long timestamp;
}
