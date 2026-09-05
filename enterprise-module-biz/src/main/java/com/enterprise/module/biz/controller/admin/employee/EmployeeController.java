package com.enterprise.module.biz.controller.admin.employee;

import com.enterprise.framework.common.pojo.CommonResult;
import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.framework.excel.core.util.ExcelUtils;
import com.enterprise.framework.apilog.core.annotation.ApiAccessLog;
import com.enterprise.module.biz.controller.admin.employee.vo.employee.*;
import com.enterprise.module.biz.dal.dataobject.employee.EmployeeDO;
import com.enterprise.module.biz.service.employee.EmployeeService;
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

@Tag(name = "管理后台 - 员工")
@RestController
@RequestMapping("/biz/employee")
@Validated
public class EmployeeController {

    @Resource
    private EmployeeService employeeService;

    @PostMapping("/create")
    @Operation(summary = "创建员工")
    @PreAuthorize("@ss.hasPermission('biz:employee:create')")
    public CommonResult<Long> createEmployee(@Valid @RequestBody EmployeeSaveReqVO createReqVO) {
        return success(employeeService.createEmployee(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新员工")
    @PreAuthorize("@ss.hasPermission('biz:employee:update')")
    public CommonResult<Boolean> updateEmployee(@Valid @RequestBody EmployeeSaveReqVO updateReqVO) {
        employeeService.updateEmployee(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除员工")
    @Parameter(name = "id", description = "主键", required = true)
    @PreAuthorize("@ss.hasPermission('biz:employee:delete')")
    public CommonResult<Boolean> deleteEmployee(@RequestParam("id") Long id) {
        employeeService.deleteEmployee(id);
        return success(true);
    }
    @GetMapping("/get")
    @Operation(summary = "获得员工")
    @Parameter(name = "id", description = "主键", required = true)
    @PreAuthorize("@ss.hasPermission('biz:employee:query')")
    public CommonResult<EmployeeRespVO> getEmployee(@RequestParam("id") Long id) {
        EmployeeDO employee = employeeService.getEmployee(id);
        return success(BeanUtils.toBean(employee, EmployeeRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得员工分页")
    @PreAuthorize("@ss.hasPermission('biz:employee:query')")
    public CommonResult<PageResult<EmployeeRespVO>> getEmployeePage(@Valid EmployeePageReqVO pageReqVO) {
        PageResult<EmployeeDO> pageResult = employeeService.getEmployeePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, EmployeeRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出员工 Excel")
    @PreAuthorize("@ss.hasPermission('biz:employee:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportEmployeeExcel(@Valid EmployeePageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PAGE_SIZE_NONE);
        List<EmployeeRespVO> list = BeanUtils.toBean(employeeService.getEmployeePage(pageReqVO).getList(), EmployeeRespVO.class);
        ExcelUtils.write(response, "员工.xls", "数据", EmployeeRespVO.class, list);
    }
}