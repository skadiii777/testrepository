package com.enterprise.module.biz.dal.mysql.leave;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.mybatis.core.mapper.BaseMapperX;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.enterprise.module.biz.controller.admin.leave.vo.leave.LeavePageReqVO;
import com.enterprise.module.biz.dal.dataobject.leave.LeaveDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LeaveMapper extends BaseMapperX<LeaveDO> {

    /**
     * 分页查询
     */
    default PageResult<LeaveDO> selectPage(LeavePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<LeaveDO>()
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
}