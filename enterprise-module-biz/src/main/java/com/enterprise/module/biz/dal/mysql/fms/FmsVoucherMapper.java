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
        return selectPage(reqVO, new LambdaQueryWrapperX<FmsVoucherDO>()
                .likeIfPresent(FmsVoucherDO::getVoucherNo, reqVO.getVoucherNo())
                .eqIfPresent(FmsVoucherDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(FmsVoucherDO::getVoucherDate, reqVO.getVoucherDateRange())
                .orderByDesc(FmsVoucherDO::getId));
    }

}
