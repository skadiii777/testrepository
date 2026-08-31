package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.Customer;

/**
 * 客户 数据层
 * 
 * @author biz
 */
public interface CustomerMapper
{
    public Customer selectCustomerById(Long id);

    public List<Customer> selectCustomerList(Customer customer);

    public int insertCustomer(Customer customer);

    public int updateCustomer(Customer customer);

    public int deleteCustomerByIds(String[] ids);
}
