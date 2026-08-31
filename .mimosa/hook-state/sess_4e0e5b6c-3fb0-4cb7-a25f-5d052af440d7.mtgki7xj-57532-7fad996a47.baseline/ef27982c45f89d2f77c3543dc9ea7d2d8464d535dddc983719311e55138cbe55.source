package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.business.domain.Product;
import com.ruoyi.business.mapper.ProductMapper;
import com.ruoyi.business.service.IProductService;

/**
 * 产品 服务层实现
 * 
 * @author biz
 */
@Service
public class ProductServiceImpl implements IProductService
{
    @Autowired
    private ProductMapper productMapper;

    @Override
    public Product selectProductById(Long id)
    {
        return productMapper.selectProductById(id);
    }

    @Override
    public List<Product> selectProductList(Product product)
    {
        return productMapper.selectProductList(product);
    }

    @Override
    public int insertProduct(Product product)
    {
        return productMapper.insertProduct(product);
    }

    @Override
    public int updateProduct(Product product)
    {
        return productMapper.updateProduct(product);
    }

    @Override
    public int deleteProductByIds(String ids)
    {
        return productMapper.deleteProductByIds(Convert.toStrArray(ids));
    }

}
