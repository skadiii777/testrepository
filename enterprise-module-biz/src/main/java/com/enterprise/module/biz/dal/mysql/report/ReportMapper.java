package com.enterprise.module.biz.dal.mysql.report;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.mybatis.core.mapper.BaseMapperX;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.enterprise.module.biz.controller.admin.report.vo.report.ReportPageReqVO;
import com.enterprise.module.biz.dal.dataobject.report.ReportDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReportMapper extends BaseMapperX<ReportDO> {

    /**
     * 分页查询
     */
    default PageResult<ReportDO> selectPage(ReportPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ReportDO>()
                .eqIfPresent(ReportDO::getReportType, reqVO.getReportType())
                .likeIfPresent(ReportDO::getTitle, reqVO.getTitle())
                .eqIfPresent(ReportDO::getReportDate, reqVO.getReportDate())
                .betweenIfPresent(ReportDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ReportDO::getId));
    }

}