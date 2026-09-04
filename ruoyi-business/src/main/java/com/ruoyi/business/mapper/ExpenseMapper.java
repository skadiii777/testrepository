package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.Expense;

/**
 * 费用报销 数据层
 *
 * @author biz
 */
public interface ExpenseMapper
{
    public Expense selectExpenseById(Long id);

    public List<Expense> selectExpenseList(Expense expense);

    public int insertExpense(Expense expense);

    public int auditExpense(Expense expense);

    public int deleteExpenseByIds(String[] ids);
}
