package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.Leave;

/**
 * 请假 服务层
 * 
 * @author biz
 */
public interface ILeaveService 
{
    public Leave selectLeaveById(Long id);

    public List<Leave> selectLeaveList(Leave leave);

    public int insertLeave(Leave leave);

    public int updateLeave(Leave leave);

    public int deleteLeaveByIds(String ids);

    public int auditLeave(Long id, String status, String auditRemark);
}
