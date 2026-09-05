package com.enterprise.module.biz.enums;

import com.enterprise.framework.common.exception.ErrorCode;

/**
 * Biz 模块错误码区间 [1_050_000_000, 1_060_000_000)
 */
public interface ErrorCodeConstants {

    // ========== 实体不存在 1_050_000_001 ==========
    ErrorCode CUSTOMER_NOT_EXISTS = new ErrorCode(1_050_000_001, "客户不存在");
    ErrorCode PRODUCT_NOT_EXISTS = new ErrorCode(1_050_000_002, "产品不存在");
    ErrorCode CONTRACT_NOT_EXISTS = new ErrorCode(1_050_000_003, "合同不存在");
    ErrorCode SUPPLIER_NOT_EXISTS = new ErrorCode(1_050_000_004, "供应商不存在");
    ErrorCode PURCHASE_NOT_EXISTS = new ErrorCode(1_050_000_005, "采购单不存在");
    ErrorCode SALES_NOT_EXISTS = new ErrorCode(1_050_000_006, "销售单不存在");
    ErrorCode STOCK_NOT_EXISTS = new ErrorCode(1_050_000_007, "库存记录不存在");
    ErrorCode EMPLOYEE_NOT_EXISTS = new ErrorCode(1_050_000_008, "员工不存在");
    ErrorCode ATTENDANCE_NOT_EXISTS = new ErrorCode(1_050_000_009, "考勤记录不存在");
    ErrorCode LEAVE_NOT_EXISTS = new ErrorCode(1_050_000_010, "请假单不存在");
    ErrorCode QUOTA_NOT_EXISTS = new ErrorCode(1_050_000_011, "假期配额不存在");
    ErrorCode LEAVEQUOTA_NOT_EXISTS = QUOTA_NOT_EXISTS; // 生成器命名别名
    ErrorCode REPORT_NOT_EXISTS = new ErrorCode(1_050_000_012, "汇报不存在");
    ErrorCode EXPENSE_NOT_EXISTS = new ErrorCode(1_050_000_013, "报销单不存在");

    // ========== 业务规则 1_050_001_XXX ==========
    ErrorCode STOCK_NOT_ENOUGH = new ErrorCode(1_050_001_001, "库存不足，无法出库，请先采购入库");
    ErrorCode LEAVE_QUOTA_NOT_ENOUGH = new ErrorCode(1_050_001_002, "假期余额不足");
    ErrorCode QUOTA_DUPLICATE = new ErrorCode(1_050_001_003, "该员工此假期类型在该年度已存在配额");
    ErrorCode QUOTA_USED_OVER = new ErrorCode(1_050_001_004, "已用天数不能大于配额天数");
    ErrorCode EXPENSE_ALREADY_AUDITED = new ErrorCode(1_050_001_005, "该报销已审批，不能重复操作");
    ErrorCode EXPENSE_AUDIT_STATUS_INVALID = new ErrorCode(1_050_001_006, "审批状态不合法");
    ErrorCode PUNCH_DUPLICATE = new ErrorCode(1_050_001_007, "今日已完成该打卡，无需重复操作");
    ErrorCode PUNCH_TYPE_INVALID = new ErrorCode(1_050_001_008, "打卡类型不合法");
    ErrorCode LEAVE_CANCEL_ONLY_APPROVED = new ErrorCode(1_050_001_009, "仅已通过的请假可以销假");
    ErrorCode PORTAL_NOT_OWNER = new ErrorCode(1_050_001_010, "仅能操作本人提交的记录");
    ErrorCode EXPENSE_AMOUNT_INVALID = new ErrorCode(1_050_001_011, "报销金额必须大于0");
    ErrorCode CORRECTION_NOT_EXISTS = new ErrorCode(1_050_001_012, "补卡申请不存在");
    ErrorCode CORRECTION_ALREADY_AUDITED = new ErrorCode(1_050_001_013, "该补卡已审批，不能重复操作");
    ErrorCode CORRECTION_AUDIT_STATUS_INVALID = new ErrorCode(1_050_001_014, "审批状态不合法");
    ErrorCode ORDER_STATUS_TRANSITION_INVALID = new ErrorCode(1_050_001_015, "单据状态流转不合法");
    ErrorCode FOLLOWUP_NOT_EXISTS = new ErrorCode(1_050_001_016, "跟进记录不存在");
}