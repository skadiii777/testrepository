package com.enterprise.module.biz.service.attendance;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.module.biz.controller.admin.attendance.vo.attendance.AttendancePageReqVO;
import com.enterprise.module.biz.controller.admin.attendance.vo.attendance.AttendanceSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.attendance.AttendanceDO;
import com.enterprise.module.biz.dal.mysql.attendance.AttendanceMapper;
import com.enterprise.module.biz.enums.ErrorCodeConstants;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;


import static com.enterprise.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.enterprise.module.biz.enums.ErrorCodeConstants.*;

/**
 * 考勤 Service 实现类
 *
 * @author 企业管理平台
 */
@Service
@Validated
public class AttendanceServiceImpl implements AttendanceService {

    @Resource
    private AttendanceMapper attendanceMapper;

    @Override
    public Long createAttendance(AttendanceSaveReqVO createReqVO) {
        AttendanceDO attendance = BeanUtils.toBean(createReqVO, AttendanceDO.class);
        attendanceMapper.insert(attendance);
        return attendance.getId();
    }


    @Override
    public AttendanceDO getTodayAttendance(String empName, String workDate) {
        return attendanceMapper.selectByEmpAndDate(empName, workDate);
    }

    @Override
    public Integer getMonthOvertimeMinutes(String empName, String month) {
        Integer minutes = attendanceMapper.selectMonthOvertimeMinutes(empName, month);
        return minutes == null ? 0 : minutes;
    }

    @Override
    public java.util.List<java.util.Map<String, Object>> getMonthlySummary(String month) {
        return attendanceMapper.selectMonthlySummary(month);
    }
    @Override
    public void updateAttendance(AttendanceSaveReqVO updateReqVO) {
        validateAttendanceExists(updateReqVO.getId());
        AttendanceDO updateObj = BeanUtils.toBean(updateReqVO, AttendanceDO.class);
        attendanceMapper.updateById(updateObj);
    }


    @Override
    public void deleteAttendance(Long id) {
        validateAttendanceExists(id);
        attendanceMapper.deleteById(id);
    }

    private void validateAttendanceExists(Long id) {
        if (attendanceMapper.selectById(id) == null) {
            throw exception(ATTENDANCE_NOT_EXISTS);
        }
    }

    @Override
    public AttendanceDO getAttendance(Long id) {
        return attendanceMapper.selectById(id);
    }

    @Override
    public PageResult<AttendanceDO> getAttendancePage(AttendancePageReqVO pageReqVO) {
        return attendanceMapper.selectPage(pageReqVO);
    }
}