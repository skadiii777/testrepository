package com.enterprise.module.biz.service.report;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.module.biz.controller.admin.report.vo.report.ReportPageReqVO;
import com.enterprise.module.biz.controller.admin.report.vo.report.ReportSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.report.ReportDO;
import com.enterprise.module.biz.dal.mysql.report.ReportMapper;
import com.enterprise.module.biz.enums.ErrorCodeConstants;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;


import static com.enterprise.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.enterprise.module.biz.enums.ErrorCodeConstants.*;

/**
 * 业务汇报 Service 实现类
 *
 * @author 企业管理平台
 */
@Service
@Validated
public class ReportServiceImpl implements ReportService {

    @Resource
    private ReportMapper reportMapper;

    @Override
    public Long createReport(ReportSaveReqVO createReqVO) {
        ReportDO report = BeanUtils.toBean(createReqVO, ReportDO.class);
        reportMapper.insert(report);
        return report.getId();
    }


    @Override
    public PageResult<ReportDO> getReportPageSelf(ReportPageReqVO pageReqVO, Long userId) {
        return reportMapper.selectPage(pageReqVO, new LambdaQueryWrapperX<ReportDO>()
                .eq(ReportDO::getCreator, String.valueOf(userId))
                .orderByDesc(ReportDO::getId));
    }
    @Override
    public void updateReport(ReportSaveReqVO updateReqVO) {
        validateReportExists(updateReqVO.getId());
        ReportDO updateObj = BeanUtils.toBean(updateReqVO, ReportDO.class);
        reportMapper.updateById(updateObj);
    }


    @Override
    public void deleteReport(Long id) {
        validateReportExists(id);
        reportMapper.deleteById(id);
    }

    private void validateReportExists(Long id) {
        if (reportMapper.selectById(id) == null) {
            throw exception(REPORT_NOT_EXISTS);
        }
    }

    @Override
    public ReportDO getReport(Long id) {
        return reportMapper.selectById(id);
    }

    @Override
    public PageResult<ReportDO> getReportPage(ReportPageReqVO pageReqVO) {
        return reportMapper.selectPage(pageReqVO);
    }
}