package com.sjt.typing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sjt.typing.common.BusinessException;
import com.sjt.typing.common.IdUtil;
import com.sjt.typing.common.ResultCode;
import com.sjt.typing.dto.req.ReportReq;
import com.sjt.typing.dto.vo.ReportVO;
import com.sjt.typing.entity.Report;
import com.sjt.typing.mapper.ReportMapper;
import com.sjt.typing.service.ReportService;
import org.springframework.stereotype.Service;

/**
 * 举报业务实现。
 */
@Service
public class ReportServiceImpl implements ReportService {

    /** 0 待处理 1 已处理 2 驳回 */
    private static final int STATUS_PENDING = 0;

    private final ReportMapper reportMapper;

    public ReportServiceImpl(ReportMapper reportMapper) {
        this.reportMapper = reportMapper;
    }

    @Override
    public void report(ReportReq req, String uid) {
        // 举报必须指明对象：要么举报用户，要么举报成绩
        if ((req.getTargetUid() == null || req.getTargetUid().isBlank()) && req.getResultId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "targetUid 与 resultId 至少填写一个");
        }
        Report entity = new Report();
        entity.setId(IdUtil.randomId());
        entity.setUid(uid);
        entity.setTargetUid(req.getTargetUid());
        entity.setResultId(req.getResultId());
        entity.setReason(req.getReason());
        entity.setDescription(req.getDescription());
        entity.setStatus(STATUS_PENDING);
        entity.setTimestamp(System.currentTimeMillis());
        reportMapper.insert(entity);
    }

    @Override
    public Page<ReportVO> list(Integer status, int page, int size) {
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<Report>()
                .eq(status != null, Report::getStatus, status)
                .orderByAsc(Report::getTimestamp);
        Page<Report> result = reportMapper.selectPage(new Page<>(page, size), wrapper);
        Page<ReportVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    @Override
    public void handle(String id, int status) {
        Report existing = reportMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "举报不存在");
        }
        Report update = new Report();
        update.setId(id);
        update.setStatus(status);
        reportMapper.updateById(update);
    }

    private ReportVO toVO(Report entity) {
        ReportVO vo = new ReportVO();
        vo.setId(entity.getId());
        vo.setUid(entity.getUid());
        vo.setTargetUid(entity.getTargetUid());
        vo.setResultId(entity.getResultId());
        vo.setReason(entity.getReason());
        vo.setDescription(entity.getDescription());
        vo.setStatus(entity.getStatus());
        vo.setTimestamp(entity.getTimestamp());
        return vo;
    }
}
