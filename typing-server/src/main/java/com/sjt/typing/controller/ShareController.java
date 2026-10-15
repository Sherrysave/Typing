package com.sjt.typing.controller;

import com.sjt.typing.common.R;
import com.sjt.typing.dto.vo.ShareResultVO;
import com.sjt.typing.service.ShareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 分享短链公开入口：前端以 {@code ?r={code}} 打开分享页时，从这里拉取渲染数据。
 */
@Tag(name = "分享", description = "分享短链解析")
@RestController
@RequestMapping("/api/share")
public class ShareController {

    private final ShareService shareService;

    public ShareController(ShareService shareService) {
        this.shareService = shareService;
    }

    @Operation(summary = "按短码获取分享成绩数据（公开）")
    @GetMapping("/{code}")
    public R<ShareResultVO> byCode(@PathVariable String code) {
        return R.ok(shareService.byCode(code));
    }
}
