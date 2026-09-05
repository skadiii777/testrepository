package com.enterprise.module.biz.service.report;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.module.biz.controller.admin.report.vo.report.ReportPageReqVO;
import com.enterprise.module.biz.controller.admin.report.vo.report.ReportSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.report.ReportDO;

/**
 * 业务汇报 Service 接口
 *
 * @author 企业管理平台
 */
public interface ReportService {

    /**
     * 创建业务汇报
     */
    Long createReport(ReportSaveReqVO createReqVO);

    /**
     * 更新业务汇报
     */
    void updateReport(ReportSaveReqVO updateReqVO);

    /**
     * 删除业务汇报
     */
    void deleteReport(Long id);

    /**
     * 获得业务汇报
     */
    ReportDO getReport(Long id);

    /**
     * 获得业务汇报分页
     */
    PageResult<ReportDO> getReportPage(ReportPageReqVO pageReqVO);

    /**
     * 仅本人分页（员工工作台）
     */
    PageResult<ReportDO> getReportPageSelf(ReportPageReqVO pageReqVO, Long userId);
}