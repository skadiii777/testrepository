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
    ErrorCode CLUE_NOT_EXISTS = new ErrorCode(1_050_000_014, "销售线索不存在");
    ErrorCode BUSINESS_NOT_EXISTS = new ErrorCode(1_050_000_015, "商机不存在");
    ErrorCode CONTACT_NOT_EXISTS = new ErrorCode(1_050_000_016, "客户联系人不存在");
    ErrorCode ANNOUNCEMENT_NOT_EXISTS = new ErrorCode(1_050_000_017, "公司公告不存在");

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
    ErrorCode PAYMENT_NOT_EXISTS = new ErrorCode(1_050_001_017, "收付款流水不存在");
    ErrorCode PAYMENT_BIZ_TYPE_INVALID = new ErrorCode(1_050_001_018, "关联单据类型不合法");
    ErrorCode PAYMENT_TYPE_BIZ_MISMATCH = new ErrorCode(1_050_001_019, "收付类型与单据类型不匹配（收款对应销售单，付款对应采购单）");
    ErrorCode PAYMENT_ORDER_NOT_EXISTS = new ErrorCode(1_050_001_020, "关联单据不存在");
    ErrorCode PAYMENT_ORDER_NOT_COMPLETED = new ErrorCode(1_050_001_021, "仅已完成（状态2）的单据可以登记收付款");
    ErrorCode PAYMENT_AMOUNT_EXCEED = new ErrorCode(1_050_001_022, "累计金额超出单据总额：已收付 {}，单据总额 {}");
    ErrorCode STOCKCHECK_NOT_EXISTS = new ErrorCode(1_050_001_023, "盘点单不存在");
    ErrorCode STOCKCHECK_ALREADY_CONFIRMED = new ErrorCode(1_050_001_024, "该盘点单已确认，不能重复操作");
    ErrorCode STOCKCHECK_CONFIRMED_CANNOT_DELETE = new ErrorCode(1_050_001_025, "已确认的盘点单不能删除");
    ErrorCode STOCKCHECK_CONFIRM_FAILED = new ErrorCode(1_050_001_026, "盘点确认失败，库存调整未生效");
    ErrorCode RETURN_NOT_EXISTS = new ErrorCode(1_050_001_027, "退货单不存在");
    ErrorCode RETURN_TYPE_INVALID = new ErrorCode(1_050_001_028, "退货类型不合法");
    ErrorCode RETURN_ORDER_NOT_EXISTS = new ErrorCode(1_050_001_029, "关联单据不存在");
    ErrorCode RETURN_ORDER_NOT_COMPLETED = new ErrorCode(1_050_001_030, "仅已完成（状态2）的单据可以退货");
    ErrorCode RETURN_QTY_EXCEED = new ErrorCode(1_050_001_031, "累计退货数量超出原单数量：已退 {}，原单数量 {}");
    ErrorCode RETURN_STATUS_INVALID = new ErrorCode(1_050_001_032, "退货单状态流转不合法");
    ErrorCode RETURN_QTY_INVALID = new ErrorCode(1_050_001_033, "退货数量必须大于0");
    ErrorCode CLUE_ALREADY_CONVERTED = new ErrorCode(1_050_001_034, "该线索已转化，不能重复操作");
    ErrorCode CLUE_STATUS_INVALID = new ErrorCode(1_050_001_035, "线索状态不合法（已转化/已无效为终态）");
    ErrorCode BUSINESS_STAGE_INVALID = new ErrorCode(1_050_001_036, "商机阶段不合法");
    ErrorCode BUSINESS_STAGE_TERMINAL = new ErrorCode(1_050_001_037, "商机已赢单/输单，不能再修改");
    ErrorCode BUSINESS_CUSTOMER_NOT_EXISTS = new ErrorCode(1_050_001_038, "关联客户不存在");
    ErrorCode CONTACT_CUSTOMER_NOT_EXISTS = new ErrorCode(1_050_001_039, "关联客户不存在");
    ErrorCode ANNOUNCEMENT_STATUS_INVALID = new ErrorCode(1_050_001_040, "公告状态不合法（仅 0=已发布 1=已下架）");
    ErrorCode BUSINESS_NOT_WIN = new ErrorCode(1_050_001_041, "仅已赢单的商机可以转为合同");
    ErrorCode PAYMENT_CONTRACT_NOT_EXISTS = new ErrorCode(1_050_001_042, "关联合同不存在");
    ErrorCode PAYMENT_TARGET_REQUIRED = new ErrorCode(1_050_001_043, "收付款必须关联单据或合同其一");
    ErrorCode ORDER_COMPLETED_LOCKED = new ErrorCode(1_050_001_044, "已完成单据不可修改或删除，请走退货/红冲流程");
    ErrorCode LEAVE_ALREADY_AUDITED = new ErrorCode(1_050_001_045, "该申请已审批，不能重复操作");
    ErrorCode LEAVE_DAYS_INVALID = new ErrorCode(1_050_001_054, "请假天数必须大于0");
    ErrorCode PAYMENT_DELETE_FORBIDDEN = new ErrorCode(1_050_001_046, "流水不可删除，请填写原因后冲销");
    ErrorCode PAYMENT_REVERSAL_INVALID = new ErrorCode(1_050_001_047, "流水不可冲销，可能已红冲或余额不足");
    ErrorCode PAYMENT_REASON_REQUIRED = new ErrorCode(1_050_001_048, "请填写200字以内的冲销原因");
    ErrorCode PAYMENT_REQUEST_CONFLICT = new ErrorCode(1_050_001_049, "请求编号已用于其他收付款内容");
    ErrorCode PAYMENT_AMOUNT_INVALID = new ErrorCode(1_050_001_050, "收付金额必须大于零");
    ErrorCode MASTER_REFERENCE_INVALID = new ErrorCode(1_050_001_051, "基础资料不存在或名称不唯一，请选择明确的编号");
    ErrorCode STOCK_DUPLICATE = new ErrorCode(1_050_001_052, "该产品在此仓库已有库存记录");
    ErrorCode STOCK_IDENTITY_LOCKED = new ErrorCode(1_050_001_053, "库存关联不可更换；数量调整请使用盘点");
}
