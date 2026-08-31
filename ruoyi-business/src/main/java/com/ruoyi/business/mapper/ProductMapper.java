package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.Product;

/**
 * 产品 数据层
 * 
 * @author biz
 */
public interface ProductMapper
{
    public Product selectProductById(Long id);

    public List<Product> selectProductList(Product product);

    public int insertProduct(Product product);

    public int updateProduct(Product product);

    public int deleteProductByIds(String[] ids);
}
