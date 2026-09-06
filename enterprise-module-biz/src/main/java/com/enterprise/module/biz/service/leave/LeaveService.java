package com.enterprise.module.biz.service.leave;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.module.biz.controller.admin.leave.vo.leave.LeavePageReqVO;
import com.enterprise.module.biz.controller.admin.leave.vo.leave.LeaveSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.leave.LeaveDO;

/**
 * 请假 Service 接口
 *
 * @author 企业管理平台
 */
public interface LeaveService {

    /**
     * 创建请假
     */
    Long createLeave(LeaveSaveReqVO createReqVO);

    /**
     * 更新请假
     */
    void updateLeave(LeaveSaveReqVO updateReqVO);

    /**
     * 删除请假
     */
    void deleteLeave(Long id);

    /**
     * 获得请假
     */
    LeaveDO getLeave(Long id);

    /**
     * 获得请假分页
     */
    PageResult<LeaveDO> getLeavePage(LeavePageReqVO pageReqVO);

    /**
     * 仅本人分页（员工工作台）
     */
    PageResult<LeaveDO> getLeavePageSelf(LeavePageReqVO pageReqVO, Long userId);

    /**
     * BPM 流程结束回调：status=2 通过（扣余额） / 3 驳回
     */
    void updateLeaveStatusFromBpm(Long id, Integer status);

    /**
     * 审批请假（status: 1=通过 2=驳回），通过时扣减假期余额
     */
    void auditLeave(Long id, String status, String auditRemark);

    /**
     * 销假（已通过 -> 已销假），返还假期余额
     */
    void cancelLeave(Long id, Long loginUserId);
}