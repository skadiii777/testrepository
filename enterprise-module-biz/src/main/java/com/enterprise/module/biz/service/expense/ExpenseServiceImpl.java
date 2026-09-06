package com.enterprise.module.biz.service.expense;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.module.biz.controller.admin.expense.vo.expense.ExpensePageReqVO;
import com.enterprise.module.biz.controller.admin.expense.vo.expense.ExpenseSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.expense.ExpenseDO;
import com.enterprise.module.biz.dal.mysql.expense.ExpenseMapper;
import com.enterprise.module.bpm.api.task.BpmProcessInstanceApi;
import com.enterprise.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static com.enterprise.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.enterprise.module.biz.enums.ErrorCodeConstants.*;

/**
 * 费用报销 Service 实现类
 *
 * 发起报销时自动接入 BPM 工作流；未部署流程模型时降级为本地审批模式
 *
 * @author 企业管理平台
 */
@Slf4j
@Service
@Validated
public class ExpenseServiceImpl implements ExpenseService {

    /** 报销流程定义 KEY（BPM 模型部署后生效） */
    public static final String PROCESS_KEY = "biz_expense";

    @Resource
    private ExpenseMapper expenseMapper;

    @Resource
    private BpmProcessInstanceApi processInstanceApi;

    @Override
    public void updateExpense(ExpenseSaveReqVO updateReqVO) {
        validateExpenseExists(updateReqVO.getId());
        ExpenseDO updateObj = BeanUtils.toBean(updateReqVO, ExpenseDO.class);
        updateObj.setStatus(null); // 状态只能通过审批变更
        updateObj.setProcessInstanceId(null);
        expenseMapper.updateById(updateObj);
    }

    @Override
    public Long createExpense(ExpenseSaveReqVO createReqVO) {
        ExpenseDO expense = BeanUtils.toBean(createReqVO, ExpenseDO.class);
        expense.setStatus("0"); // 强制初始状态，防止客户端篡改
        int rows = expenseMapper.insert(expense);
        if (rows > 0) {
            // 尝试发起 BPM 流程；未部署时降级为本地审批模式
            try {
                String processInstanceId = processInstanceApi.createProcessInstance(
                        Long.valueOf(expense.getCreator()),
                        new BpmProcessInstanceCreateReqDTO().setProcessDefinitionKey(PROCESS_KEY)
                                .setBusinessKey(String.valueOf(expense.getId())));
                expenseMapper.updateById(new ExpenseDO().setId(expense.getId())
                        .setProcessInstanceId(processInstanceId));
            } catch (Exception e) {
                log.warn("[insertExpense][BPM 流程未部署或发起失败，降级为本地审批] expenseId({}) 原因: {}",
                        expense.getId(), e.getMessage());
            }
        }
        return expense.getId();
    }

    @Override
    public void updateExpenseStatusFromBpm(Long id, Integer status) {
        // BPM 状态映射：2=通过 -> 表 1；3=驳回 -> 表 2
        ExpenseDO update = ExpenseDO.builder()
                .id(id)
                .status(status == null ? null : String.valueOf(status - 1))
                .build();
        expenseMapper.auditExpense(update);
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
    public PageResult<ExpenseDO> getExpensePageSelf(ExpensePageReqVO pageReqVO, Long userId) {
        return expenseMapper.selectPage(pageReqVO, new LambdaQueryWrapperX<ExpenseDO>()
                .eq(ExpenseDO::getCreator, String.valueOf(userId))
                .orderByDesc(ExpenseDO::getId));
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
