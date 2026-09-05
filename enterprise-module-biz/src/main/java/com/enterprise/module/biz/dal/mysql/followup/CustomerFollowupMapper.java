package com.enterprise.module.biz.dal.mysql.followup;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.mybatis.core.mapper.BaseMapperX;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.enterprise.module.biz.controller.admin.followup.vo.followup.CustomerFollowupPageReqVO;
import com.enterprise.module.biz.dal.dataobject.followup.CustomerFollowupDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CustomerFollowupMapper extends BaseMapperX<CustomerFollowupDO> {

    /**
     * 分页查询
     */
    default PageResult<CustomerFollowupDO> selectPage(CustomerFollowupPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CustomerFollowupDO>()
                .eqIfPresent(CustomerFollowupDO::getCustomerName, reqVO.getCustomerName())
                .eqIfPresent(CustomerFollowupDO::getMethod, reqVO.getMethod())
                .betweenIfPresent(CustomerFollowupDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(CustomerFollowupDO::getId));
    }

    /**
     * 按客户查询跟进记录（时间倒序）
     */
    default List<CustomerFollowupDO> selectListByCustomer(String customerName) {
        return selectList(new LambdaQueryWrapperX<CustomerFollowupDO>()
                .eq(CustomerFollowupDO::getCustomerName, customerName)
                .orderByDesc(CustomerFollowupDO::getFollowTime));
    }
}
