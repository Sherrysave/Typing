package com.sjt.typing.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sjt.typing.aop.RateLimit;
import com.sjt.typing.common.R;
import com.sjt.typing.dto.req.SubmitResultReq;
import com.sjt.typing.dto.vo.ShareLinkVO;
import com.sjt.typing.dto.vo.ShareResultVO;
import com.sjt.typing.dto.vo.SubmitResultVO;
import com.sjt.typing.entity.Result;
import com.sjt.typing.security.LoginUser;
import com.sjt.typing.security.RequireLogin;
import com.sjt.typing.security.UserContext;
import com.sjt.typing.service.ResultService;
import com.sjt.typing.service.ShareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 成绩接口（对标 api/routes/results）。
 */
@Tag(name = "成绩", description = "提交成绩 / 我的成绩 / 分享短链")
@RestController
@RequestMapping("/api/results")
public class ResultController {

    private final ResultService resultService;
    private final ShareService shareService;

    public ResultController(ResultService resultService, ShareService shareService) {
        this.resultService = resultService;
        this.shareService = shareService;
    }

    @Operation(summary = "提交成绩（核心接口）")
    @RequireLogin
    @RateLimit(key = "results.submit", limit = 10, windowSeconds = 60)
    @PostMapping
    public R<SubmitResultVO> submit(@Valid @RequestBody SubmitResultReq req) {
        LoginUser loginUser = UserContext.get();
        return R.ok(resultService.submit(req, loginUser));
    }

    @Operation(summary = "分页查询我的成绩")
    @RequireLogin
    @GetMapping("/my")
    public R<Page<Result>> myResults(@RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "20") int size) {
        String uid = UserContext.getUidOrThrow();
        return R.ok(resultService.listMy(uid, page, size));
    }

    @Operation(summary = "生成成绩分享短链（幂等，仅本人有效成绩）")
    @RequireLogin
    @RateLimit(key = "results.share", limit = 20, windowSeconds = 60)
    @PostMapping("/{id}/share")
    public R<ShareLinkVO> share(@PathVariable long id) {
        return R.ok(shareService.create(id, UserContext.get()));
    }

    @Operation(summary = "查看成绩分享数据（本人或管理员）")
    @RequireLogin
    @GetMapping("/{id}/share")
    public R<ShareResultVO> shareDetail(@PathVariable long id) {
        return R.ok(shareService.detail(id, UserContext.get()));
    }
}
