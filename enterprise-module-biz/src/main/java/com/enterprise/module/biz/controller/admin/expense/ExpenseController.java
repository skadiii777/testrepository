package com.enterprise.module.biz.controller.admin.expense;

import com.enterprise.framework.common.pojo.CommonResult;
import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.framework.excel.core.util.ExcelUtils;
import com.enterprise.framework.apilog.core.annotation.ApiAccessLog;
import com.enterprise.module.biz.controller.admin.expense.vo.expense.*;
import com.enterprise.module.biz.dal.dataobject.expense.ExpenseDO;
import com.enterprise.module.biz.service.expense.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static com.enterprise.framework.common.pojo.CommonResult.success;
import static com.enterprise.framework.common.pojo.PageParam.PAGE_SIZE_NONE;
import static com.enterprise.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.enterprise.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 费用报销")
@RestController
@RequestMapping("/biz/expense")
@Validated
public class ExpenseController {

    @Resource
    private ExpenseService expenseService;

    @PostMapping("/audit")
    @Operation(summary = "审批报销")
    @PreAuthorize("@ss.hasPermission('biz:expense:audit')")
    public CommonResult<Boolean> auditExpense(@RequestParam("id") Long id,
                                              @RequestParam("status") String status,
                                              @RequestParam(value = "auditRemark", required = false) String auditRemark) {
        expenseService.auditExpense(id, status, auditRemark, getLoginUserId());
        return success(true);
    }
    @GetMapping("/get")
    @Operation(summary = "获得费用报销")
    @Parameter(name = "id", description = "主键", required = true)
    @PreAuthorize("@ss.hasPermission('biz:expense:query')")
    public CommonResult<ExpenseRespVO> getExpense(@RequestParam("id") Long id) {
        ExpenseDO expense = expenseService.getExpense(id);
        return success(BeanUtils.toBean(expense, ExpenseRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得费用报销分页")
    @PreAuthorize("@ss.hasPermission('biz:expense:query')")
    public CommonResult<PageResult<ExpenseRespVO>> getExpensePage(@Valid ExpensePageReqVO pageReqVO) {
        PageResult<ExpenseDO> pageResult = expenseService.getExpensePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ExpenseRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出费用报销 Excel")
    @PreAuthorize("@ss.hasPermission('biz:expense:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportExpenseExcel(@Valid ExpensePageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PAGE_SIZE_NONE);
        List<ExpenseRespVO> list = BeanUtils.toBean(expenseService.getExpensePage(pageReqVO).getList(), ExpenseRespVO.class);
        ExcelUtils.write(response, "费用报销.xls", "数据", ExpenseRespVO.class, list);
    }
}