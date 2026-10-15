package com.sjt.typing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sjt.typing.common.JsonUtil;
import com.sjt.typing.dto.vo.PsaVO;
import com.sjt.typing.dto.vo.PublicStatsVO;
import com.sjt.typing.entity.Psa;
import com.sjt.typing.entity.Result;
import com.sjt.typing.entity.SiteConfig;
import com.sjt.typing.mapper.PsaMapper;
import com.sjt.typing.mapper.QuoteMapper;
import com.sjt.typing.mapper.ResultMapper;
import com.sjt.typing.mapper.SiteConfigMapper;
import com.sjt.typing.mapper.UserMapper;
import com.sjt.typing.service.ConfigService;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 站点公共数据实现。
 * <p>缓存策略：读时先查 Redis，未命中回源 MySQL 并回填；写入方（AdminService）在变更时主动删 Key，
 * 即 Cache-Aside，不做双写，避免两边不一致。</p>
 */
@Service
public class ConfigServiceImpl implements ConfigService {

    /** 缓存 Key 与方案 5 章 Redis 设计一致 */
    private static final String KEY_CONFIG = "config:global";
    private static final String KEY_PSA = "psa:active";

    private static final Duration CONFIG_TTL = Duration.ofMinutes(30);
    private static final Duration PSA_TTL = Duration.ofMinutes(5);
    private static final Duration STATS_TTL = Duration.ofSeconds(60);

    private final SiteConfigMapper siteConfigMapper;
    private final PsaMapper psaMapper;
    private final ResultMapper resultMapper;
    private final UserMapper userMapper;
    private final QuoteMapper quoteMapper;
    private final StringRedisTemplate redisTemplate;

    public ConfigServiceImpl(SiteConfigMapper siteConfigMapper,
                             PsaMapper psaMapper,
                             ResultMapper resultMapper,
                             UserMapper userMapper,
                             QuoteMapper quoteMapper,
                             StringRedisTemplate redisTemplate) {
        this.siteConfigMapper = siteConfigMapper;
        this.psaMapper = psaMapper;
        this.resultMapper = resultMapper;
        this.userMapper = userMapper;
        this.quoteMapper = quoteMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Map<String, Object> getConfigs() {
        String cached = redisTemplate.opsForValue().get(KEY_CONFIG);
        if (cached != null) {
            return JsonUtil.parse(cached, new TypeReference<LinkedHashMap<String, Object>>() {
            });
        }
        List<SiteConfig> rows = siteConfigMapper.selectList(null);
        Map<String, Object> configs = new LinkedHashMap<>();
        for (SiteConfig row : rows) {
            // value 列本身存的就是 JSON，读出来按 JSON 解析，让前端拿到结构化值而非字符串
            configs.put(row.getKey(), JsonUtil.parse(row.getValue(), Object.class));
        }
        redisTemplate.opsForValue().set(KEY_CONFIG, JsonUtil.toJson(configs), CONFIG_TTL);
        return configs;
    }

    @Override
    public List<PsaVO> getActivePsas() {
        String cached = redisTemplate.opsForValue().get(KEY_PSA);
        if (cached != null) {
            return JsonUtil.parse(cached, new TypeReference<List<PsaVO>>() {
            });
        }
        long now = System.currentTimeMillis();
        List<Psa> rows = psaMapper.selectList(new LambdaQueryWrapper<Psa>()
                // expires_at 为 null 表示永不过期
                .and(w -> w.isNull(Psa::getExpiresAt).or().gt(Psa::getExpiresAt, now))
                .orderByDesc(Psa::getSticky)
                .orderByDesc(Psa::getCreatedAt));
        List<PsaVO> result = new ArrayList<>(rows.size());
        for (Psa row : rows) {
            PsaVO vo = new PsaVO();
            vo.setId(row.getId());
            vo.setMessage(row.getMessage());
            vo.setSticky(row.getSticky());
            vo.setCreatedAt(row.getCreatedAt());
            vo.setExpiresAt(row.getExpiresAt());
            result.add(vo);
        }
        redisTemplate.opsForValue().set(KEY_PSA, JsonUtil.toJson(result), PSA_TTL);
        return result;
    }

    @Override
    public PublicStatsVO publicStats() {
        long now = System.currentTimeMillis();
        PublicStatsVO vo = new PublicStatsVO();
        // 用聚合查询而不是把全表捞回来：只取 SUM 与 COUNT，一条 SQL 出结果
        QueryWrapper<Result> aggregate = new QueryWrapper<>();
        aggregate.select("COUNT(*) AS totalTests", "IFNULL(SUM(test_duration), 0) AS totalDuration");
        List<Map<String, Object>> rows = resultMapper.selectMaps(aggregate);
        Map<String, Object> row = rows.isEmpty() ? Map.of() : rows.get(0);
        vo.setTotalTests(toLong(row.get("totalTests")));
        vo.setTotalDurationSeconds(Math.round(toDouble(row.get("totalDuration"))));
        vo.setTotalUsers(count(userMapper.selectCount(null)));
        vo.setTotalQuotes(count(quoteMapper.selectCount(null)));
        vo.setUpdatedAt(now);
        return vo;
    }

    private long count(Long value) {
        return value == null ? 0L : value;
    }

    private long toLong(Object value) {
        return value == null ? 0L : ((Number) value).longValue();
    }

    private double toDouble(Object value) {
        return value == null ? 0d : ((Number) value).doubleValue();
    }
}
