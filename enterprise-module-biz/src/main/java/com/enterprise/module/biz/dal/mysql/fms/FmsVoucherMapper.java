package com.enterprise.module.biz.dal.mysql.fms;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.mybatis.core.mapper.BaseMapperX;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.enterprise.module.biz.controller.admin.fms.vo.fms.FmsVoucherPageReqVO;
import com.enterprise.module.biz.dal.dataobject.fms.FmsVoucherDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FmsVoucherMapper extends BaseMapperX<FmsVoucherDO> {

    default PageResult<FmsVoucherDO> selectPage(FmsVoucherPageReqVO reqVO) {
        LambdaQueryWrapperX<FmsVoucherDO> wrapper = new LambdaQueryWrapperX<FmsVoucherDO>()
                .likeIfPresent(FmsVoucherDO::getVoucherNo, reqVO.getVoucherNo())
                .eqIfPresent(FmsVoucherDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(FmsVoucherDO::getVoucherDate, reqVO.getVoucherDateRange());
        // 手工凭证 source_type 为 NULL，选「manual」时按 IS NULL 过滤
        if ("manual".equals(reqVO.getSourceType())) {
            wrapper.isNull(FmsVoucherDO::getSourceType);
        } else {
            wrapper.eqIfPresent(FmsVoucherDO::getSourceType, reqVO.getSourceType());
        }
        return selectPage(reqVO, wrapper.orderByDesc(FmsVoucherDO::getId));
    }

    default FmsVoucherDO selectBySource(String sourceType, Long sourceId) {
        return selectOne(new LambdaQueryWrapperX<FmsVoucherDO>()
                .eq(FmsVoucherDO::getSourceType, sourceType)
                .eq(FmsVoucherDO::getSourceId, sourceId));
    }

}
