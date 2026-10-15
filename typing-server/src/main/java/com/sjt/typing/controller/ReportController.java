package com.sjt.typing.controller;

import com.sjt.typing.aop.RateLimit;
import com.sjt.typing.common.R;
import com.sjt.typing.dto.req.ReportReq;
import com.sjt.typing.security.RequireLogin;
import com.sjt.typing.security.UserContext;
import com.sjt.typing.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 举报接口（对标 api/routes/report）。
 */
@Tag(name = "举报", description = "举报成绩或用户")
@RestController
@RequestMapping("/api/report")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @Operation(summary = "提交举报")
    @RequireLogin
    @RateLimit(key = "report.submit", limit = 5, windowSeconds = 60)
    @PostMapping
    public R<Void> report(@Valid @RequestBody ReportReq req) {
        reportService.report(req, UserContext.getUidOrThrow());
        return R.ok();
    }
}
