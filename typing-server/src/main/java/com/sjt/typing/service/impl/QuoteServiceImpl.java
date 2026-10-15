package com.sjt.typing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sjt.typing.common.BusinessException;
import com.sjt.typing.common.IdUtil;
import com.sjt.typing.common.ResultCode;
import com.sjt.typing.dto.req.SubmitQuoteReq;
import com.sjt.typing.dto.vo.NewQuoteVO;
import com.sjt.typing.dto.vo.QuoteVO;
import com.sjt.typing.entity.NewQuote;
import com.sjt.typing.entity.Quote;
import com.sjt.typing.entity.QuoteRating;
import com.sjt.typing.mapper.NewQuoteMapper;
import com.sjt.typing.mapper.QuoteMapper;
import com.sjt.typing.mapper.QuoteRatingMapper;
import com.sjt.typing.service.QuoteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 名言业务实现，对应方案 10.1 的名言审核流：
 * <pre>
 * 用户提交 → t_new_quote(status=0)
 *   管理员审核 ├─ 通过 → 写入 t_quote(approved_by=管理员uid)，置 status=1
 *              └─ 驳回 → status=2
 * </pre>
 */
@Service
public class QuoteServiceImpl implements QuoteService {

    /** 待审状态：0 待审 1 通过 2 驳回 */
    private static final int STATUS_PENDING = 0;
    private static final int STATUS_APPROVED = 1;
    private static final int STATUS_REJECTED = 2;

    /** 默认随机取题长度区间（对标 Monkeytype quote 模式的常用范围） */
    private static final int DEFAULT_MIN_LENGTH = 0;
    private static final int DEFAULT_MAX_LENGTH = 1000;

    private final QuoteMapper quoteMapper;
    private final NewQuoteMapper newQuoteMapper;
    private final QuoteRatingMapper quoteRatingMapper;

    public QuoteServiceImpl(QuoteMapper quoteMapper,
                            NewQuoteMapper newQuoteMapper,
                            QuoteRatingMapper quoteRatingMapper) {
        this.quoteMapper = quoteMapper;
        this.newQuoteMapper = newQuoteMapper;
        this.quoteRatingMapper = quoteRatingMapper;
    }

    @Override
    public QuoteVO random(String language, Integer minLength, Integer maxLength) {
        LambdaQueryWrapper<Quote> wrapper = new LambdaQueryWrapper<Quote>()
                .eq(Quote::getLanguage, language == null || language.isBlank() ? "english" : language)
                .ge(Quote::getLength, minLength == null ? DEFAULT_MIN_LENGTH : minLength)
                .le(Quote::getLength, maxLength == null ? DEFAULT_MAX_LENGTH : maxLength)
                // 名言库规模有限，直接用 MySQL 的 RAND() 取一条；数据量变大后再换成「随机 offset」策略
                .last("ORDER BY RAND() LIMIT 1");
        Quote quote = quoteMapper.selectOne(wrapper);
        if (quote == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "名言库中没有符合条件的名言");
        }
        return toVO(quote);
    }

    @Override
    public NewQuoteVO submit(SubmitQuoteReq req, String uid) {
        NewQuote entity = new NewQuote();
        entity.setId(IdUtil.randomId());
        entity.setUid(uid);
        entity.setLanguage(req.getLanguage());
        entity.setText(req.getText());
        entity.setSource(req.getSource());
        entity.setStatus(STATUS_PENDING);
        entity.setTimestamp(System.currentTimeMillis());
        newQuoteMapper.insert(entity);
        return toVO(entity);
    }

    /**
     * 评分：同一用户对同一条名言重复评分时覆盖旧值。
     * <p>t_quote_rating 是 (quote_id, uid) 复合主键，MyBatis-Plus 没有单列主键可供 updateById，
     * 因此走「查存量 → 有则按双条件 update，无则 insert」。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rate(String quoteId, String uid, int rating) {
        Quote quote = quoteMapper.selectById(quoteId);
        if (quote == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "名言不存在");
        }
        LambdaQueryWrapper<QuoteRating> keyWrapper = new LambdaQueryWrapper<QuoteRating>()
                .eq(QuoteRating::getQuoteId, quoteId)
                .eq(QuoteRating::getUid, uid);
        QuoteRating existing = quoteRatingMapper.selectOne(keyWrapper);

        long now = System.currentTimeMillis();
        if (existing == null) {
            QuoteRating entity = new QuoteRating();
            entity.setQuoteId(quoteId);
            entity.setUid(uid);
            entity.setRating(rating);
            entity.setTimestamp(now);
            quoteRatingMapper.insert(entity);
        } else {
            QuoteRating update = new QuoteRating();
            update.setRating(rating);
            update.setTimestamp(now);
            quoteRatingMapper.update(update, keyWrapper);
        }
    }

    @Override
    public Page<NewQuoteVO> listPending(Integer status, int page, int size) {
        LambdaQueryWrapper<NewQuote> wrapper = new LambdaQueryWrapper<NewQuote>()
                .eq(status != null, NewQuote::getStatus, status)
                .orderByAsc(NewQuote::getTimestamp);
        Page<NewQuote> result = newQuoteMapper.selectPage(new Page<>(page, size), wrapper);
        // 手动搬一页，避免把实体直接当响应体暴露出去
        Page<NewQuoteVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuoteVO approve(String newQuoteId, String adminUid) {
        NewQuote pending = newQuoteMapper.selectById(newQuoteId);
        if (pending == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "待审名言不存在");
        }
        if (pending.getStatus() != null && pending.getStatus() != STATUS_PENDING) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "该名言已审核过");
        }

        Quote quote = new Quote();
        quote.setId(pending.getId());
        quote.setLanguage(pending.getLanguage());
        quote.setText(pending.getText());
        quote.setSource(pending.getSource());
        quote.setLength(pending.getText() == null ? 0 : pending.getText().length());
        quote.setApprovedBy(adminUid);
        quote.setTimestamp(System.currentTimeMillis());
        quoteMapper.insert(quote);

        NewQuote update = new NewQuote();
        update.setId(pending.getId());
        update.setStatus(STATUS_APPROVED);
        newQuoteMapper.updateById(update);

        return toVO(quote);
    }

    @Override
    public void reject(String newQuoteId) {
        NewQuote pending = newQuoteMapper.selectById(newQuoteId);
        if (pending == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "待审名言不存在");
        }
        // 与 approve 保持同一套前置校验：已审核过的记录不允许再改判，
        // 否则「已通过」的记录会被改成「已驳回」，审核轨迹与实际入库数据自相矛盾
        if (pending.getStatus() != null && pending.getStatus() != STATUS_PENDING) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "该名言已审核过");
        }
        NewQuote update = new NewQuote();
        update.setId(newQuoteId);
        update.setStatus(STATUS_REJECTED);
        newQuoteMapper.updateById(update);
    }

    // =========================================================================
    // 转换与聚合
    // =========================================================================

    /** 名言转 VO，并聚合评分（无评分时平均值为 null） */
    private QuoteVO toVO(Quote quote) {
        QuoteVO vo = new QuoteVO();
        vo.setId(quote.getId());
        vo.setLanguage(quote.getLanguage());
        vo.setText(quote.getText());
        vo.setSource(quote.getSource());
        vo.setLength(quote.getLength());

        List<QuoteRating> ratings = quoteRatingMapper.selectList(
                new LambdaQueryWrapper<QuoteRating>().eq(QuoteRating::getQuoteId, quote.getId()));
        vo.setRatingCount(ratings.size());
        vo.setAverageRating(ratings.isEmpty() ? null
                : ratings.stream().mapToInt(QuoteRating::getRating).average().orElse(0d));
        return vo;
    }

    private NewQuoteVO toVO(NewQuote entity) {
        NewQuoteVO vo = new NewQuoteVO();
        vo.setId(entity.getId());
        vo.setUid(entity.getUid());
        vo.setLanguage(entity.getLanguage());
        vo.setText(entity.getText());
        vo.setSource(entity.getSource());
        vo.setStatus(entity.getStatus());
        vo.setTimestamp(entity.getTimestamp());
        return vo;
    }
}
