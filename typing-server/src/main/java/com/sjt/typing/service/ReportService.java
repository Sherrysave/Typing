package com.sjt.typing.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sjt.typing.dto.req.ReportReq;
import com.sjt.typing.dto.vo.ReportVO;

/**
 * 举报业务（对标 dal/report.ts）。
 */
public interface ReportService {

    /** 提交举报 */
    void report(ReportReq req, String uid);

    /** 后台：分页查询举报（status 为空则查全部） */
    Page<ReportVO> list(Integer status, int page, int size);

    /** 后台：处理举报（1 已处理 / 2 驳回） */
    void handle(String id, int status);
}
