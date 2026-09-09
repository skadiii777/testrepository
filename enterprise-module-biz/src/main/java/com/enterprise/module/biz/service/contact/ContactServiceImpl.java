package com.enterprise.module.biz.service.contact;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.module.biz.controller.admin.contact.vo.contact.ContactPageReqVO;
import com.enterprise.module.biz.controller.admin.contact.vo.contact.ContactSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.contact.ContactDO;
import com.enterprise.module.biz.dal.dataobject.customer.CustomerDO;
import com.enterprise.module.biz.dal.mysql.contact.ContactMapper;
import com.enterprise.module.biz.dal.mysql.customer.CustomerMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static com.enterprise.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.enterprise.module.biz.enums.ErrorCodeConstants.*;

/**
 * 客户联系人 Service 实现类
 *
 * @author 企业管理平台
 */
@Slf4j
@Service
@Validated
public class ContactServiceImpl implements ContactService {

    @Resource
    private ContactMapper contactMapper;
    @Resource
    private CustomerMapper customerMapper;

    @Override
    public Long createContact(ContactSaveReqVO createReqVO) {
        CustomerDO customer = customerMapper.selectById(createReqVO.getCustomerId());
        if (customer == null) {
            throw exception(CONTACT_CUSTOMER_NOT_EXISTS);
        }
        ContactDO contact = BeanUtils.toBean(createReqVO, ContactDO.class);
        contact.setId(null);
        contact.setCustomerName(customer.getCustomerName());
        contactMapper.insert(contact);
        return contact.getId();
    }

    @Override
    public void updateContact(ContactSaveReqVO updateReqVO) {
        ContactDO exists = validateContactExists(updateReqVO.getId());
        // 换客户时重新校验并刷新冗余名称
        if (updateReqVO.getCustomerId() != null
                && !updateReqVO.getCustomerId().equals(exists.getCustomerId())) {
            CustomerDO customer = customerMapper.selectById(updateReqVO.getCustomerId());
            if (customer == null) {
                throw exception(CONTACT_CUSTOMER_NOT_EXISTS);
            }
        }
        ContactDO updateObj = BeanUtils.toBean(updateReqVO, ContactDO.class);
        if (updateObj.getCustomerId() != null) {
            CustomerDO customer = customerMapper.selectById(updateObj.getCustomerId());
            updateObj.setCustomerName(customer != null ? customer.getCustomerName() : null);
        }
        contactMapper.updateById(updateObj);
    }

    @Override
    public void deleteContact(Long id) {
        validateContactExists(id);
        contactMapper.deleteById(id);
    }

    @Override
    public ContactDO getContact(Long id) {
        return contactMapper.selectById(id);
    }

    @Override
    public PageResult<ContactDO> getContactPage(ContactPageReqVO pageReqVO) {
        return contactMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ContactDO> getContactListByCustomer(Long customerId) {
        return contactMapper.selectListByCustomer(customerId);
    }

    private ContactDO validateContactExists(Long id) {
        ContactDO contact = contactMapper.selectById(id);
        if (contact == null) {
            throw exception(CONTACT_NOT_EXISTS);
        }
        return contact;
    }

}
