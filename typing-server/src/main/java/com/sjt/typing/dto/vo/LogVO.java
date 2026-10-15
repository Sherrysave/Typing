package com.sjt.typing.dto.vo;

import lombok.Data;

/**
 * 操作日志响应体。detail 已从存储的 JSON 字符串解析回对象，便于前端直接展示。
 */
@Data
public class LogVO {

    private Long id;
    private String uid;
    private String action;
    private String target;
    private Object detail;
    private String ip;
    private Long timestamp;
}
