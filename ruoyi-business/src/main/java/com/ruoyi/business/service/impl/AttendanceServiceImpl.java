package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.business.domain.Attendance;
import com.ruoyi.business.mapper.AttendanceMapper;
import com.ruoyi.business.service.IAttendanceService;

/**
 * 考勤 服务层实现
 * 
 * @author biz
 */
@Service
public class AttendanceServiceImpl implements IAttendanceService
{
    @Autowired
    private AttendanceMapper attendanceMapper;

    @Override
    public Attendance selectAttendanceById(Long id)
    {
        return attendanceMapper.selectAttendanceById(id);
    }

    @Override
    public List<Attendance> selectAttendanceList(Attendance attendance)
    {
        return attendanceMapper.selectAttendanceList(attendance);
    }

    @Override
    public int insertAttendance(Attendance attendance)
    {
        return attendanceMapper.insertAttendance(attendance);
    }

    @Override
    public int updateAttendance(Attendance attendance)
    {
        return attendanceMapper.updateAttendance(attendance);
    }

    @Override
    public int deleteAttendanceByIds(String ids)
    {
        return attendanceMapper.deleteAttendanceByIds(Convert.toStrArray(ids));
    }

}
