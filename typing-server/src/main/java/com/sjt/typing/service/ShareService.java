package com.sjt.typing.service;

import com.sjt.typing.dto.vo.ShareLinkVO;
import com.sjt.typing.dto.vo.ShareResultVO;
import com.sjt.typing.security.LoginUser;

/**
 * 分享业务：成绩分享短链的生成与解析。
 * <p>设计要点：</p>
 * <ul>
 *   <li>短码存在 Redis（share:code / share:res 双向映射），<b>不设 TTL</b>，短链长期有效；</li>
 *   <li>只有有效性为 1 的成绩可被分享，避免把待审/拒绝的成绩扩散出去；</li>
 *   <li>生成是幂等的：同一条成绩重复调用返回同一个短码。</li>
 * </ul>
 */
public interface ShareService {

    /** 为指定成绩生成（或复用）分享短链，要求成绩属于当前登录用户且有效 */
    ShareLinkVO create(long resultId, LoginUser loginUser);

    /** 按短码解析分享数据（公开，无需登录） */
    ShareResultVO byCode(String code);

    /** 直接按成绩主键预览分享数据（本人或管理员） */
    ShareResultVO detail(long resultId, LoginUser loginUser);
}
