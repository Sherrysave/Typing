package com.sjt.typing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sjt.typing.common.BusinessException;
import com.sjt.typing.common.JsonUtil;
import com.sjt.typing.common.ResultCode;
import com.sjt.typing.dto.req.SubmitResultReq;
import com.sjt.typing.dto.vo.SubmitResultVO;
import com.sjt.typing.entity.Result;
import com.sjt.typing.mapper.ResultMapper;
import com.sjt.typing.security.LoginUser;
import com.sjt.typing.service.LeaderboardService;
import com.sjt.typing.service.ResultService;
import com.sjt.typing.validation.ResultValidator;
import com.sjt.typing.validation.ResultValidator.ValidationOutcome;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * 成绩业务实现。
 */
@Service
public class ResultServiceImpl implements ResultService {

    /** 去重 Key 的 TTL（秒） */
    private static final long DEDUP_TTL_SECONDS = 300;

    private final ResultMapper resultMapper;
    private final ResultValidator resultValidator;
    private final LeaderboardService leaderboardService;
    private final StringRedisTemplate redisTemplate;

    public ResultServiceImpl(ResultMapper resultMapper,
                             ResultValidator resultValidator,
                             LeaderboardService leaderboardService,
                             StringRedisTemplate redisTemplate) {
        this.resultMapper = resultMapper;
        this.resultValidator = resultValidator;
        this.leaderboardService = leaderboardService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubmitResultVO submit(SubmitResultReq req, LoginUser loginUser) {
        String uid = loginUser.getUid();
        long clientTimestamp = req.getClientTimestamp() == null
                ? System.currentTimeMillis()
                : req.getClientTimestamp();

        // 1. Redis 去重：SETNX dedup:{uid}:{clientTimestamp} EX 300，防重复提交/重放
        String dedupKey = "dedup:" + uid + ":" + clientTimestamp;
        Boolean firstSubmit = redisTemplate.opsForValue()
                .setIfAbsent(dedupKey, "1", Duration.ofSeconds(DEDUP_TTL_SECONDS));
        if (!Boolean.TRUE.equals(firstSubmit)) {
            throw new BusinessException(ResultCode.DUP_SUBMIT);
        }

        // 2. 反作弊校验（交叉一致性校验）
        ValidationOutcome outcome = resultValidator.validate(req);
        boolean valid = outcome.getValidity() == 1;

        // 3. 查历史最高 WPM（仅有效成绩），用于 isPb 判定；须在插入本成绩之前查询
        BigDecimal historyMax = resultMapper.selectMaxWpm(
                uid, req.getMode(), req.getMode2(), req.getLanguage());
        boolean isPb = valid && (historyMax == null
                || req.getWpm().compareTo(historyMax) > 0);

        // 4. 组装实体并落库（无论是否有效都留证）
        long now = System.currentTimeMillis();
        Result entity = new Result();
        entity.setUid(uid);
        entity.setWpm(req.getWpm());
        entity.setRawWpm(req.getRawWpm());
        entity.setAcc(req.getAcc());
        entity.setConsistency(req.getConsistency());
        entity.setMode(req.getMode());
        entity.setMode2(req.getMode2());
        entity.setLanguage(req.getLanguage());
        entity.setPunctuation(Boolean.TRUE.equals(req.getPunctuation()));
        entity.setNumbers(Boolean.TRUE.equals(req.getNumbers()));
        entity.setLazyMode(false);
        entity.setTestDuration(req.getTestDuration());
        entity.setRestartCount(req.getRestartCount() == null ? 0 : req.getRestartCount());
        entity.setIncompleteTests(req.getIncompleteTests() == null ? 0 : req.getIncompleteTests());
        entity.setAfkDuration(req.getAfkDuration() == null ? 0 : req.getAfkDuration());
        entity.setBailedOut(false);
        entity.setCharStats(JsonUtil.toJson(req.getCharStats()));
        entity.setKeySpacingStats(JsonUtil.toJson(req.getKeySpacingStats()));
        entity.setKeyDurationStats(JsonUtil.toJson(req.getKeyDurationStats()));
        entity.setChartData(JsonUtil.toJson(req.getChartData()));
        entity.setFlags(JsonUtil.toJson(outcome.getFlags()));
        entity.setTags(JsonUtil.toJson(req.getTags()));
        entity.setIsPb(isPb);
        entity.setValidity(outcome.getValidity());
        entity.setClientTimestamp(clientTimestamp);
        entity.setServerTimestamp(now);
        resultMapper.insert(entity);

        // 5. 只有有效成绩才更新排行榜与名次
        Integer rank = null;
        if (valid) {
            leaderboardService.update(uid, entity);
            rank = leaderboardService.getRank(
                    uid, req.getMode(), req.getMode2(), req.getLanguage());
        }

        // 6. 组装响应
        SubmitResultVO vo = new SubmitResultVO();
        vo.setId(entity.getId());
        vo.setIsPb(isPb);
        vo.setRank(rank);
        vo.setValidity(outcome.getValidity());
        vo.setFlags(outcome.getFlags());
        return vo;
    }

    @Override
    public Page<Result> listMy(String uid, int page, int size) {
        return listByUid(uid, page, size);
    }

    @Override
    public Page<Result> listByUid(String uid, int page, int size) {
        Page<Result> pageParam = new Page<>(page, size);
        return resultMapper.selectPage(pageParam, new LambdaQueryWrapper<Result>()
                .eq(Result::getUid, uid)
                .orderByDesc(Result::getServerTimestamp));
    }

    @Override
    public long countByUid(String uid) {
        Long count = resultMapper.selectCount(
                new LambdaQueryWrapper<Result>().eq(Result::getUid, uid));
        return count == null ? 0L : count;
    }

    @Override
    public Result getOwned(long id, LoginUser loginUser) {
        Result result = resultMapper.selectById(id);
        if (result == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "成绩不存在");
        }
        if (!result.getUid().equals(loginUser.getUid()) && !loginUser.isAdmin()) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权查看该成绩");
        }
        return result;
    }

    @Override
    public Result getValid(long id) {
        Result result = resultMapper.selectById(id);
        if (result == null || result.getValidity() == null || result.getValidity() != 1) {
            throw new BusinessException(ResultCode.NOT_FOUND, "分享的成绩不存在或已失效");
        }
        return result;
    }
}
