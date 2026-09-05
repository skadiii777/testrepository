package com.enterprise.module.biz.dal.mysql.supplier;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.mybatis.core.mapper.BaseMapperX;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.enterprise.module.biz.controller.admin.supplier.vo.supplier.SupplierPageReqVO;
import com.enterprise.module.biz.dal.dataobject.supplier.SupplierDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SupplierMapper extends BaseMapperX<SupplierDO> {

    /**
     * 分页查询
     */
    default PageResult<SupplierDO> selectPage(SupplierPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SupplierDO>()
                .likeIfPresent(SupplierDO::getSupplierName, reqVO.getSupplierName())
                .eqIfPresent(SupplierDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(SupplierDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SupplierDO::getId));
    }

}