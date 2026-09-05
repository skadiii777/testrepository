package com.enterprise.module.biz.controller.admin.contract;

import com.enterprise.framework.common.pojo.CommonResult;
import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.framework.excel.core.util.ExcelUtils;
import com.enterprise.framework.apilog.core.annotation.ApiAccessLog;
import com.enterprise.module.biz.controller.admin.contract.vo.contract.*;
import com.enterprise.module.biz.dal.dataobject.contract.ContractDO;
import com.enterprise.module.biz.service.contract.ContractService;
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

@Tag(name = "管理后台 - 合同")
@RestController
@RequestMapping("/biz/contract")
@Validated
public class ContractController {

    @Resource
    private ContractService contractService;

    @PostMapping("/create")
    @Operation(summary = "创建合同")
    @PreAuthorize("@ss.hasPermission('biz:contract:create')")
    public CommonResult<Long> createContract(@Valid @RequestBody ContractSaveReqVO createReqVO) {
        return success(contractService.createContract(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新合同")
    @PreAuthorize("@ss.hasPermission('biz:contract:update')")
    public CommonResult<Boolean> updateContract(@Valid @RequestBody ContractSaveReqVO updateReqVO) {
        contractService.updateContract(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除合同")
    @Parameter(name = "id", description = "主键", required = true)
    @PreAuthorize("@ss.hasPermission('biz:contract:delete')")
    public CommonResult<Boolean> deleteContract(@RequestParam("id") Long id) {
        contractService.deleteContract(id);
        return success(true);
    }
    @GetMapping("/get")
    @Operation(summary = "获得合同")
    @Parameter(name = "id", description = "主键", required = true)
    @PreAuthorize("@ss.hasPermission('biz:contract:query')")
    public CommonResult<ContractRespVO> getContract(@RequestParam("id") Long id) {
        ContractDO contract = contractService.getContract(id);
        return success(BeanUtils.toBean(contract, ContractRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得合同分页")
    @PreAuthorize("@ss.hasPermission('biz:contract:query')")
    public CommonResult<PageResult<ContractRespVO>> getContractPage(@Valid ContractPageReqVO pageReqVO) {
        PageResult<ContractDO> pageResult = contractService.getContractPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ContractRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出合同 Excel")
    @PreAuthorize("@ss.hasPermission('biz:contract:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportContractExcel(@Valid ContractPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PAGE_SIZE_NONE);
        List<ContractRespVO> list = BeanUtils.toBean(contractService.getContractPage(pageReqVO).getList(), ContractRespVO.class);
        ExcelUtils.write(response, "合同.xls", "数据", ContractRespVO.class, list);
    }
}