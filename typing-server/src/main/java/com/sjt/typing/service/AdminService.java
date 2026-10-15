package com.sjt.typing.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sjt.typing.dto.req.BanReq;
import com.sjt.typing.dto.req.PsaReq;
import com.sjt.typing.dto.vo.AdminUidVO;
import com.sjt.typing.dto.vo.BlocklistVO;
import com.sjt.typing.dto.vo.LogVO;
import com.sjt.typing.dto.vo.PsaVO;

import java.util.List;

/**
 * 后台管理业务（对标 dal/blocklist.ts + dal/admin-uids.ts + dal/psa.ts + dal/logs.ts）。
 * <p>所有方法都只由 /api/admin/** 暴露，访问前已由 AdminAuthInterceptor 校验管理员身份。</p>
 */
public interface AdminService {

    /** 封禁用户：写 t_blocklist 并置 t_user.banned=1 */
    void ban(String uid, String reason, String adminUid);

    /** 解封用户 */
    void unban(String uid);

    /** 分页查询封禁名单 */
    Page<BlocklistVO> blocklist(int page, int size);

    /** 发布公告，并主动删除公告缓存（Cache-Aside 失效） */
    PsaVO createPsa(PsaReq req);

    /** 分页查询操作日志 */
    Page<LogVO> logs(int page, int size);

    /** 查询管理员名单 */
    List<AdminUidVO> admins();

    /** 新增管理员 */
    void addAdmin(String uid, String addedBy);

    /** 移除管理员（不允许移除自己，避免误操作把自己锁在外面） */
    void removeAdmin(String uid, String operator);
}
