package com.enterprise.module.biz.service.contact;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.module.biz.controller.admin.contact.vo.contact.ContactPageReqVO;
import com.enterprise.module.biz.controller.admin.contact.vo.contact.ContactSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.contact.ContactDO;

import java.util.List;

/**
 * 客户联系人 Service 接口
 *
 * @author 企业管理平台
 */
public interface ContactService {

    /**
     * 创建联系人（校验客户存在，冗余客户名称）
     */
    Long createContact(ContactSaveReqVO createReqVO);

    /**
     * 更新联系人
     */
    void updateContact(ContactSaveReqVO updateReqVO);

    /**
     * 删除联系人
     */
    void deleteContact(Long id);

    /**
     * 获得联系人
     */
    ContactDO getContact(Long id);

    /**
     * 获得联系人分页
     */
    PageResult<ContactDO> getContactPage(ContactPageReqVO pageReqVO);

    /**
     * 查询指定客户的全部联系人
     */
    List<ContactDO> getContactListByCustomer(Long customerId);

}
