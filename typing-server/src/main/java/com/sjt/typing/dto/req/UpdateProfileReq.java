package com.sjt.typing.dto.req;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改个人资料请求。字段为空表示不修改（PATCH 语义）。
 */
@Data
public class UpdateProfileReq {

    @Size(max = 256, message = "个人简介不能超过 256 字")
    private String bio;

    @Size(max = 64, message = "键盘名称不能超过 64 字")
    private String keyboard;

    /** 是否退出排行榜 */
    private Boolean lbOptOut;
}
