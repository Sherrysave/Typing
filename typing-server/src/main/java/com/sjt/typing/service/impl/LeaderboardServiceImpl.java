package com.sjt.typing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sjt.typing.dto.vo.LeaderboardVO;
import com.sjt.typing.entity.LeaderboardEntry;
import com.sjt.typing.entity.Result;
import com.sjt.typing.entity.User;
import com.sjt.typing.mapper.LeaderboardEntryMapper;
import com.sjt.typing.mapper.UserMapper;
import com.sjt.typing.service.LeaderboardService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 排行榜业务实现。
 *
 * <p>写路径：Redis ZSET 用「先 ZSCORE 比大小、只有更高才 ZADD」避免无谓更新；
 * 读路径完全走 Redis，未命中再回源 MySQL 冷备表并回填。</p>
 */
@Service
public class LeaderboardServiceImpl implements LeaderboardService {

    private static final String KEY_PREFIX = "lb:";

    private final StringRedisTemplate redisTemplate;
    private final LeaderboardEntryMapper leaderboardEntryMapper;
    private final UserMapper userMapper;

    public LeaderboardServiceImpl(StringRedisTemplate redisTemplate,
                                  LeaderboardEntryMapper leaderboardEntryMapper,
                                  UserMapper userMapper) {
        this.redisTemplate = redisTemplate;
        this.leaderboardEntryMapper = leaderboardEntryMapper;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(String uid, Result result) {
        String key = buildKey(result.getMode(), result.getMode2(), result.getLanguage());
        double newWpm = result.getWpm().doubleValue();

        // 1. 先比后写：只有更高才写 Redis
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        Double current = zSetOps.score(key, uid);
        if (current == null || newWpm > current) {
            zSetOps.add(key, uid, newWpm);
        }

        // 2. upsert 冷备表（每人每维度只保留最好一条）
        LeaderboardEntry existing = leaderboardEntryMapper.selectOne(
                new LambdaQueryWrapper<LeaderboardEntry>()
                        .eq(LeaderboardEntry::getMode, result.getMode())
                        .eq(LeaderboardEntry::getMode2, result.getMode2())
                        .eq(LeaderboardEntry::getLanguage, result.getLanguage())
                        .eq(LeaderboardEntry::getUid, uid)
                        .last("LIMIT 1"));

        if (existing == null) {
            LeaderboardEntry entry = new LeaderboardEntry();
            entry.setMode(result.getMode());
            entry.setMode2(result.getMode2());
            entry.setLanguage(result.getLanguage());
            entry.setUid(uid);
            entry.setResultId(result.getId());
            entry.setWpm(result.getWpm());
            entry.setRawWpm(result.getRawWpm());
            entry.setAcc(result.getAcc());
            entry.setConsistency(result.getConsistency());
            entry.setTimestamp(result.getClientTimestamp());
            leaderboardEntryMapper.insert(entry);
        } else if (existing.getWpm() == null || newWpm > existing.getWpm().doubleValue()) {
            existing.setResultId(result.getId());
            existing.setWpm(result.getWpm());
            existing.setRawWpm(result.getRawWpm());
            existing.setAcc(result.getAcc());
            existing.setConsistency(result.getConsistency());
            existing.setTimestamp(result.getClientTimestamp());
            leaderboardEntryMapper.updateById(existing);
        }
    }

    @Override
    public List<LeaderboardVO> getTop(String mode, String mode2, String language, int limit) {
        String key = buildKey(mode, mode2, language);
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

        // 先读 Redis
        Set<ZSetOperations.TypedTuple<String>> tuples =
                zSetOps.reverseRangeWithScores(key, 0, limit - 1L);

        if (tuples != null && !tuples.isEmpty()) {
            return buildFromRedis(tuples, mode, mode2, language);
        }

        // 回源冷备表并回填 Redis
        List<LeaderboardEntry> entries =
                leaderboardEntryMapper.selectTopByScope(mode, mode2, language, limit);
        if (entries == null || entries.isEmpty()) {
            return new ArrayList<>();
        }
        for (LeaderboardEntry entry : entries) {
            if (entry.getWpm() != null) {
                zSetOps.add(key, entry.getUid(), entry.getWpm().doubleValue());
            }
        }
        return buildFromEntries(entries);
    }

    @Override
    public Integer getRank(String uid, String mode, String mode2, String language) {
        String key = buildKey(mode, mode2, language);
        Long rank = redisTemplate.opsForZSet().reverseRank(key, uid);
        return rank == null ? null : (int) (rank + 1);
    }

    /**
     * 个人主页取各维度最好成绩：直接读冷备表即可（该表每人每维度只留最好一条，天然满足需求），
     * 不必回源 Redis——Redis 里没有 acc/consistency 等明细。
     */
    @Override
    public List<LeaderboardVO> bestOf(String uid) {
        List<LeaderboardEntry> entries = leaderboardEntryMapper.selectList(
                new LambdaQueryWrapper<LeaderboardEntry>()
                        .eq(LeaderboardEntry::getUid, uid)
                        .orderByDesc(LeaderboardEntry::getWpm));
        return buildFromEntries(entries);
    }

    // =========================================================================
    // 工具方法
    // =========================================================================

    private String buildKey(String mode, String mode2, String language) {
        return KEY_PREFIX + mode + ":" + mode2 + ":" + language;
    }

    /** 从 Redis 结果组装 VO：名称查用户表，明细查冷备表 */
    private List<LeaderboardVO> buildFromRedis(Set<ZSetOperations.TypedTuple<String>> tuples,
                                               String mode, String mode2, String language) {
        List<String> uids = tuples.stream()
                .map(ZSetOperations.TypedTuple::getValue)
                .filter(v -> v != null)
                .collect(Collectors.toList());

        Map<String, String> nameMap = loadNames(uids);
        Map<String, LeaderboardEntry> entryMap = loadEntries(mode, mode2, language, uids);

        List<LeaderboardVO> result = new ArrayList<>(tuples.size());
        int rank = 1;
        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            String uid = tuple.getValue();
            if (uid == null) {
                continue;
            }
            LeaderboardVO vo = new LeaderboardVO();
            vo.setRank(rank++);
            vo.setUid(uid);
            vo.setName(nameMap.get(uid));
            vo.setWpm(toDecimal(tuple.getScore()));
            LeaderboardEntry entry = entryMap.get(uid);
            if (entry != null) {
                vo.setRawWpm(entry.getRawWpm());
                vo.setAcc(entry.getAcc());
                vo.setConsistency(entry.getConsistency());
                vo.setTimestamp(entry.getTimestamp());
            }
            result.add(vo);
        }
        return result;
    }

    /** 从冷备表条目组装 VO */
    private List<LeaderboardVO> buildFromEntries(List<LeaderboardEntry> entries) {
        List<String> uids = entries.stream()
                .map(LeaderboardEntry::getUid)
                .collect(Collectors.toList());
        Map<String, String> nameMap = loadNames(uids);

        List<LeaderboardVO> result = new ArrayList<>(entries.size());
        int rank = 1;
        for (LeaderboardEntry entry : entries) {
            LeaderboardVO vo = new LeaderboardVO();
            vo.setRank(rank++);
            vo.setUid(entry.getUid());
            vo.setName(nameMap.get(entry.getUid()));
            vo.setWpm(entry.getWpm());
            vo.setRawWpm(entry.getRawWpm());
            vo.setAcc(entry.getAcc());
            vo.setConsistency(entry.getConsistency());
            vo.setTimestamp(entry.getTimestamp());
            result.add(vo);
        }
        return result;
    }

    /** 批量查用户名，返回 uid → name */
    private Map<String, String> loadNames(List<String> uids) {
        if (uids == null || uids.isEmpty()) {
            return Collections.emptyMap();
        }
        List<User> users = userMapper.selectList(
                new LambdaQueryWrapper<User>().in(User::getUid, uids));
        Map<String, String> map = new HashMap<>(users.size());
        for (User user : users) {
            map.put(user.getUid(), user.getName());
        }
        return map;
    }

    /** 批量查冷备明细，返回 uid → entry */
    private Map<String, LeaderboardEntry> loadEntries(String mode, String mode2,
                                                      String language, List<String> uids) {
        if (uids == null || uids.isEmpty()) {
            return Collections.emptyMap();
        }
        List<LeaderboardEntry> entries = leaderboardEntryMapper.selectList(
                new LambdaQueryWrapper<LeaderboardEntry>()
                        .eq(LeaderboardEntry::getMode, mode)
                        .eq(LeaderboardEntry::getMode2, mode2)
                        .eq(LeaderboardEntry::getLanguage, language)
                        .in(LeaderboardEntry::getUid, uids));
        Map<String, LeaderboardEntry> map = new HashMap<>(entries.size());
        for (LeaderboardEntry entry : entries) {
            map.put(entry.getUid(), entry);
        }
        return map;
    }

    private BigDecimal toDecimal(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }
}
