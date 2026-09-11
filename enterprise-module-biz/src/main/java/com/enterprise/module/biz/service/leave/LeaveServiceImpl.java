package com.enterprise.module.biz.service.leave;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.module.biz.controller.admin.leave.vo.leave.LeavePageReqVO;
import com.enterprise.module.biz.controller.admin.leave.vo.leave.LeaveSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.leave.LeaveDO;
import com.enterprise.module.biz.dal.mysql.leave.LeaveMapper;
import com.enterprise.module.bpm.api.task.BpmProcessInstanceApi;
import com.enterprise.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class LeaveServiceImpl implements LeaveService {

    @Resource
    private LeaveMapper leaveMapper;
    @Resource
    private LeaveQuotaService leaveQuotaService;
    @Resource private com.enterprise.module.biz.service.support.BizReferenceService references;

    @Resource
    private BpmProcessInstanceApi processInstanceApi;

    /** 请假流程定义 KEY（BPM 模型部署后生效） */
    public static final String PROCESS_KEY = "biz_leave";



    @Override
    public Long createLeave(LeaveSaveReqVO createReqVO) {
        if (createReqVO.getDays() == null || createReqVO.getDays().signum() <= 0) throw exception(LEAVE_DAYS_INVALID);
        LeaveDO leave = BeanUtils.toBean(createReqVO, LeaveDO.class);
        var employee = references.employee(leave.getEmployeeId(),leave.getEmpName());
        leave.setEmployeeId(employee.getId()); leave.setEmpName(employee.getEmpName());
        leave.setStatus("0"); // 强制初始状态，防止客户端篡改
        leaveMapper.insert(leave);
        // 尝试发起 BPM 流程；未部署流程模型时降级为本地直批模式（状态仍由 auditLeave 驱动）
        try {
            String processInstanceId = processInstanceApi.createProcessInstance(
                    Long.valueOf(leave.getCreator()),
                    new BpmProcessInstanceCreateReqDTO().setProcessDefinitionKey(PROCESS_KEY)
                            .setBusinessKey(String.valueOf(leave.getId())));
            leaveMapper.updateById(new LeaveDO().setId(leave.getId())
                    .setProcessInstanceId(processInstanceId));
        } catch (Exception e) {
            log.warn("[createLeave][BPM 流程未部署或发起失败，降级为本地直批] leaveId({}) 原因: {}",
                    leave.getId(), e.getMessage());
        }
        return leave.getId();
    }

    @Override
    public PageResult<LeaveDO> getLeavePageSelf(LeavePageReqVO pageReqVO, Long userId) {
        return leaveMapper.selectPage(pageReqVO, new LambdaQueryWrapperX<LeaveDO>()
                .eq(LeaveDO::getCreator, String.valueOf(userId))
                .orderByDesc(LeaveDO::getId));
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void updateLeaveStatusFromBpm(Long id, Integer status) {
        if (!Integer.valueOf(2).equals(status) && !Integer.valueOf(3).equals(status)) return;
        LeaveDO leave = leaveMapper.selectForUpdate(id);
        if (leave == null) return;
        String target = Integer.valueOf(2).equals(status) ? "1" : "2";
        // Replayed BPM events have no side effects.
        if (leaveMapper.updateStatusCas(id, target, null) == 0) return;
        if ("1".equals(target)) leaveQuotaService.deductUsedDays(leave.getEmployeeId(),leave.getLeaveType(),
                yearOf(leave.getStartDate()),leave.getDays());
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void auditLeave(Long id, String status, String auditRemark) {
        LeaveDO leave = leaveMapper.selectForUpdate(id);
        if (leave == null) {
            throw exception(LEAVE_NOT_EXISTS);
        }
        if (!"1".equals(status) && !"2".equals(status)) throw exception(EXPENSE_AUDIT_STATUS_INVALID);
        // CAS 防重：仅待审批(0)可流转；重复审批/并发审批只会有一个成功
        int rows = leaveMapper.updateStatusCas(id, status, auditRemark);
        if (rows == 0) {
            throw exception(LEAVE_ALREADY_AUDITED);
        }
        if ("1".equals(status)) {
            leaveQuotaService.deductUsedDays(leave.getEmployeeId(), leave.getLeaveType(),
                    yearOf(leave.getStartDate()), leave.getDays());
        }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void cancelLeave(Long id, Long loginUserId) {
        LeaveDO leave = leaveMapper.selectForUpdate(id);
        if (leave == null) {
            throw exception(LEAVE_NOT_EXISTS);
        }
        if (!String.valueOf(loginUserId).equals(leave.getCreator())) {
            throw exception(PORTAL_NOT_OWNER);
        }
        if (!"1".equals(leave.getStatus())) {
            throw exception(LEAVE_CANCEL_ONLY_APPROVED);
        }
        // CAS 防重：仅已通过(1)可销假，重复销假只会有一个成功（避免重复返还余额）
        if (leaveMapper.updateStatusByCas(id, "1", "3", "员工申请销假（提前返岗）") == 0) {
            throw exception(LEAVE_ALREADY_AUDITED);
        }
        leaveQuotaService.refundUsedDays(leave.getEmployeeId(), leave.getLeaveType(),
                yearOf(leave.getStartDate()), leave.getDays());
    }

    private String yearOf(String date) {
        return date != null && date.length() >= 4 ? date.substring(0, 4)
                : String.valueOf(java.time.Year.now().getValue());
    }
    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void updateLeave(LeaveSaveReqVO updateReqVO) {
        LeaveDO old = leaveMapper.selectForUpdate(updateReqVO.getId());
        if (old == null) throw exception(LEAVE_NOT_EXISTS);
        if (!"0".equals(old.getStatus())) throw exception(LEAVE_ALREADY_AUDITED);
        if (updateReqVO.getDays() != null && updateReqVO.getDays().signum() <= 0) throw exception(LEAVE_DAYS_INVALID);
        LeaveDO updateObj = BeanUtils.toBean(updateReqVO, LeaveDO.class);
        updateObj.setEmployeeId(old.getEmployeeId()); updateObj.setEmpName(old.getEmpName()); updateObj.setStatus(null);
        updateObj.setProcessInstanceId(null);
        leaveMapper.updateById(updateObj);
    }


    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void deleteLeave(Long id) {
        LeaveDO leave = leaveMapper.selectForUpdate(id);
        if (leave == null) throw exception(LEAVE_NOT_EXISTS);
        if (!"0".equals(leave.getStatus())) throw exception(LEAVE_ALREADY_AUDITED);
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
