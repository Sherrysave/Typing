package com.sjt.typing.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sjt.typing.dto.req.SubmitQuoteReq;
import com.sjt.typing.dto.vo.NewQuoteVO;
import com.sjt.typing.dto.vo.QuoteVO;

/**
 * 名言业务：随机取题、提交待审、评分，以及后台审核流（对标 dal/quote.ts 与 dal/new-quotes.ts）。
 */
public interface QuoteService {

    /** 随机取一条已通过审核的名言（可按语言与长度区间筛选） */
    QuoteVO random(String language, Integer minLength, Integer maxLength);

    /** 用户提交名言，进入待审队列 */
    NewQuoteVO submit(SubmitQuoteReq req, String uid);

    /** 对名言评分（1~5），同一用户重复评分覆盖旧值 */
    void rate(String quoteId, String uid, int rating);

    /** 后台：分页查询待审名言（status 为空则查全部） */
    Page<NewQuoteVO> listPending(Integer status, int page, int size);

    /** 后台：通过审核——写入 t_quote 并将待审记录置为已通过 */
    QuoteVO approve(String newQuoteId, String adminUid);

    /** 后台：驳回待审名言 */
    void reject(String newQuoteId);
}
