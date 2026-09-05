package com.enterprise.module.biz.service.correction;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.enterprise.module.biz.controller.admin.correction.vo.correction.AttendanceCorrectionPageReqVO;
import com.enterprise.module.biz.controller.admin.correction.vo.correction.AttendanceCorrectionSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.attendance.AttendanceDO;
import com.enterprise.module.biz.dal.dataobject.correction.AttendanceCorrectionDO;
import com.enterprise.module.biz.dal.mysql.attendance.AttendanceMapper;
import com.enterprise.module.biz.dal.mysql.correction.AttendanceCorrectionMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;

import static com.enterprise.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.enterprise.module.biz.enums.ErrorCodeConstants.*;

/**
 * 补卡申请 Service 实现类
 *
 * @author 企业管理平台
 */
@Service
@Validated
public class AttendanceCorrectionServiceImpl implements AttendanceCorrectionService {

    /** 上班时间，晚于此打卡记迟到（与 PortalController 保持一致） */
    private static final String WORK_START = "09:00";
    /** 下班时间，早于此打卡记早退 */
    private static final String WORK_END = "18:00";

    @Resource
    private AttendanceCorrectionMapper correctionMapper;
    @Resource
    private AttendanceMapper attendanceMapper;

    @Override
    public Long createCorrection(AttendanceCorrectionSaveReqVO createReqVO) {
        AttendanceCorrectionDO correction = BeanUtils.toBean(createReqVO, AttendanceCorrectionDO.class);
        correction.setStatus("0"); // 强制初始状态，防止客户端篡改
        correctionMapper.insert(correction);
        return correction.getId();
    }

    @Override
    public void deleteCorrection(Long id) {
        validateCorrectionExists(id);
        correctionMapper.deleteById(id);
    }

    private void validateCorrectionExists(Long id) {
        if (correctionMapper.selectById(id) == null) {
            throw exception(CORRECTION_NOT_EXISTS);
        }
    }

    @Override
    public AttendanceCorrectionDO getCorrection(Long id) {
        return correctionMapper.selectById(id);
    }

    @Override
    public PageResult<AttendanceCorrectionDO> getCorrectionPage(AttendanceCorrectionPageReqVO pageReqVO) {
        return correctionMapper.selectPage(pageReqVO);
    }

    @Override
    public PageResult<AttendanceCorrectionDO> getCorrectionPageSelf(AttendanceCorrectionPageReqVO pageReqVO, Long userId) {
        return correctionMapper.selectPage(pageReqVO, new LambdaQueryWrapperX<AttendanceCorrectionDO>()
                .eq(AttendanceCorrectionDO::getCreator, String.valueOf(userId))
                .orderByDesc(AttendanceCorrectionDO::getId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int auditCorrection(Long id, String status, String auditRemark, Long auditorUserId) {
        if (!"1".equals(status) && !"2".equals(status)) {
            throw exception(CORRECTION_AUDIT_STATUS_INVALID);
        }
        AttendanceCorrectionDO update = AttendanceCorrectionDO.builder()
                .id(id).status(status).auditRemark(auditRemark)
                .auditBy(String.valueOf(auditorUserId))
                .auditTime(LocalDate.now().toString())
                .build();
        int rows = correctionMapper.auditCorrection(update);
        if (rows == 0) {
            throw exception(CORRECTION_ALREADY_AUDITED);
        }
        // 审批通过：自动回写考勤记录
        if ("1".equals(status)) {
            applyCorrection(correctionMapper.selectById(id));
        }
        return rows;
    }

    /**
     * 将补卡时间写回考勤记录，并按打卡规则重算状态：
     * 上班打卡晚于 09:00 记迟到；否则下班早于 18:00 记早退；其余正常。
     */
    private void applyCorrection(AttendanceCorrectionDO correction) {
        if (correction == null) {
            return;
        }
        AttendanceDO attendance = attendanceMapper.selectByEmpAndDate(
                correction.getEmpName(), correction.getWorkDate());
        if (attendance == null) {
            attendance = AttendanceDO.builder()
                    .empName(correction.getEmpName())
                    .workDate(correction.getWorkDate())
                    .build();
        }
        if ("1".equals(correction.getCorrectType())) {
            attendance.setCheckIn(correction.getCorrectTime());
        } else {
            attendance.setCheckOut(correction.getCorrectTime());
            // 补的下班卡晚于 18:00 同样累计加班
            if (correction.getCorrectTime().compareTo(WORK_END) > 0) {
                attendance.setOvertimeMinutes(otMinutes(correction.getCorrectTime()));
            }
        }
        attendance.setStatus(recomputeStatus(attendance));
        if (attendance.getId() == null) {
            attendanceMapper.insert(attendance);
        } else {
            attendanceMapper.updateById(attendance);
        }
    }

    private Integer otMinutes(String time) {
        int nowMin = Integer.parseInt(time.substring(0, 2)) * 60 + Integer.parseInt(time.substring(3));
        int endMin = Integer.parseInt(WORK_END.substring(0, 2)) * 60 + Integer.parseInt(WORK_END.substring(3));
        return Math.max(0, nowMin - endMin);
    }

    private String recomputeStatus(AttendanceDO attendance) {
        if (attendance.getCheckIn() != null && attendance.getCheckIn().compareTo(WORK_START) > 0) {
            return "1"; // 迟到
        }
        if (attendance.getCheckOut() != null && attendance.getCheckOut().compareTo(WORK_END) < 0) {
            return "2"; // 早退
        }
        return "0"; // 正常
    }
}
