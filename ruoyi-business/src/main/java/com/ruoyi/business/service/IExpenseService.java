package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.Expense;

/**
 * 费用报销 服务层
 *
 * @author biz
 */
public interface IExpenseService
{
    public Expense selectExpenseById(Long id);

    public List<Expense> selectExpenseList(Expense expense);

    public int insertExpense(Expense expense);

    /**
     * 审批（1通过 2驳回），仅待审批可流转
     *
     * @return 0=记录不存在或已审批；1=成功
     */
    public int auditExpense(Long id, String status, String auditRemark, String auditBy);

    public int deleteExpenseByIds(String ids);
}
