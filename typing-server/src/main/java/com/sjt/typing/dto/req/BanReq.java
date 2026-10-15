package com.sjt.typing.dto.req;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 封禁请求体：被封禁的 uid 走路径变量，这里只承载原因。
 */
@Data
public class BanReq {

    @Size(max = 256, message = "封禁原因不能超过 256 字")
    private String reason;
}
