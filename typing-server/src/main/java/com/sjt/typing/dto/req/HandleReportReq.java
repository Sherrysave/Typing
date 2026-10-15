package com.sjt.typing.dto.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 处理举报请求：1 已处理 / 2 驳回。
 */
@Data
public class HandleReportReq {

    @NotNull(message = "status 不能为空")
    @Min(value = 1, message = "status 只能为 1 或 2")
    @Max(value = 2, message = "status 只能为 1 或 2")
    private Integer status;
}
