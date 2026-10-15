package com.sjt.typing.dto.vo;

import lombok.Data;

/**
 * 举报响应体。
 */
@Data
public class ReportVO {

    private String id;
    private String uid;
    private String targetUid;
    private Long resultId;
    private String reason;
    private String description;

    /** 0 待处理 / 1 已处理 / 2 驳回 */
    private Integer status;

    private Long timestamp;
}
