package com.enterprise.module.biz.service.product;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.module.biz.controller.admin.product.vo.product.ProductPageReqVO;
import com.enterprise.module.biz.controller.admin.product.vo.product.ProductSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.product.ProductDO;

/**
 * 产品 Service 接口
 *
 * @author 企业管理平台
 */
public interface ProductService {

    /**
     * 创建产品
     */
    Long createProduct(ProductSaveReqVO createReqVO);

    /**
     * 更新产品
     */
    void updateProduct(ProductSaveReqVO updateReqVO);

    /**
     * 删除产品
     */
    void deleteProduct(Long id);

    /**
     * 获得产品
     */
    ProductDO getProduct(Long id);

    /**
     * 获得产品分页
     */
    PageResult<ProductDO> getProductPage(ProductPageReqVO pageReqVO);

}