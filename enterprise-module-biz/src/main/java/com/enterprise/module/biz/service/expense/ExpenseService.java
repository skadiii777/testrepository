package com.enterprise.module.biz.service.expense;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.module.biz.controller.admin.expense.vo.expense.ExpensePageReqVO;
import com.enterprise.module.biz.controller.admin.expense.vo.expense.ExpenseSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.expense.ExpenseDO;

/**
 * 费用报销 Service 接口
 *
 * @author 企业管理平台
 */
public interface ExpenseService {

    /**
     * 创建费用报销
     */
    Long createExpense(ExpenseSaveReqVO createReqVO);

    /**
     * 更新费用报销
     */
    void updateExpense(ExpenseSaveReqVO updateReqVO);

    /**
     * 删除费用报销
     */
    void deleteExpense(Long id);

    /**
     * 获得费用报销
     */
    ExpenseDO getExpense(Long id);

    /**
     * 获得费用报销分页
     */
    PageResult<ExpenseDO> getExpensePage(ExpensePageReqVO pageReqVO);

    /**
     * 仅本人分页（员工工作台）
     */
    PageResult<ExpenseDO> getExpensePageSelf(ExpensePageReqVO pageReqVO, Long userId);

    /**
     * 审批报销（1=通过 2=驳回），SQL 层防重复审批
     */
    void auditExpense(Long id, String status, String auditRemark, Long auditorUserId);
}