package com.sjt.typing.service;

import com.sjt.typing.dto.req.PresetReq;
import com.sjt.typing.dto.vo.PresetVO;

import java.util.List;

/**
 * 用户配置预设业务（对标 dal/preset.ts）。
 */
public interface PresetService {

    /** 列出某用户的全部预设（按更新时间倒序） */
    List<PresetVO> list(String uid);

    /** 新建预设 */
    PresetVO create(PresetReq req, String uid);

    /** 删除预设（校验归属，禁止删他人预设） */
    void delete(String id, String uid);
}
