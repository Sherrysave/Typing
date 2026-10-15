package com.sjt.typing.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

/**
 * 保存用户预设请求。config 用 Map 承载任意 JSON，与 Monkeytype 的预设格式一致。
 */
@Data
public class PresetReq {

    @NotBlank(message = "name 不能为空")
    @Size(max = 64, message = "预设名称不能超过 64 字")
    private String name;

    @NotNull(message = "config 不能为空")
    private Map<String, Object> config;
}
