package com.enterprise.module.system.controller.admin.registerapply;

import com.enterprise.framework.apilog.core.annotation.ApiAccessLog;
import com.enterprise.framework.common.pojo.CommonResult;
import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.module.system.controller.admin.registerapply.vo.registerapply.RegisterApplyPageReqVO;
import com.enterprise.module.system.controller.admin.registerapply.vo.registerapply.RegisterApplyRespVO;
import com.enterprise.module.system.dal.dataobject.registerapply.RegisterApplyDO;
import com.enterprise.module.system.service.registerapply.RegisterApplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.enterprise.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.enterprise.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 注册审批")
@RestController
@RequestMapping("/system/register-apply")
@Validated
public class RegisterApplyController {

    @Resource
    private RegisterApplyService registerApplyService;

    @GetMapping("/page")
    @Operation(summary = "获得注册申请分页")
    @PreAuthorize("@ss.hasPermission('biz:register-apply:query')")
    public CommonResult<PageResult<RegisterApplyRespVO>> getRegisterApplyPage(@Valid RegisterApplyPageReqVO pageReqVO) {
        PageResult<RegisterApplyDO> pageResult = registerApplyService.getRegisterApplyPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, RegisterApplyRespVO.class));
    }

    @PostMapping("/approve")
    @Operation(summary = "审批通过（创建正式账号并加入部门/岗位、分配角色）")
    @Parameter(name = "id", description = "申请编号", required = true)
    @PreAuthorize("@ss.hasPermission('biz:register-apply:audit')")
    public CommonResult<Boolean> approveRegisterApply(@RequestParam("id") Long id) {
        registerApplyService.approveApply(id);
        return success(true);
    }

    @PostMapping("/reject")
    @Operation(summary = "驳回注册申请")
    @PreAuthorize("@ss.hasPermission('biz:register-apply:audit')")
    public CommonResult<Boolean> rejectRegisterApply(@RequestParam("id") Long id,
                                                     @RequestParam(value = "reason", required = false) String reason) {
        registerApplyService.rejectApply(id, reason);
        return success(true);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出注册申请 Excel")
    @ApiAccessLog(operateType = EXPORT)
    @PreAuthorize("@ss.hasPermission('biz:register-apply:query')")
    public void exportRegisterApplyExcel(@Valid RegisterApplyPageReqVO pageReqVO,
                                         jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        pageReqVO.setPageSize(com.enterprise.framework.common.pojo.PageParam.PAGE_SIZE_NONE);
        java.util.List<RegisterApplyRespVO> list = BeanUtils.toBean(
                registerApplyService.getRegisterApplyPage(pageReqVO).getList(), RegisterApplyRespVO.class);
        com.enterprise.framework.excel.core.util.ExcelUtils.write(response, "注册申请.xls", "数据",
                RegisterApplyRespVO.class, list);
    }

}
