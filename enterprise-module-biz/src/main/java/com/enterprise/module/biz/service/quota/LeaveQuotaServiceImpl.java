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



    @Override
    public Long createLeaveQuota(LeaveQuotaSaveReqVO createReqVO) {
        // 唯一键防重：员工+类型+年份
        if (quotaMapper.selectUnique(createReqVO.getEmpName(), createReqVO.getLeaveType(), createReqVO.getYear()) != null) {
            throw exception(QUOTA_DUPLICATE);
        }
        LeaveQuotaDO quota = BeanUtils.toBean(createReqVO, LeaveQuotaDO.class);
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
    public void updateLeaveQuota(LeaveQuotaSaveReqVO updateReqVO) {
        validateLeaveQuotaExists(updateReqVO.getId());
        LeaveQuotaDO exist = quotaMapper.selectUnique(updateReqVO.getEmpName(), updateReqVO.getLeaveType(), updateReqVO.getYear());
        if (exist != null && !exist.getId().equals(updateReqVO.getId())) {
            throw exception(QUOTA_DUPLICATE);
        }
        LeaveQuotaDO updateObj = BeanUtils.toBean(updateReqVO, LeaveQuotaDO.class);
        if (updateObj.getUsedDays() != null && updateObj.getQuotaDays() != null
                && updateObj.getUsedDays().compareTo(updateObj.getQuotaDays()) > 0) {
            throw exception(QUOTA_USED_OVER);
        }
        quotaMapper.updateById(updateObj);
    }

    @Override
    public java.math.BigDecimal findRemainDays(String empName, String leaveType, String year) {
        LeaveQuotaDO quota = quotaMapper.selectUnique(empName, leaveType, year);
        // 无配额记录 = 该类型未配置限额
        return quota == null ? new java.math.BigDecimal("99999") : quota.getRemainDays();
    }

    @Override
    public void deductUsedDays(String empName, String leaveType, String year, java.math.BigDecimal days) {
        LeaveQuotaDO quota = quotaMapper.selectUnique(empName, leaveType, year);
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
    public void refundUsedDays(String empName, String leaveType, String year, java.math.BigDecimal days) {
        LeaveQuotaDO quota = quotaMapper.selectUnique(empName, leaveType, year);
        if (quota != null) {
            quotaMapper.adjustUsedDays(quota.getId(), days.negate(), null);
        }
    }


    @Override
    public void deleteLeaveQuota(Long id) {
        validateLeaveQuotaExists(id);
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