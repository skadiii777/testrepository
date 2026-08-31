package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.Customer;

/**
 * 客户 服务层
 * 
 * @author biz
 */
public interface ICustomerService 
{
    public Customer selectCustomerById(Long id);

    public List<Customer> selectCustomerList(Customer customer);

    public int insertCustomer(Customer customer);

    public int updateCustomer(Customer customer);

    public int deleteCustomerByIds(String ids);
}
