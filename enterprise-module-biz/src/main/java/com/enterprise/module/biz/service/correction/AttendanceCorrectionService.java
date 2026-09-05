package com.enterprise.module.biz.service.correction;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.module.biz.controller.admin.correction.vo.correction.AttendanceCorrectionPageReqVO;
import com.enterprise.module.biz.controller.admin.correction.vo.correction.AttendanceCorrectionSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.correction.AttendanceCorrectionDO;

/**
 * 补卡申请 Service 接口
 *
 * @author 企业管理平台
 */
public interface AttendanceCorrectionService {

    /**
     * 创建补卡申请（状态强制为待审批）
     */
    Long createCorrection(AttendanceCorrectionSaveReqVO createReqVO);

    /**
     * 删除补卡申请
     */
    void deleteCorrection(Long id);

    /**
     * 获得补卡申请
     */
    AttendanceCorrectionDO getCorrection(Long id);

    /**
     * 获得补卡申请分页
     */
    PageResult<AttendanceCorrectionDO> getCorrectionPage(AttendanceCorrectionPageReqVO pageReqVO);

    /**
     * 仅本人分页（员工工作台）
     */
    PageResult<AttendanceCorrectionDO> getCorrectionPageSelf(AttendanceCorrectionPageReqVO pageReqVO, Long userId);

    /**
     * 审批补卡（1=通过 2=驳回）
     * 通过时自动回写考勤记录：更新打卡时间并重算考勤状态
     *
     * @return 0=记录不存在或已审批；1=成功
     */
    int auditCorrection(Long id, String status, String auditRemark, Long auditorUserId);
}
