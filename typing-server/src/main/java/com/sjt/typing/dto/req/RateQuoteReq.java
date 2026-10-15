package com.sjt.typing.dto.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 名言评分请求（1~5 分）。
 */
@Data
public class RateQuoteReq {

    @NotNull(message = "rating 不能为空")
    @Min(value = 1, message = "评分不能低于 1")
    @Max(value = 5, message = "评分不能高于 5")
    private Integer rating;
}
