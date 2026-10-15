package com.sjt.typing.dto.vo;

import lombok.Data;

/**
 * 用户预设响应体。config 以 Map 承载任意 JSON。
 */
@Data
public class PresetVO {

    private String id;
    private String name;
    private Object config;
    private Long createdAt;
    private Long updatedAt;
}
