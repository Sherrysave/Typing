package com.sjt.typing.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sjt.typing.dto.req.SubmitResultReq;
import com.sjt.typing.dto.vo.SubmitResultVO;
import com.sjt.typing.entity.Result;
import com.sjt.typing.security.LoginUser;

/**
 * 成绩业务：提交（含去重与反作弊）、个人成绩分页。
 */
public interface ResultService {

    /**
     * 提交成绩：Redis 去重 → 反作弊校验 → 落库 → 更新排行榜 → 判定 isPb。
     */
    SubmitResultVO submit(SubmitResultReq req, LoginUser loginUser);

    /** 分页查询某用户自己的成绩（按服务端时间倒序） */
    Page<Result> listMy(String uid, int page, int size);

    /** 分页查询指定用户的成绩（用于 /users/{uid}/results，调用方负责鉴权） */
    Page<Result> listByUid(String uid, int page, int size);

    /** 统计某用户的成绩条数 */
    long countByUid(String uid);

    /**
     * 按主键取成绩，要求存在且属于当前登录用户（管理员可越权查看）。
     * 不存在抛 404，非本人且非管理员抛 403。
     */
    Result getOwned(long id, LoginUser loginUser);

    /** 按主键取成绩，要求存在且有效性为 1；用于分享短链的公开数据读取，否则抛 404 */
    Result getValid(long id);
}
