package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.business.domain.LeaveQuota;
import com.ruoyi.business.mapper.LeaveQuotaMapper;
import com.ruoyi.business.service.ILeaveQuotaService;

/**
 * 假期余额 服务层实现
 *
 * @author biz
 */
@Service
public class LeaveQuotaServiceImpl implements ILeaveQuotaService
{
    @Autowired
    private LeaveQuotaMapper quotaMapper;

    @Override
    public LeaveQuota selectLeaveQuotaById(Long id)
    {
        return quotaMapper.selectLeaveQuotaById(id);
    }

    @Override
    public List<LeaveQuota> selectLeaveQuotaList(LeaveQuota quota)
    {
        return quotaMapper.selectLeaveQuotaList(quota);
    }

    @Override
    public LeaveQuota findQuota(String empName, String leaveType, String year)
    {
        LeaveQuota query = new LeaveQuota();
        query.setEmpName(empName);
        query.setLeaveType(leaveType);
        query.setYear(year);
        return quotaMapper.selectLeaveQuotaUnique(query);
    }

    @Override
    public BigDecimal findRemainDays(String empName, String leaveType, String year)
    {
        LeaveQuota quota = findQuota(empName, leaveType, year);
        // 无配额记录 = 该类型未配置限额，返回足够大的值表示不限额
        return quota == null ? new BigDecimal("99999") : quota.getRemainDays();
    }

    @Override
    public int insertLeaveQuota(LeaveQuota quota)
    {
        // 唯一键防重：同员工+类型+年份只允许一条
        if (findQuota(quota.getEmpName(), quota.getLeaveType(), quota.getYear()) != null)
        {
            throw new ServiceException("该员工此假期类型在" + quota.getYear() + "年已有余额记录，请直接修改");
        }
        if (quota.getUsedDays() == null)
        {
            quota.setUsedDays(BigDecimal.ZERO);
        }
        if (quota.getQuotaDays() == null)
        {
            quota.setQuotaDays(BigDecimal.ZERO);
        }
        if (quota.getUsedDays().compareTo(quota.getQuotaDays()) > 0)
        {
            throw new ServiceException("已用天数不能大于配额天数");
        }
        return quotaMapper.insertLeaveQuota(quota);
    }

    @Override
    public int updateLeaveQuota(LeaveQuota quota)
    {
        LeaveQuota db = quotaMapper.selectLeaveQuotaById(quota.getId());
        if (db == null)
        {
            throw new ServiceException("余额记录不存在");
        }
        // 改了员工/类型/年份时检查唯一键
        LeaveQuota changed = new LeaveQuota();
        changed.setEmpName(quota.getEmpName() != null ? quota.getEmpName() : db.getEmpName());
        changed.setLeaveType(quota.getLeaveType() != null ? quota.getLeaveType() : db.getLeaveType());
        changed.setYear(quota.getYear() != null ? quota.getYear() : db.getYear());
        LeaveQuota exist = findQuota(changed.getEmpName(), changed.getLeaveType(), changed.getYear());
        if (exist != null && !exist.getId().equals(db.getId()))
        {
            throw new ServiceException("该员工此假期类型在" + changed.getYear() + "年已有余额记录");
        }
        BigDecimal quotaDays = quota.getQuotaDays() != null ? quota.getQuotaDays() : db.getQuotaDays();
        BigDecimal usedDays = quota.getUsedDays() != null ? quota.getUsedDays() : db.getUsedDays();
        if (usedDays.compareTo(quotaDays) > 0)
        {
            throw new ServiceException("已用天数不能大于配额天数");
        }
        return quotaMapper.updateLeaveQuota(quota);
    }

    @Override
    public int deleteLeaveQuotaByIds(String ids)
    {
        return quotaMapper.deleteLeaveQuotaByIds(Convert.toStrArray(ids));
    }

    @Override
    public boolean deductUsedDays(String empName, String leaveType, String year, BigDecimal days)
    {
        LeaveQuota quota = findQuota(empName, leaveType, year);
        if (quota == null)
        {
            return false;
        }
        LeaveQuota update = new LeaveQuota();
        update.setId(quota.getId());
        update.setUsedDays(days);
        update.setQuotaDays(quota.getQuotaDays()); // 仅作SQL并发兜底条件
        return quotaMapper.adjustUsedDays(update) > 0;
    }

    @Override
    public boolean refundUsedDays(String empName, String leaveType, String year, BigDecimal days)
    {
        LeaveQuota quota = findQuota(empName, leaveType, year);
        if (quota == null)
        {
            return false;
        }
        LeaveQuota update = new LeaveQuota();
        update.setId(quota.getId());
        update.setUsedDays(days.negate());
        return quotaMapper.adjustUsedDays(update) > 0;
    }
}
