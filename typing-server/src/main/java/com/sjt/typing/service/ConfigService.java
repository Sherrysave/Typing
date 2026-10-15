package com.sjt.typing.service;

import com.sjt.typing.dto.vo.PsaVO;
import com.sjt.typing.dto.vo.PublicStatsVO;

import java.util.List;
import java.util.Map;

/**
 * 站点公共数据：全站配置、站内公告、公开统计（对标 dal/config.ts / dal/psa.ts / dal/public.ts）。
 * <p>三者都是「读多写极少」的数据，统一走 Redis 缓存 + 回源重建（Cache-Aside）。</p>
 */
public interface ConfigService {

    /** 全站配置：key → 任意 JSON 值 */
    Map<String, Object> getConfigs();

    /** 当前有效公告（未过期），置顶优先 */
    List<PsaVO> getActivePsas();

    /** 公开统计：总测试数、总时长、用户数、名言数 */
    PublicStatsVO publicStats();
}
