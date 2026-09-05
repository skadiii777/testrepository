package com.enterprise.module.biz.service.leave;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.module.biz.controller.admin.leave.vo.leave.LeavePageReqVO;
import com.enterprise.module.biz.controller.admin.leave.vo.leave.LeaveSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.leave.LeaveDO;
import com.enterprise.module.biz.dal.mysql.leave.LeaveMapper;
import com.enterprise.module.biz.enums.ErrorCodeConstants;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import com.enterprise.module.biz.service.quota.LeaveQuotaService;

import static com.enterprise.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.enterprise.module.biz.enums.ErrorCodeConstants.*;

/**
 * 请假 Service 实现类
 *
 * @author 企业管理平台
 */
@Service
@Validated
public class LeaveServiceImpl implements LeaveService {

    @Resource
    private LeaveMapper leaveMapper;
    @Resource
    private LeaveQuotaService leaveQuotaService;



    @Override
    public Long createLeave(LeaveSaveReqVO createReqVO) {
        LeaveDO leave = BeanUtils.toBean(createReqVO, LeaveDO.class);
        leave.setStatus("0"); // 强制初始状态，防止客户端篡改
        leaveMapper.insert(leave);
        return leave.getId();
    }

    @Override
    public PageResult<LeaveDO> getLeavePageSelf(LeavePageReqVO pageReqVO, Long userId) {
        return leaveMapper.selectPage(pageReqVO, new LambdaQueryWrapperX<LeaveDO>()
                .eq(LeaveDO::getCreator, String.valueOf(userId))
                .orderByDesc(LeaveDO::getId));
    }

    @Override
    public void auditLeave(Long id, String status, String auditRemark) {
        LeaveDO leave = leaveMapper.selectById(id);
        if (leave == null) {
            throw exception(LEAVE_NOT_EXISTS);
        }
        if ("1".equals(status)) {
            leaveQuotaService.deductUsedDays(leave.getEmpName(), leave.getLeaveType(),
                    yearOf(leave.getStartDate()), leave.getDays());
        }
        LeaveDO update = new LeaveDO();
        update.setId(id);
        update.setStatus(status);
        update.setRemark(auditRemark);
        leaveMapper.updateById(update);
    }

    @Override
    public void cancelLeave(Long id, Long loginUserId) {
        LeaveDO leave = leaveMapper.selectById(id);
        if (leave == null) {
            throw exception(LEAVE_NOT_EXISTS);
        }
        if (!String.valueOf(loginUserId).equals(leave.getCreator())) {
            throw exception(PORTAL_NOT_OWNER);
        }
        if (!"1".equals(leave.getStatus())) {
            throw exception(LEAVE_CANCEL_ONLY_APPROVED);
        }
        leaveQuotaService.refundUsedDays(leave.getEmpName(), leave.getLeaveType(),
                yearOf(leave.getStartDate()), leave.getDays());
        LeaveDO update = new LeaveDO();
        update.setId(id);
        update.setStatus("3");
        update.setRemark("员工申请销假（提前返岗）");
        leaveMapper.updateById(update);
    }

    private String yearOf(String date) {
        return date != null && date.length() >= 4 ? date.substring(0, 4)
                : String.valueOf(java.time.Year.now().getValue());
    }
    @Override
    public void updateLeave(LeaveSaveReqVO updateReqVO) {
        validateLeaveExists(updateReqVO.getId());
        LeaveDO updateObj = BeanUtils.toBean(updateReqVO, LeaveDO.class);
        leaveMapper.updateById(updateObj);
    }


    @Override
    public void deleteLeave(Long id) {
        validateLeaveExists(id);
        leaveMapper.deleteById(id);
    }

    private void validateLeaveExists(Long id) {
        if (leaveMapper.selectById(id) == null) {
            throw exception(LEAVE_NOT_EXISTS);
        }
    }

    @Override
    public LeaveDO getLeave(Long id) {
        return leaveMapper.selectById(id);
    }

    @Override
    public PageResult<LeaveDO> getLeavePage(LeavePageReqVO pageReqVO) {
        return leaveMapper.selectPage(pageReqVO);
    }
}