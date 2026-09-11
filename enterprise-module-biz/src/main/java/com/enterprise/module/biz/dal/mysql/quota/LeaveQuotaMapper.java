package com.enterprise.module.biz.dal.mysql.quota;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.mybatis.core.mapper.BaseMapperX;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.enterprise.module.biz.controller.admin.quota.vo.quota.LeaveQuotaPageReqVO;
import com.enterprise.module.biz.dal.dataobject.quota.LeaveQuotaDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
@Mapper
public interface LeaveQuotaMapper extends BaseMapperX<LeaveQuotaDO> {
    default LeaveQuotaDO selectForUpdate(Long id) {
        return selectOne(new LambdaQueryWrapperX<LeaveQuotaDO>().eq(LeaveQuotaDO::getId,id).last("FOR UPDATE"));
    }


    /**
     * 分页查询
     */
    default PageResult<LeaveQuotaDO> selectPage(LeaveQuotaPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<LeaveQuotaDO>()
                .eqIfPresent(LeaveQuotaDO::getEmployeeId, reqVO.getEmployeeId())
                .likeIfPresent(LeaveQuotaDO::getEmpName, reqVO.getEmpName())
                .eqIfPresent(LeaveQuotaDO::getLeaveType, reqVO.getLeaveType())
                .eqIfPresent(LeaveQuotaDO::getYear, reqVO.getYear())
                .betweenIfPresent(LeaveQuotaDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(LeaveQuotaDO::getId));
    }

    /**
     * 按员工+类型+年份精确查询（唯一键）
     */
    default LeaveQuotaDO selectUnique(Long employeeId, String leaveType, String year) {
        return selectOne(new LambdaQueryWrapperX<LeaveQuotaDO>()
                .eq(LeaveQuotaDO::getEmployeeId, employeeId)
                .eq(LeaveQuotaDO::getLeaveType, leaveType)
                .eq(LeaveQuotaDO::getYear, year)
                .last("LIMIT 1"));
    }

    /**
     * 原子增减已用天数（delta 正数扣减/负数返还）
     * 带 quotaDays 时做余额兜底：used_days + delta <= quota_days
     */
    @Update("<script>UPDATE biz_leave_quota SET used_days = used_days + #{delta}, update_time = NOW() "
            + "WHERE id = #{id} AND deleted = 0 AND used_days + #{delta} >= 0 "
            + "<if test='quotaDays != null'>AND used_days + #{delta} &lt;= quota_days</if></script>")
    int adjustUsedDays(@Param("id") Long id, @Param("delta") java.math.BigDecimal delta,
                       @Param("quotaDays") java.math.BigDecimal quotaDays);
}
