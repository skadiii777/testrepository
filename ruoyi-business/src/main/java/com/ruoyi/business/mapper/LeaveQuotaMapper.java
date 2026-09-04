package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.LeaveQuota;

/**
 * 假期余额 数据层
 *
 * @author biz
 */
public interface LeaveQuotaMapper
{
    public LeaveQuota selectLeaveQuotaById(Long id);

    /** 精确查询：员工+类型+年份（唯一键） */
    public LeaveQuota selectLeaveQuotaUnique(LeaveQuota quota);

    public List<LeaveQuota> selectLeaveQuotaList(LeaveQuota quota);

    public int insertLeaveQuota(LeaveQuota quota);

    public int updateLeaveQuota(LeaveQuota quota);

    /** 原子增减已用天数（delta 正数扣减、负数返还） */
    public int adjustUsedDays(LeaveQuota quota);

    public int deleteLeaveQuotaByIds(String[] ids);
}
