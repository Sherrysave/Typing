package com.sjt.typing.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 关注 / 取关请求（对标 api/routes/connections 的 body 结构）。
 */
@Data
public class ConnectionReq {

    @NotBlank(message = "targetUid 不能为空")
    @Size(max = 22, message = "targetUid 不合法")
    private String targetUid;

    /** true 关注 / false 取关 */
    @NotNull(message = "follow 不能为空")
    private Boolean follow;
}
