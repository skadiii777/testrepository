package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.business.domain.Report;
import com.ruoyi.business.mapper.ReportMapper;
import com.ruoyi.business.service.IReportService;

/**
 * 业务汇报 服务层实现
 *
 * @author biz
 */
@Service
public class ReportServiceImpl implements IReportService
{
    @Autowired
    private ReportMapper reportMapper;

    @Override
    public Report selectReportById(Long id)
    {
        return reportMapper.selectReportById(id);
    }

    @Override
    public List<Report> selectReportList(Report report)
    {
        return reportMapper.selectReportList(report);
    }

    @Override
    public int insertReport(Report report)
    {
        return reportMapper.insertReport(report);
    }

    @Override
    public int updateReport(Report report)
    {
        return reportMapper.updateReport(report);
    }

    @Override
    public int deleteReportByIds(String ids)
    {
        return reportMapper.deleteReportByIds(Convert.toStrArray(ids));
    }
}
