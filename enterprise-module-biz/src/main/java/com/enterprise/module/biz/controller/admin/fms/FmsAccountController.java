package com.enterprise.module.biz.controller.admin.fms;

import com.enterprise.framework.common.pojo.CommonResult;
import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.module.biz.controller.admin.fms.vo.fms.*;
import com.enterprise.module.biz.service.fms.FmsAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.enterprise.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 会计科目")
@RestController
@RequestMapping("/biz/fms/account")
@Validated
public class FmsAccountController {

    @Resource
    private FmsAccountService accountService;

    @PostMapping("/create")
    @Operation(summary = "创建科目")
    @PreAuthorize("@ss.hasPermission('biz:fms:account:create')")
    public CommonResult<Long> createAccount(@Valid @RequestBody FmsAccountSaveReqVO reqVO) {
        return success(accountService.createAccount(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改科目")
    @PreAuthorize("@ss.hasPermission('biz:fms:account:update')")
    public CommonResult<Boolean> updateAccount(@Valid @RequestBody FmsAccountSaveReqVO reqVO) {
        accountService.updateAccount(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除科目")
    @Parameter(name = "id", description = "科目编号", required = true)
    @PreAuthorize("@ss.hasPermission('biz:fms:account:delete')")
    public CommonResult<Boolean> deleteAccount(@RequestParam("id") Long id) {
        accountService.deleteAccount(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得科目")
    @Parameter(name = "id", description = "科目编号", required = true)
    @PreAuthorize("@ss.hasPermission('biz:fms:account:query')")
    public CommonResult<FmsAccountRespVO> getAccount(@RequestParam("id") Long id) {
        return success(accountService.getAccount(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得科目分页")
    @PreAuthorize("@ss.hasPermission('biz:fms:account:query')")
    public CommonResult<PageResult<FmsAccountRespVO>> getAccountPage(@Valid FmsAccountPageReqVO pageReqVO) {
        return success(accountService.getAccountPage(pageReqVO));
    }
}
