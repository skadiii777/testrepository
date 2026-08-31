package com.ruoyi.business.controller;

import java.util.List;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.business.domain.Contract;
import com.ruoyi.business.service.IContractService;

/**
 * 合同 信息操作处理
 * 
 * @author biz
 */
@Controller
@RequestMapping("/biz/contract")
public class ContractController extends BaseController
{
    private String prefix = "biz/contract";

    @Autowired
    private IContractService contractService;

    @RequiresPermissions("biz:contract:view")
    @GetMapping()
    public String contract()
    {
        return prefix + "/contract";
    }

    @RequiresPermissions("biz:contract:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(Contract contract)
    {
        startPage();
        List<Contract> list = contractService.selectContractList(contract);
        return getDataTable(list);
    }

    @RequiresPermissions("biz:contract:add")
    @GetMapping("/add")
    public String add()
    {
        return prefix + "/add";
    }

    @RequiresPermissions("biz:contract:add")
    @Log(title = "合同", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(Contract contract)
    {
        contract.setCreateBy(ShiroUtils.getLoginName());
        return toAjax(contractService.insertContract(contract));
    }

    @RequiresPermissions("biz:contract:edit")
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap mmap)
    {
        mmap.put("contract", contractService.selectContractById(id));
        return prefix + "/edit";
    }

    @RequiresPermissions("biz:contract:edit")
    @Log(title = "合同", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(Contract contract)
    {
        contract.setUpdateBy(ShiroUtils.getLoginName());
        return toAjax(contractService.updateContract(contract));
    }

    @RequiresPermissions("biz:contract:remove")
    @Log(title = "合同", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(contractService.deleteContractByIds(ids));
    }
}
