package com.sjt.typing.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sjt.typing.common.BusinessException;
import com.sjt.typing.common.IdUtil;
import com.sjt.typing.common.JsonUtil;
import com.sjt.typing.common.ResultCode;
import com.sjt.typing.dto.vo.ShareLinkVO;
import com.sjt.typing.dto.vo.ShareResultVO;
import com.sjt.typing.entity.Result;
import com.sjt.typing.entity.User;
import com.sjt.typing.security.LoginUser;
import com.sjt.typing.service.LeaderboardService;
import com.sjt.typing.service.ResultService;
import com.sjt.typing.service.ShareService;
import com.sjt.typing.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 分享业务实现：短码 ↔ 成绩主键的双向映射存于 Redis。
 */
@Service
public class ShareServiceImpl implements ShareService {

    /** 短码 → 成绩主键 */
    private static final String CODE_KEY_PREFIX = "share:code:";
    /** 成绩主键 → 短码（用于生成幂等） */
    private static final String RES_KEY_PREFIX = "share:res:";
    /** 短码长度 */
    private static final int CODE_LENGTH = 8;
    /** 短码冲突时的最大重试次数 */
    private static final int CODE_RETRY = 5;

    private final ResultService resultService;
    private final UserService userService;
    private final LeaderboardService leaderboardService;
    private final StringRedisTemplate redisTemplate;

    /** 分享链接前缀，形如 http://host/?r= ，尾部的短码由本服务拼接 */
    private final String baseUrl;

    public ShareServiceImpl(ResultService resultService,
                            UserService userService,
                            LeaderboardService leaderboardService,
                            StringRedisTemplate redisTemplate,
                            @Value("${typing.share.base-url:http://127.0.0.1:8090/?r=}") String baseUrl) {
        this.resultService = resultService;
        this.userService = userService;
        this.leaderboardService = leaderboardService;
        this.redisTemplate = redisTemplate;
        this.baseUrl = baseUrl;
    }

    @Override
    public ShareLinkVO create(long resultId, LoginUser loginUser) {
        Result result = resultService.getOwned(resultId, loginUser);
        if (result.getValidity() == null || result.getValidity() != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "只有有效成绩可以分享");
        }

        String resKey = RES_KEY_PREFIX + resultId;
        String code = redisTemplate.opsForValue().get(resKey);
        if (code == null) {
            // 生成短码并落 Redis；两端都幂等写入，避免并发重复生成
            code = generateCode(resultId);
            redisTemplate.opsForValue().setIfAbsent(resKey, code);
            String canonical = redisTemplate.opsForValue().get(resKey);
            code = canonical == null ? code : canonical;
        }

        ShareLinkVO vo = new ShareLinkVO();
        vo.setResultId(resultId);
        vo.setCode(code);
        vo.setUrl(baseUrl + code);
        return vo;
    }

    @Override
    public ShareResultVO byCode(String code) {
        String resultId = redisTemplate.opsForValue().get(CODE_KEY_PREFIX + code);
        if (resultId == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "分享链接不存在或已失效");
        }
        long id;
        try {
            id = Long.parseLong(resultId);
        } catch (NumberFormatException e) {
            throw new BusinessException(ResultCode.NOT_FOUND, "分享链接不存在或已失效");
        }
        return toVO(resultService.getValid(id));
    }

    @Override
    public ShareResultVO detail(long resultId, LoginUser loginUser) {
        return toVO(resultService.getOwned(resultId, loginUser));
    }

    /** 生成不冲突的短码：占用则重试 */
    private String generateCode(long resultId) {
        for (int i = 0; i < CODE_RETRY; i++) {
            String code = IdUtil.shortCode(CODE_LENGTH);
            Boolean ok = redisTemplate.opsForValue()
                    .setIfAbsent(CODE_KEY_PREFIX + code, String.valueOf(resultId));
            if (Boolean.TRUE.equals(ok)) {
                return code;
            }
        }
        throw new BusinessException(ResultCode.BIZ_ERROR, "短码生成失败，请稍后重试");
    }

    /** 实体 → 分享 VO：JSON 字符串列解析为结构化对象，并补上用户名与名次 */
    private ShareResultVO toVO(Result result) {
        ShareResultVO vo = new ShareResultVO();
        vo.setId(result.getId());
        vo.setUid(result.getUid());
        vo.setWpm(result.getWpm());
        vo.setRawWpm(result.getRawWpm());
        vo.setAcc(result.getAcc());
        vo.setConsistency(result.getConsistency());
        vo.setMode(result.getMode());
        vo.setMode2(result.getMode2());
        vo.setLanguage(result.getLanguage());
        vo.setPunctuation(result.getPunctuation());
        vo.setNumbers(result.getNumbers());
        vo.setTestDuration(result.getTestDuration());
        vo.setIsPb(result.getIsPb());
        vo.setCharStats(JsonUtil.parse(result.getCharStats(),
                new TypeReference<Map<String, Object>>() {
                }));
        vo.setKeySpacingStats(JsonUtil.parse(result.getKeySpacingStats(),
                new TypeReference<Map<String, Object>>() {
                }));
        vo.setKeyDurationStats(JsonUtil.parse(result.getKeyDurationStats(),
                new TypeReference<Map<String, Object>>() {
                }));
        vo.setChartData(JsonUtil.parse(result.getChartData(),
                new TypeReference<List<Map<String, Object>>>() {
                }));
        vo.setTags(JsonUtil.parse(result.getTags(),
                new TypeReference<List<String>>() {
                }));
        vo.setClientTimestamp(result.getClientTimestamp());

        User user = userService.getById(result.getUid());
        if (user != null) {
            vo.setName(user.getName());
        }
        if (Boolean.TRUE.equals(result.getIsPb())) {
            vo.setRank(leaderboardService.getRank(result.getUid(),
                    result.getMode(), result.getMode2(), result.getLanguage()));
        }
        return vo;
    }
}
