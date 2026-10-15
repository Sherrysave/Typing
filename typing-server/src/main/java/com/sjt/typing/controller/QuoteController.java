package com.sjt.typing.controller;

import com.sjt.typing.aop.RateLimit;
import com.sjt.typing.common.R;
import com.sjt.typing.dto.req.RateQuoteReq;
import com.sjt.typing.dto.req.SubmitQuoteReq;
import com.sjt.typing.dto.vo.NewQuoteVO;
import com.sjt.typing.dto.vo.QuoteVO;
import com.sjt.typing.security.RequireLogin;
import com.sjt.typing.security.UserContext;
import com.sjt.typing.service.QuoteService;
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
 * 名言接口（对标 api/routes/quotes）。
 */
@Tag(name = "名言", description = "随机取题 / 提交待审 / 评分")
@RestController
@RequestMapping("/api/quotes")
public class QuoteController {

    private final QuoteService quoteService;

    public QuoteController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @Operation(summary = "随机取一条名言（名言模式取题）")
    @GetMapping("/random")
    public R<QuoteVO> random(@RequestParam(defaultValue = "english") String language,
                             @RequestParam(required = false) Integer minLength,
                             @RequestParam(required = false) Integer maxLength) {
        return R.ok(quoteService.random(language, minLength, maxLength));
    }

    @Operation(summary = "提交名言（进入待审队列）")
    @RequireLogin
    @RateLimit(key = "quotes.submit", limit = 10, windowSeconds = 60)
    @PostMapping
    public R<NewQuoteVO> submit(@Valid @RequestBody SubmitQuoteReq req) {
        return R.ok(quoteService.submit(req, UserContext.getUidOrThrow()));
    }

    @Operation(summary = "对名言评分（1~5，同一用户重复评分覆盖）")
    @RequireLogin
    @RateLimit(key = "quotes.rate", limit = 30, windowSeconds = 60)
    @PostMapping("/{id}/rating")
    public R<Void> rate(@PathVariable String id, @Valid @RequestBody RateQuoteReq req) {
        quoteService.rate(id, UserContext.getUidOrThrow(), req.getRating());
        return R.ok();
    }
}
