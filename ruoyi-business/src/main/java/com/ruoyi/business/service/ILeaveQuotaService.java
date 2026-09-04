package com.ruoyi.business.service;

import java.util.List;
import java.math.BigDecimal;
import com.ruoyi.business.domain.LeaveQuota;

/**
 * 假期余额 服务层
 *
 * @author biz
 */
public interface ILeaveQuotaService
{
    public LeaveQuota selectLeaveQuotaById(Long id);

    public List<LeaveQuota> selectLeaveQuotaList(LeaveQuota quota);

    /** 查询员工某年某类型的余额记录，无记录返回 null */
    public LeaveQuota findQuota(String empName, String leaveType, String year);

    /** 查询员工某年某类型的剩余天数，无记录返回 0 */
    public BigDecimal findRemainDays(String empName, String leaveType, String year);

    public int insertLeaveQuota(LeaveQuota quota);

    public int updateLeaveQuota(LeaveQuota quota);

    public int deleteLeaveQuotaByIds(String ids);

    /**
     * 扣减已用天数（审批通过时调用）
     *
     * @return true=扣减成功；false=无记录或余额不足
     */
    public boolean deductUsedDays(String empName, String leaveType, String year, BigDecimal days);

    /**
     * 返还已用天数（销假时调用），不校验配额
     *
     * @return true=返还成功；false=无记录
     */
    public boolean refundUsedDays(String empName, String leaveType, String year, BigDecimal days);
}
