package com.enterprise.module.biz.dal.mysql.product;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.mybatis.core.mapper.BaseMapperX;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.enterprise.module.biz.controller.admin.product.vo.product.ProductPageReqVO;
import com.enterprise.module.biz.dal.dataobject.product.ProductDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapperX<ProductDO> {
    default ProductDO selectForUpdate(Long id) {
        return selectOne(new LambdaQueryWrapperX<ProductDO>().eq(ProductDO::getId,id).last("FOR UPDATE"));
    }


    /**
     * 分页查询
     */
    default PageResult<ProductDO> selectPage(ProductPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ProductDO>()
                .likeIfPresent(ProductDO::getProductCode, reqVO.getProductCode())
                .likeIfPresent(ProductDO::getProductName, reqVO.getProductName())
                .eqIfPresent(ProductDO::getCategory, reqVO.getCategory())
                .eqIfPresent(ProductDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ProductDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ProductDO::getId));
    }

}