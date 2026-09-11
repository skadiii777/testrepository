package com.enterprise.module.biz.service.quota;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.module.biz.controller.admin.quota.vo.quota.LeaveQuotaPageReqVO;
import com.enterprise.module.biz.controller.admin.quota.vo.quota.LeaveQuotaSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.quota.LeaveQuotaDO;
import com.enterprise.module.biz.dal.mysql.quota.LeaveQuotaMapper;
import com.enterprise.module.biz.enums.ErrorCodeConstants;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;


import static com.enterprise.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.enterprise.module.biz.enums.ErrorCodeConstants.*;

/**
 * 假期余额 Service 实现类
 *
 * @author 企业管理平台
 */
@Service
@Validated
public class LeaveQuotaServiceImpl implements LeaveQuotaService {

    @Resource
    private LeaveQuotaMapper quotaMapper;
    @Resource private com.enterprise.module.biz.service.support.BizReferenceService references;



    @Override
    public Long createLeaveQuota(LeaveQuotaSaveReqVO createReqVO) {
        var employee = references.employee(createReqVO.getEmployeeId(),createReqVO.getEmpName());
        createReqVO.setEmployeeId(employee.getId()); createReqVO.setEmpName(employee.getEmpName());
        // 唯一键防重：员工ID+类型+年份
        if (quotaMapper.selectUnique(createReqVO.getEmployeeId(), createReqVO.getLeaveType(), createReqVO.getYear()) != null) {
            throw exception(QUOTA_DUPLICATE);
        }
        LeaveQuotaDO quota = BeanUtils.toBean(createReqVO, LeaveQuotaDO.class);
        if (quota.getQuotaDays() == null || quota.getQuotaDays().signum() < 0 || quota.getUsedDays() != null && quota.getUsedDays().signum() < 0) throw exception(QUOTA_USED_OVER);
        if (quota.getUsedDays() == null) {
            quota.setUsedDays(java.math.BigDecimal.ZERO);
        }
        if (quota.getUsedDays().compareTo(quota.getQuotaDays()) > 0) {
            throw exception(QUOTA_USED_OVER);
        }
        quotaMapper.insert(quota);
        return quota.getId();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void updateLeaveQuota(LeaveQuotaSaveReqVO updateReqVO) {
        validateLeaveQuotaExists(updateReqVO.getId());
        var old = quotaMapper.selectForUpdate(updateReqVO.getId());
        var employee = references.employee(updateReqVO.getEmployeeId() != null ? updateReqVO.getEmployeeId() : old.getEmployeeId(),updateReqVO.getEmpName());
        if (!employee.getId().equals(old.getEmployeeId())
                || !java.util.Objects.equals(updateReqVO.getLeaveType(),old.getLeaveType())
                || !java.util.Objects.equals(updateReqVO.getYear(),old.getYear())) throw exception(MASTER_REFERENCE_INVALID);
        updateReqVO.setEmployeeId(employee.getId()); updateReqVO.setEmpName(employee.getEmpName());
        LeaveQuotaDO exist = quotaMapper.selectUnique(updateReqVO.getEmployeeId(), updateReqVO.getLeaveType(), updateReqVO.getYear());
        if (exist != null && !exist.getId().equals(updateReqVO.getId())) {
            throw exception(QUOTA_DUPLICATE);
        }
        if (updateReqVO.getUsedDays() != null && updateReqVO.getUsedDays().compareTo(old.getUsedDays()) != 0) throw exception(QUOTA_USED_OVER);
        if (updateReqVO.getQuotaDays() != null && updateReqVO.getQuotaDays().compareTo(old.getUsedDays()) < 0) throw exception(QUOTA_USED_OVER);
        LeaveQuotaDO updateObj = BeanUtils.toBean(updateReqVO, LeaveQuotaDO.class);
        updateObj.setUsedDays(null);
        quotaMapper.updateById(updateObj);
    }

    @Override
    public java.math.BigDecimal findRemainDays(Long employeeId, String leaveType, String year) {
        LeaveQuotaDO quota = quotaMapper.selectUnique(employeeId, leaveType, year);
        // 无配额记录 = 该类型未配置限额
        return quota == null ? new java.math.BigDecimal("99999") : quota.getRemainDays();
    }

    @Override
    public void deductUsedDays(Long employeeId, String leaveType, String year, java.math.BigDecimal days) {
        if (days == null || days.signum() <= 0) throw exception(LEAVE_DAYS_INVALID);
        LeaveQuotaDO quota = quotaMapper.selectUnique(employeeId, leaveType, year);
        if (quota == null) {
            return; // 未配置配额的类型不限额
        }
        if (quota.getRemainDays().compareTo(days) < 0) {
            throw exception(LEAVE_QUOTA_NOT_ENOUGH);
        }
        // 原子扣减（SQL 条件 used_days+delta<=quota_days），检查影响行数：并发下扣减失败即报余额不足，
        // 避免"审批通过但未扣余额"
        int rows = quotaMapper.adjustUsedDays(quota.getId(), days, quota.getQuotaDays());
        if (rows == 0) {
            throw exception(LEAVE_QUOTA_NOT_ENOUGH);
        }
    }

    @Override
    public void refundUsedDays(Long employeeId, String leaveType, String year, java.math.BigDecimal days) {
        if (days == null || days.signum() <= 0) throw exception(LEAVE_DAYS_INVALID);
        LeaveQuotaDO quota = quotaMapper.selectUnique(employeeId, leaveType, year);
        if (quota != null) {
            if (quotaMapper.adjustUsedDays(quota.getId(), days.negate(), null) == 0) throw exception(LEAVE_QUOTA_NOT_ENOUGH);
        }
    }


    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void deleteLeaveQuota(Long id) {
        var quota = quotaMapper.selectForUpdate(id);
        if (quota == null) throw exception(LEAVEQUOTA_NOT_EXISTS);
        if (quota.getUsedDays().signum() != 0) throw exception(QUOTA_USED_OVER);
        quotaMapper.deleteById(id);
    }

    private void validateLeaveQuotaExists(Long id) {
        if (quotaMapper.selectById(id) == null) {
            throw exception(LEAVEQUOTA_NOT_EXISTS);
        }
    }

    @Override
    public LeaveQuotaDO getLeaveQuota(Long id) {
        return quotaMapper.selectById(id);
    }

    @Override
    public PageResult<LeaveQuotaDO> getLeaveQuotaPage(LeaveQuotaPageReqVO pageReqVO) {
        return quotaMapper.selectPage(pageReqVO);
    }
}
