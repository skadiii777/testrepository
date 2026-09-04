package com.ruoyi.business.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.ruoyi.business.domain.Leave;
import com.ruoyi.business.service.ILeaveService;
import com.ruoyi.business.service.IExpenseService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 审批中心：聚合各业务模块的待审批项
 * 第一版接入请假审批；费用报销、补卡等模块上线后自动扩展
 *
 * @author biz
 */
@Controller
@RequestMapping("/biz/approval")
public class BizApprovalController extends BaseController
{
    private String prefix = "biz/approval";

    @Autowired
    private ILeaveService leaveService;

    @Autowired
    private IExpenseService expenseService;

    @RequiresPermissions("biz:approval:view")
    @GetMapping()
    public String approval()
    {
        return prefix + "/approval";
    }

    /**
     * 待办数量（页面角标）
     */
    @RequiresPermissions("biz:approval:view")
    @PostMapping("/pending")
    @ResponseBody
    public AjaxResult pending()
    {
        Map<String, Object> data = new HashMap<>();
        Leave query = new Leave();
        query.setStatus("0");
        data.put("leaveCount", leaveService.selectLeaveList(query).size());
        com.ruoyi.business.domain.Expense expQuery = new com.ruoyi.business.domain.Expense();
        expQuery.setStatus("0");
        data.put("expenseCount", expenseService.selectExpenseList(expQuery).size());
        return success(data);
    }

    /**
     * 报销审批
     */
    @RequiresPermissions("biz:approval:view")
    @Log(title = "报销审批", businessType = com.ruoyi.common.enums.BusinessType.UPDATE)
    @PostMapping("/expenseAudit")
    @ResponseBody
    public AjaxResult expenseAudit(Long id, String status, String auditRemark)
    {
        return toAjax(expenseService.auditExpense(id, status, auditRemark, getLoginName()));
    }

    /**
     * 待审批报销列表
     */
    @RequiresPermissions("biz:approval:view")
    @PostMapping("/expenseList")
    @ResponseBody
    public TableDataInfo expenseList(com.ruoyi.business.domain.Expense expense)
    {
        startPage();
        expense.setStatus("0");
        List<com.ruoyi.business.domain.Expense> list = expenseService.selectExpenseList(expense);
        return getDataTable(list);
    }

    /**
     * 待审批请假列表
     */
    @RequiresPermissions("biz:approval:view")
    @PostMapping("/leaveList")
    @ResponseBody
    public TableDataInfo leaveList(Leave leave)
    {
        startPage();
        leave.setStatus("0");
        List<Leave> list = leaveService.selectLeaveList(leave);
        return getDataTable(list);
    }
}
