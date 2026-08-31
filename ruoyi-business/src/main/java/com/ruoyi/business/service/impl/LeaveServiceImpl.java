package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.business.domain.Leave;
import com.ruoyi.business.mapper.LeaveMapper;
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
     */
    @Override
    public int auditLeave(Long id, String status, String auditRemark)
    {
        Leave leave = new Leave();
        leave.setId(id);
        leave.setStatus(status);
        leave.setUpdateBy(ShiroUtils.getLoginName());
        leave.setRemark(auditRemark);
        return leaveMapper.updateLeave(leave);
    }

}
