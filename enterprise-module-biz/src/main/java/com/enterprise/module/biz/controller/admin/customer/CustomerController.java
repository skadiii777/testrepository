package com.enterprise.module.biz.controller.admin.customer;

import com.enterprise.framework.common.pojo.CommonResult;
import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.framework.excel.core.util.ExcelUtils;
import com.enterprise.framework.apilog.core.annotation.ApiAccessLog;
import com.enterprise.module.biz.controller.admin.customer.vo.customer.*;
import com.enterprise.module.biz.dal.dataobject.customer.CustomerDO;
import com.enterprise.module.biz.service.customer.CustomerService;
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

@Tag(name = "管理后台 - 客户")
@RestController
@RequestMapping("/biz/customer")
@Validated
public class CustomerController {

    @Resource
    private CustomerService customerService;

    @PostMapping("/create")
    @Operation(summary = "创建客户")
    @PreAuthorize("@ss.hasPermission('biz:customer:create')")
    public CommonResult<Long> createCustomer(@Valid @RequestBody CustomerSaveReqVO createReqVO) {
        return success(customerService.createCustomer(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新客户")
    @PreAuthorize("@ss.hasPermission('biz:customer:update')")
    public CommonResult<Boolean> updateCustomer(@Valid @RequestBody CustomerSaveReqVO updateReqVO) {
        customerService.updateCustomer(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除客户")
    @Parameter(name = "id", description = "主键", required = true)
    @PreAuthorize("@ss.hasPermission('biz:customer:delete')")
    public CommonResult<Boolean> deleteCustomer(@RequestParam("id") Long id) {
        customerService.deleteCustomer(id);
        return success(true);
    }
    @GetMapping("/get")
    @Operation(summary = "获得客户")
    @Parameter(name = "id", description = "主键", required = true)
    @PreAuthorize("@ss.hasPermission('biz:customer:query')")
    public CommonResult<CustomerRespVO> getCustomer(@RequestParam("id") Long id) {
        CustomerDO customer = customerService.getCustomer(id);
        return success(BeanUtils.toBean(customer, CustomerRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得客户分页")
    @PreAuthorize("@ss.hasPermission('biz:customer:query')")
    public CommonResult<PageResult<CustomerRespVO>> getCustomerPage(@Valid CustomerPageReqVO pageReqVO) {
        PageResult<CustomerDO> pageResult = customerService.getCustomerPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, CustomerRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出客户 Excel")
    @PreAuthorize("@ss.hasPermission('biz:customer:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportCustomerExcel(@Valid CustomerPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PAGE_SIZE_NONE);
        List<CustomerRespVO> list = BeanUtils.toBean(customerService.getCustomerPage(pageReqVO).getList(), CustomerRespVO.class);
        ExcelUtils.write(response, "客户.xls", "数据", CustomerRespVO.class, list);
    }
}