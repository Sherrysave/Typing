package com.sjt.typing.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新令牌请求。
 */
@Data
public class RefreshReq {

    @NotBlank(message = "refreshToken 不能为空")
    private String refreshToken;
}
