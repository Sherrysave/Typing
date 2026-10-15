package com.sjt.typing.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发布站内公告请求。
 */
@Data
public class PsaReq {

    @NotBlank(message = "message 不能为空")
    @Size(max = 2000, message = "公告内容不能超过 2000 字")
    private String message;

    /** 是否置顶 */
    private Boolean sticky;

    /** 过期时间 epoch 毫秒，null 表示不过期 */
    private Long expiresAt;
}
