package com.enterprise.module.biz.dal.mysql.customer;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.mybatis.core.mapper.BaseMapperX;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.enterprise.module.biz.controller.admin.customer.vo.customer.CustomerPageReqVO;
import com.enterprise.module.biz.dal.dataobject.customer.CustomerDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CustomerMapper extends BaseMapperX<CustomerDO> {

    /**
     * 分页查询
     */
    default PageResult<CustomerDO> selectPage(CustomerPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CustomerDO>()
                .likeIfPresent(CustomerDO::getCustomerName, reqVO.getCustomerName())
                .eqIfPresent(CustomerDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(CustomerDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(CustomerDO::getId));
    }

}