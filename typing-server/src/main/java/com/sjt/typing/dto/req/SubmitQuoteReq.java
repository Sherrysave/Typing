package com.sjt.typing.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 提交名言请求（进入待审队列）。
 */
@Data
public class SubmitQuoteReq {

    @NotBlank(message = "language 不能为空")
    @Size(max = 16, message = "language 过长")
    private String language;

    @NotBlank(message = "text 不能为空")
    @Size(max = 2000, message = "名言正文不能超过 2000 字")
    private String text;

    @NotBlank(message = "source 不能为空")
    @Size(max = 128, message = "出处不能超过 128 字")
    private String source;
}
