package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.Attendance;

/**
 * 考勤 数据层
 * 
 * @author biz
 */
public interface AttendanceMapper
{
    public Attendance selectAttendanceById(Long id);

    public List<Attendance> selectAttendanceList(Attendance attendance);

    public int insertAttendance(Attendance attendance);

    public int updateAttendance(Attendance attendance);

    public int deleteAttendanceByIds(String[] ids);
}
