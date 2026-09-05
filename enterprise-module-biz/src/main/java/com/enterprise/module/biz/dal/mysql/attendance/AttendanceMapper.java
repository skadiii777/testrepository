package com.enterprise.module.biz.dal.mysql.attendance;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.mybatis.core.mapper.BaseMapperX;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.enterprise.module.biz.controller.admin.attendance.vo.attendance.AttendancePageReqVO;
import com.enterprise.module.biz.dal.dataobject.attendance.AttendanceDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AttendanceMapper extends BaseMapperX<AttendanceDO> {

    /**
     * 分页查询
     */
    default PageResult<AttendanceDO> selectPage(AttendancePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AttendanceDO>()
                .likeIfPresent(AttendanceDO::getEmpName, reqVO.getEmpName())
                .eqIfPresent(AttendanceDO::getWorkDate, reqVO.getWorkDate())
                .eqIfPresent(AttendanceDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(AttendanceDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(AttendanceDO::getId));
    }

    /**
     * 查询员工某天的考勤记录
     */
    /**
     * 员工某月加班总分钟
     */
    @Select("SELECT IFNULL(SUM(overtime_minutes), 0) FROM biz_attendance "
            + "WHERE deleted = 0 AND emp_name = #{empName} AND work_date LIKE CONCAT(#{month}, '%')")
    Integer selectMonthOvertimeMinutes(@Param("empName") String empName, @Param("month") String month);

    /**
     * 考勤月报：按员工汇总出勤天数/迟到/早退/缺勤/加班分钟
     */
    @Select("SELECT emp_name AS empName, COUNT(*) AS attendDays, "
            + "SUM(CASE WHEN status = '1' THEN 1 ELSE 0 END) AS lateCount, "
            + "SUM(CASE WHEN status = '2' THEN 1 ELSE 0 END) AS earlyCount, "
            + "SUM(CASE WHEN status = '3' THEN 1 ELSE 0 END) AS absentCount, "
            + "IFNULL(SUM(overtime_minutes), 0) AS overtimeMinutes "
            + "FROM biz_attendance WHERE deleted = 0 AND work_date LIKE CONCAT(#{month}, '%') "
            + "GROUP BY emp_name ORDER BY emp_name")
    java.util.List<java.util.Map<String, Object>> selectMonthlySummary(@Param("month") String month);

    default AttendanceDO selectByEmpAndDate(String empName, String workDate) {
        return selectOne(new LambdaQueryWrapperX<AttendanceDO>()
                .eq(AttendanceDO::getEmpName, empName)
                .eq(AttendanceDO::getWorkDate, workDate)
                .last("LIMIT 1"));
    }
}