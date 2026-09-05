package com.enterprise.module.biz.dal.mysql.correction;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.mybatis.core.mapper.BaseMapperX;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.enterprise.module.biz.controller.admin.correction.vo.correction.AttendanceCorrectionPageReqVO;
import com.enterprise.module.biz.dal.dataobject.correction.AttendanceCorrectionDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AttendanceCorrectionMapper extends BaseMapperX<AttendanceCorrectionDO> {

    /**
     * 分页查询
     */
    default PageResult<AttendanceCorrectionDO> selectPage(AttendanceCorrectionPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AttendanceCorrectionDO>()
                .likeIfPresent(AttendanceCorrectionDO::getEmpName, reqVO.getEmpName())
                .eqIfPresent(AttendanceCorrectionDO::getWorkDate, reqVO.getWorkDate())
                .eqIfPresent(AttendanceCorrectionDO::getCorrectType, reqVO.getCorrectType())
                .eqIfPresent(AttendanceCorrectionDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(AttendanceCorrectionDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(AttendanceCorrectionDO::getId));
    }

    /**
     * 审批补卡（SQL 层防重复审批：仅待审批可流转）
     */
    @Update("UPDATE biz_attendance_correction SET status = #{status}, audit_remark = #{auditRemark}, "
            + "audit_by = #{auditBy}, audit_time = #{auditTime}, update_time = NOW() "
            + "WHERE id = #{id} AND status = '0' AND deleted = 0")
    int auditCorrection(AttendanceCorrectionDO correction);
}
