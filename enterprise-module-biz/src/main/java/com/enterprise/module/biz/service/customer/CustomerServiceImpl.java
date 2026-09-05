package com.enterprise.module.biz.service.customer;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.module.biz.controller.admin.customer.vo.customer.CustomerPageReqVO;
import com.enterprise.module.biz.controller.admin.customer.vo.customer.CustomerSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.customer.CustomerDO;
import com.enterprise.module.biz.dal.mysql.customer.CustomerMapper;
import com.enterprise.module.biz.enums.ErrorCodeConstants;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;


import static com.enterprise.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.enterprise.module.biz.enums.ErrorCodeConstants.*;

/**
 * 客户 Service 实现类
 *
 * @author 企业管理平台
 */
@Service
@Validated
public class CustomerServiceImpl implements CustomerService {

    @Resource
    private CustomerMapper customerMapper;

    @Override
    public Long createCustomer(CustomerSaveReqVO createReqVO) {
        CustomerDO customer = BeanUtils.toBean(createReqVO, CustomerDO.class);
        customerMapper.insert(customer);
        return customer.getId();
    }


    @Override
    public void updateCustomer(CustomerSaveReqVO updateReqVO) {
        validateCustomerExists(updateReqVO.getId());
        CustomerDO updateObj = BeanUtils.toBean(updateReqVO, CustomerDO.class);
        customerMapper.updateById(updateObj);
    }


    @Override
    public void deleteCustomer(Long id) {
        validateCustomerExists(id);
        customerMapper.deleteById(id);
    }

    private void validateCustomerExists(Long id) {
        if (customerMapper.selectById(id) == null) {
            throw exception(CUSTOMER_NOT_EXISTS);
        }
    }

    @Override
    public CustomerDO getCustomer(Long id) {
        return customerMapper.selectById(id);
    }

    @Override
    public PageResult<CustomerDO> getCustomerPage(CustomerPageReqVO pageReqVO) {
        return customerMapper.selectPage(pageReqVO);
    }
}