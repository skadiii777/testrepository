package com.enterprise.module.biz.dal.mysql.leave;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.mybatis.core.mapper.BaseMapperX;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.enterprise.module.biz.controller.admin.leave.vo.leave.LeavePageReqVO;
import com.enterprise.module.biz.dal.dataobject.leave.LeaveDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LeaveMapper extends BaseMapperX<LeaveDO> {
    default LeaveDO selectForUpdate(Long id) {
        return selectOne(new LambdaQueryWrapperX<LeaveDO>().eq(LeaveDO::getId,id).last("FOR UPDATE"));
    }

    /**
     * 分页查询
     */
    default PageResult<LeaveDO> selectPage(LeavePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<LeaveDO>()
                .eqIfPresent(LeaveDO::getEmployeeId, reqVO.getEmployeeId())
                .likeIfPresent(LeaveDO::getEmpName, reqVO.getEmpName())
                .eqIfPresent(LeaveDO::getLeaveType, reqVO.getLeaveType())
                .eqIfPresent(LeaveDO::getStartDate, reqVO.getStartDate())
                .eqIfPresent(LeaveDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(LeaveDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(LeaveDO::getId));
    }

    /**
     * 各审批状态数量统计
     */
    default java.util.List<java.util.Map<String, Object>> selectStatusCount() {
        return selectMaps(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<LeaveDO>()
                .select("status", "count(*) AS cnt").groupBy("status"));
    }

    /**
     * 审批 CAS：仅待审批(0)可流转（请假用）
     */
    default int updateStatusCas(Long id, String toStatus, String auditRemark) {
        return update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<LeaveDO>()
                .eq(LeaveDO::getId, id)
                .eq(LeaveDO::getStatus, "0")
                .set(LeaveDO::getStatus, toStatus)
                .set(LeaveDO::getRemark, auditRemark));
    }

    /**
     * 销假 CAS：仅已通过(1)可流转到已销假(3)
     */
    default int updateStatusByCas(Long id, String fromStatus, String toStatus, String auditRemark) {
        return update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<LeaveDO>()
                .eq(LeaveDO::getId, id)
                .eq(LeaveDO::getStatus, fromStatus)
                .set(LeaveDO::getStatus, toStatus)
                .set(LeaveDO::getRemark, auditRemark));
    }
}
