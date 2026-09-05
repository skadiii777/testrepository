package com.enterprise.module.biz.dal.mysql.expense;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.mybatis.core.mapper.BaseMapperX;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.enterprise.module.biz.controller.admin.expense.vo.expense.ExpensePageReqVO;
import com.enterprise.module.biz.dal.dataobject.expense.ExpenseDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
@Mapper
public interface ExpenseMapper extends BaseMapperX<ExpenseDO> {

    /**
     * 分页查询
     */
    default PageResult<ExpenseDO> selectPage(ExpensePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ExpenseDO>()
                .likeIfPresent(ExpenseDO::getEmpName, reqVO.getEmpName())
                .eqIfPresent(ExpenseDO::getCategory, reqVO.getCategory())
                .eqIfPresent(ExpenseDO::getExpenseDate, reqVO.getExpenseDate())
                .eqIfPresent(ExpenseDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ExpenseDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ExpenseDO::getId));
    }

    /**
     * 审批报销（SQL 层防重复审批：仅待审批可流转）
     */
    @Update("UPDATE biz_expense SET status = #{status}, audit_remark = #{auditRemark}, "
            + "audit_by = #{auditBy}, audit_time = #{auditTime}, update_time = NOW() "
            + "WHERE id = #{id} AND status = '0' AND deleted = 0")
    int auditExpense(ExpenseDO expense);
}