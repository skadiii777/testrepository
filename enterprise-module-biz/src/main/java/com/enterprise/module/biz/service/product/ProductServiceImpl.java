package com.enterprise.module.biz.service.product;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.module.biz.controller.admin.product.vo.product.ProductPageReqVO;
import com.enterprise.module.biz.controller.admin.product.vo.product.ProductSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.product.ProductDO;
import com.enterprise.module.biz.dal.mysql.product.ProductMapper;
import com.enterprise.module.biz.enums.ErrorCodeConstants;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;


import static com.enterprise.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.enterprise.module.biz.enums.ErrorCodeConstants.*;

/**
 * 产品 Service 实现类
 *
 * @author 企业管理平台
 */
@Service
@Validated
public class ProductServiceImpl implements ProductService {

    @Resource
    private ProductMapper productMapper;

    @Override
    public Long createProduct(ProductSaveReqVO createReqVO) {
        ProductDO product = BeanUtils.toBean(createReqVO, ProductDO.class);
        productMapper.insert(product);
        return product.getId();
    }


    @Override
    public void updateProduct(ProductSaveReqVO updateReqVO) {
        validateProductExists(updateReqVO.getId());
        ProductDO updateObj = BeanUtils.toBean(updateReqVO, ProductDO.class);
        productMapper.updateById(updateObj);
    }


    @Override
    public void deleteProduct(Long id) {
        validateProductExists(id);
        productMapper.deleteById(id);
    }

    private void validateProductExists(Long id) {
        if (productMapper.selectById(id) == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
    }

    @Override
    public ProductDO getProduct(Long id) {
        return productMapper.selectById(id);
    }

    @Override
    public PageResult<ProductDO> getProductPage(ProductPageReqVO pageReqVO) {
        return productMapper.selectPage(pageReqVO);
    }
}