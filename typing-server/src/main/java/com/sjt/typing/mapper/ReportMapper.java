package com.sjt.typing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sjt.typing.entity.Report;
import org.apache.ibatis.annotations.Mapper;

/**
 * 举报数据访问（对应 dal/report.ts）。
 */
@Mapper
public interface ReportMapper extends BaseMapper<Report> {
}
