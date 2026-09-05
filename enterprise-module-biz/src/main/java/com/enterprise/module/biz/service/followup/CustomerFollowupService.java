package com.enterprise.module.biz.service.followup;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.module.biz.controller.admin.followup.vo.followup.CustomerFollowupPageReqVO;
import com.enterprise.module.biz.controller.admin.followup.vo.followup.CustomerFollowupSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.followup.CustomerFollowupDO;

import java.util.List;

/**
 * 客户跟进 Service 接口
 *
 * @author 企业管理平台
 */
public interface CustomerFollowupService {

    /**
     * 创建跟进记录
     */
    Long createFollowup(CustomerFollowupSaveReqVO createReqVO);

    /**
     * 删除跟进记录
     */
    void deleteFollowup(Long id);

    /**
     * 获得跟进记录
     */
    CustomerFollowupDO getFollowup(Long id);

    /**
     * 获得跟进分页
     */
    PageResult<CustomerFollowupDO> getFollowupPage(CustomerFollowupPageReqVO pageReqVO);

    /**
     * 按客户查询跟进记录（时间倒序，客户详情用）
     */
    List<CustomerFollowupDO> getFollowupListByCustomer(String customerName);
}
