package com.enterprise.module.biz.service.quota;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.module.biz.controller.admin.quota.vo.quota.LeaveQuotaPageReqVO;
import com.enterprise.module.biz.controller.admin.quota.vo.quota.LeaveQuotaSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.quota.LeaveQuotaDO;

/**
 * 假期余额 Service 接口
 *
 * @author 企业管理平台
 */
public interface LeaveQuotaService {

    /**
     * 创建假期余额
     */
    Long createLeaveQuota(LeaveQuotaSaveReqVO createReqVO);

    /**
     * 更新假期余额
     */
    void updateLeaveQuota(LeaveQuotaSaveReqVO updateReqVO);

    /**
     * 删除假期余额
     */
    void deleteLeaveQuota(Long id);

    /**
     * 获得假期余额
     */
    LeaveQuotaDO getLeaveQuota(Long id);

    /**
     * 获得假期余额分页
     */
    PageResult<LeaveQuotaDO> getLeaveQuotaPage(LeaveQuotaPageReqVO pageReqVO);

    /**
     * 查询剩余天数，无配额记录返回足够大值（不限额）
     */
    java.math.BigDecimal findRemainDays(String empName, String leaveType, String year);

    /**
     * 原子扣减已用天数（余额不足抛异常）
     */
    void deductUsedDays(String empName, String leaveType, String year, java.math.BigDecimal days);

    /**
     * 原子返还已用天数
     */
    void refundUsedDays(String empName, String leaveType, String year, java.math.BigDecimal days);
}