package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.Report;

/**
 * 业务汇报 服务层
 *
 * @author biz
 */
public interface IReportService
{
    public Report selectReportById(Long id);

    public List<Report> selectReportList(Report report);

    public int insertReport(Report report);

    public int updateReport(Report report);

    public int deleteReportByIds(String ids);
}
