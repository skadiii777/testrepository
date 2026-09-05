package com.enterprise.module.biz.service.expense;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.module.biz.controller.admin.expense.vo.expense.ExpensePageReqVO;
import com.enterprise.module.biz.controller.admin.expense.vo.expense.ExpenseSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.expense.ExpenseDO;
import com.enterprise.module.biz.dal.mysql.expense.ExpenseMapper;
import com.enterprise.module.biz.enums.ErrorCodeConstants;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;


import static com.enterprise.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.enterprise.module.biz.enums.ErrorCodeConstants.*;

/**
 * 费用报销 Service 实现类
 *
 * @author 企业管理平台
 */
@Service
@Validated
public class ExpenseServiceImpl implements ExpenseService {

    @Resource
    private ExpenseMapper expenseMapper;



    @Override
    public Long createExpense(ExpenseSaveReqVO createReqVO) {
        ExpenseDO expense = BeanUtils.toBean(createReqVO, ExpenseDO.class);
        expense.setStatus("0"); // 强制初始状态，防止客户端篡改
        expenseMapper.insert(expense);
        return expense.getId();
    }

    @Override
    public PageResult<ExpenseDO> getExpensePageSelf(ExpensePageReqVO pageReqVO, Long userId) {
        return expenseMapper.selectPage(pageReqVO, new LambdaQueryWrapperX<ExpenseDO>()
                .eq(ExpenseDO::getCreator, String.valueOf(userId))
                .orderByDesc(ExpenseDO::getId));
    }

    @Override
    public void auditExpense(Long id, String status, String auditRemark, Long auditorUserId) {
        if (!"1".equals(status) && !"2".equals(status)) {
            throw exception(EXPENSE_AUDIT_STATUS_INVALID);
        }
        ExpenseDO update = ExpenseDO.builder()
                .id(id).status(status).auditRemark(auditRemark)
                .auditBy(String.valueOf(auditorUserId))
                .auditTime(java.time.LocalDate.now().toString())
                .build();
        if (expenseMapper.auditExpense(update) == 0) {
            throw exception(EXPENSE_ALREADY_AUDITED);
        }
    }
    @Override
    public void updateExpense(ExpenseSaveReqVO updateReqVO) {
        validateExpenseExists(updateReqVO.getId());
        ExpenseDO updateObj = BeanUtils.toBean(updateReqVO, ExpenseDO.class);
        expenseMapper.updateById(updateObj);
    }


    @Override
    public void deleteExpense(Long id) {
        validateExpenseExists(id);
        expenseMapper.deleteById(id);
    }

    private void validateExpenseExists(Long id) {
        if (expenseMapper.selectById(id) == null) {
            throw exception(EXPENSE_NOT_EXISTS);
        }
    }

    @Override
    public ExpenseDO getExpense(Long id) {
        return expenseMapper.selectById(id);
    }

    @Override
    public PageResult<ExpenseDO> getExpensePage(ExpensePageReqVO pageReqVO) {
        return expenseMapper.selectPage(pageReqVO);
    }
}