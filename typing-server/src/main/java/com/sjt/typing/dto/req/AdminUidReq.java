package com.sjt.typing.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 增删管理员请求（仅现有管理员可调用）。
 */
@Data
public class AdminUidReq {

    @NotBlank(message = "uid 不能为空")
    @Size(max = 22, message = "uid 不合法")
    private String uid;
}
