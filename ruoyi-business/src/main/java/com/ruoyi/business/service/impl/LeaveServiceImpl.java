package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.business.domain.Leave;
import com.ruoyi.business.mapper.LeaveMapper;
import com.ruoyi.business.mapper.LeaveQuotaMapper;
import com.ruoyi.business.domain.LeaveQuota;
import com.ruoyi.business.service.ILeaveService;

/**
 * 请假 服务层实现
 * 
 * @author biz
 */
@Service
public class LeaveServiceImpl implements ILeaveService
{
    @Autowired
    private LeaveMapper leaveMapper;

    @Autowired
    private LeaveQuotaMapper quotaMapper;

    @Override
    public Leave selectLeaveById(Long id)
    {
        return leaveMapper.selectLeaveById(id);
    }

    @Override
    public List<Leave> selectLeaveList(Leave leave)
    {
        return leaveMapper.selectLeaveList(leave);
    }

    @Override
    public int insertLeave(Leave leave)
    {
        return leaveMapper.insertLeave(leave);
    }

    @Override
    public int updateLeave(Leave leave)
    {
        return leaveMapper.updateLeave(leave);
    }

    @Override
    public int deleteLeaveByIds(String ids)
    {
        return leaveMapper.deleteLeaveByIds(Convert.toStrArray(ids));
    }

    /**
     * 审批请假（status: 1=通过 2=驳回）
     * 通过时若员工当年度该类型有假期配额，则扣减已用天数
     */
    @Override
    public int auditLeave(Long id, String status, String auditRemark)
    {
        if ("1".equals(status))
        {
            Leave leave = leaveMapper.selectLeaveById(id);
            if (leave != null)
            {
                deductQuota(leave, "审批");
            }
        }
        Leave update = new Leave();
        update.setId(id);
        update.setStatus(status);
        update.setUpdateBy(ShiroUtils.getLoginName());
        update.setRemark(auditRemark);
        return leaveMapper.updateLeave(update);
    }

    /**
     * 扣减假期余额：仅当该员工当年该类型存在配额记录时扣；无记录不拦批（未配置配额的类型不限额）
     * 余额不足时抛异常拒绝审批通过
     */
    private void deductQuota(Leave leave, String scene)
    {
        String year = leave.getStartDate() != null && leave.getStartDate().length() >= 4
                ? leave.getStartDate().substring(0, 4) : String.valueOf(java.time.Year.now().getValue());
        LeaveQuota query = new LeaveQuota();
        query.setEmpName(leave.getEmpName());
        query.setLeaveType(leave.getLeaveType());
        query.setYear(year);
        LeaveQuota quota = quotaMapper.selectLeaveQuotaUnique(query);
        if (quota == null)
        {
            return;
        }
        BigDecimal days = leave.getDays() == null ? BigDecimal.ZERO : leave.getDays();
        BigDecimal remain = quota.getRemainDays();
        if (remain.compareTo(days) < 0)
        {
            throw new ServiceException(String.format("%s失败：[%s]%d年%s余额不足，剩余%s天，本次申请%s天",
                    scene, leave.getEmpName(), Integer.parseInt(year), typeLabel(leave.getLeaveType()), remain, days));
        }
        LeaveQuota upd = new LeaveQuota();
        upd.setId(quota.getId());
        upd.setUsedDays(days);
        upd.setQuotaDays(quota.getQuotaDays());
        quotaMapper.adjustUsedDays(upd);
    }

    private String typeLabel(String type)
    {
        switch (type == null ? "" : type)
        {
            case "1": return "事假";
            case "2": return "病假";
            case "3": return "年假";
            case "4": return "调休";
            default: return "假期";
        }
    }

}
