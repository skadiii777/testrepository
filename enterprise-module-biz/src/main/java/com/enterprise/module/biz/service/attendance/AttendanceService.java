package com.enterprise.module.biz.service.attendance;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.module.biz.controller.admin.attendance.vo.attendance.AttendancePageReqVO;
import com.enterprise.module.biz.controller.admin.attendance.vo.attendance.AttendanceSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.attendance.AttendanceDO;

/**
 * 考勤 Service 接口
 *
 * @author 企业管理平台
 */
public interface AttendanceService {

    /**
     * 创建考勤
     */
    Long createAttendance(AttendanceSaveReqVO createReqVO);

    /**
     * 更新考勤
     */
    void updateAttendance(AttendanceSaveReqVO updateReqVO);

    /**
     * 删除考勤
     */
    void deleteAttendance(Long id);

    /**
     * 获得考勤
     */
    AttendanceDO getAttendance(Long id);

    /**
     * 获得考勤分页
     */
    PageResult<AttendanceDO> getAttendancePage(AttendancePageReqVO pageReqVO);

    /**
     * 查询员工某天的考勤记录
     */
    AttendanceDO getTodayAttendance(String empName, String workDate);

    /**
     * 查询员工某月加班总分钟
     */
    Integer getMonthOvertimeMinutes(String empName, String month);

    /**
     * 考勤月报：按员工汇总出勤/迟到/早退/缺勤/加班
     */
    java.util.List<java.util.Map<String, Object>> getMonthlySummary(String month);
}