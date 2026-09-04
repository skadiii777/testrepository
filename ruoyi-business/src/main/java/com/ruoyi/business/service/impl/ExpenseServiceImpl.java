package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.business.domain.Expense;
import com.ruoyi.business.mapper.ExpenseMapper;
import com.ruoyi.business.service.IExpenseService;

/**
 * 费用报销 服务层实现
 *
 * @author biz
 */
@Service
public class ExpenseServiceImpl implements IExpenseService
{
    @Autowired
    private ExpenseMapper expenseMapper;

    @Override
    public Expense selectExpenseById(Long id)
    {
        return expenseMapper.selectExpenseById(id);
    }

    @Override
    public List<Expense> selectExpenseList(Expense expense)
    {
        return expenseMapper.selectExpenseList(expense);
    }

    @Override
    public int insertExpense(Expense expense)
    {
        return expenseMapper.insertExpense(expense);
    }

    @Override
    public int auditExpense(Long id, String status, String auditRemark, String auditBy)
    {
        if (!"1".equals(status) && !"2".equals(status))
        {
            throw new com.ruoyi.common.exception.ServiceException("审批状态不合法");
        }
        Expense update = new Expense();
        update.setId(id);
        update.setStatus(status);
        update.setAuditRemark(auditRemark);
        update.setAuditBy(auditBy);
        return expenseMapper.auditExpense(update);
    }

    @Override
    public int deleteExpenseByIds(String ids)
    {
        return expenseMapper.deleteExpenseByIds(Convert.toStrArray(ids));
    }
}
