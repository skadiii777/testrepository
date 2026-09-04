package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.Report;

/**
 * 业务汇报 数据层
 *
 * @author biz
 */
public interface ReportMapper
{
    public Report selectReportById(Long id);

    public List<Report> selectReportList(Report report);

    public int insertReport(Report report);

    public int updateReport(Report report);

    public int deleteReportByIds(String[] ids);
}
