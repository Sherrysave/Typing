package com.sjt.typing.dto.vo;

import lombok.Data;

import java.util.List;

/**
 * 提交成绩响应。
 */
@Data
public class SubmitResultVO {

    /** 成绩记录 id */
    private Long id;

    /** 是否个人最好成绩 */
    private Boolean isPb;

    /** 当前维度名次（无则 null） */
    private Integer rank;

    /** 有效性：1有效 0待审 -1拒绝 */
    private Integer validity;

    /** 反作弊命中项（可为空列表） */
    private List<String> flags;
}
