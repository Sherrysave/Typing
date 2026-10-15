package com.sjt.typing.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 举报请求：被举报对象可以是用户（targetUid）或成绩（resultId），至少给一个。
 */
@Data
public class ReportReq {

    @Size(max = 22, message = "targetUid 不合法")
    private String targetUid;

    private Long resultId;

    @NotBlank(message = "reason 不能为空")
    @Size(max = 64, message = "举报原因不能超过 64 字")
    private String reason;

    @Size(max = 512, message = "补充说明不能超过 512 字")
    private String description;
}
