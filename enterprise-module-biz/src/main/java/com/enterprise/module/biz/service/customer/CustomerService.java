package com.enterprise.module.biz.service.customer;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.module.biz.controller.admin.customer.vo.customer.CustomerPageReqVO;
import com.enterprise.module.biz.controller.admin.customer.vo.customer.CustomerSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.customer.CustomerDO;

/**
 * 客户 Service 接口
 *
 * @author 企业管理平台
 */
public interface CustomerService {

    /**
     * 创建客户
     */
    Long createCustomer(CustomerSaveReqVO createReqVO);

    /**
     * 更新客户
     */
    void updateCustomer(CustomerSaveReqVO updateReqVO);

    /**
     * 删除客户
     */
    void deleteCustomer(Long id);

    /**
     * 获得客户
     */
    CustomerDO getCustomer(Long id);

    /**
     * 获得客户分页
     */
    PageResult<CustomerDO> getCustomerPage(CustomerPageReqVO pageReqVO);

}