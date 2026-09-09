package com.enterprise.module.biz.service.business;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.module.biz.controller.admin.business.vo.business.BusinessFunnelRespVO;
import com.enterprise.module.biz.controller.admin.business.vo.business.BusinessPageReqVO;
import com.enterprise.module.biz.controller.admin.business.vo.business.BusinessSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.business.BusinessDO;

import java.util.List;

/**
 * 商机 Service 接口
 *
 * @author 企业管理平台
 */
public interface BusinessService {

    /**
     * 创建商机（负责人=当前登录人；阶段限 1-6）
     */
    Long createBusiness(BusinessSaveReqVO createReqVO);

    /**
     * 更新商机（赢单/输单为终局，不可更新）
     */
    void updateBusiness(BusinessSaveReqVO updateReqVO);

    /**
     * 删除商机
     */
    void deleteBusiness(Long id);

    /**
     * 获得商机
     */
    BusinessDO getBusiness(Long id);

    /**
     * 获得商机分页
     */
    PageResult<BusinessDO> getBusinessPage(BusinessPageReqVO pageReqVO);

    /**
     * 销售漏斗统计：各阶段商机数量与预期金额（固定返回 1-6 阶段，含 0）
     */
    List<BusinessFunnelRespVO> getFunnelStats();

}
