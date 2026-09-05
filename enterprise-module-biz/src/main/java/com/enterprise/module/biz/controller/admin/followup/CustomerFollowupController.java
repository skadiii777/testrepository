package com.enterprise.module.biz.controller.admin.followup;

import com.enterprise.framework.apilog.core.annotation.ApiAccessLog;
import com.enterprise.framework.common.pojo.CommonResult;
import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.framework.excel.core.util.ExcelUtils;
import com.enterprise.module.biz.controller.admin.followup.vo.followup.CustomerFollowupPageReqVO;
import com.enterprise.module.biz.controller.admin.followup.vo.followup.CustomerFollowupRespVO;
import com.enterprise.module.biz.controller.admin.followup.vo.followup.CustomerFollowupSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.followup.CustomerFollowupDO;
import com.enterprise.module.biz.service.followup.CustomerFollowupService;
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

import static com.enterprise.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.enterprise.framework.common.pojo.CommonResult.success;
import static com.enterprise.framework.common.pojo.PageParam.PAGE_SIZE_NONE;

@Tag(name = "管理后台 - 客户跟进")
@RestController
@RequestMapping("/biz/followup")
@Validated
public class CustomerFollowupController {

    @Resource
    private CustomerFollowupService followupService;

    @PostMapping("/create")
    @Operation(summary = "创建跟进记录")
    @PreAuthorize("@ss.hasPermission('biz:followup:create')")
    public CommonResult<Long> createFollowup(@Valid @RequestBody CustomerFollowupSaveReqVO createReqVO) {
        return success(followupService.createFollowup(createReqVO));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除跟进记录")
    @Parameter(name = "id", description = "主键", required = true)
    @PreAuthorize("@ss.hasPermission('biz:followup:delete')")
    public CommonResult<Boolean> deleteFollowup(@RequestParam("id") Long id) {
        followupService.deleteFollowup(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得跟进记录")
    @Parameter(name = "id", description = "主键", required = true)
    @PreAuthorize("@ss.hasPermission('biz:followup:query')")
    public CommonResult<CustomerFollowupRespVO> getFollowup(@RequestParam("id") Long id) {
        CustomerFollowupDO followup = followupService.getFollowup(id);
        return success(BeanUtils.toBean(followup, CustomerFollowupRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得跟进分页")
    @PreAuthorize("@ss.hasPermission('biz:followup:query')")
    public CommonResult<PageResult<CustomerFollowupRespVO>> getFollowupPage(
            @Valid CustomerFollowupPageReqVO pageReqVO) {
        PageResult<CustomerFollowupDO> pageResult = followupService.getFollowupPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, CustomerFollowupRespVO.class));
    }

    @GetMapping("/list-by-customer")
    @Operation(summary = "按客户查询跟进记录（时间倒序）")
    @Parameter(name = "customerName", description = "客户名称", required = true)
    @PreAuthorize("@ss.hasPermission('biz:followup:query')")
    public CommonResult<List<CustomerFollowupRespVO>> getFollowupListByCustomer(
            @RequestParam("customerName") String customerName) {
        List<CustomerFollowupDO> list = followupService.getFollowupListByCustomer(customerName);
        return success(BeanUtils.toBean(list, CustomerFollowupRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出跟进 Excel")
    @PreAuthorize("@ss.hasPermission('biz:followup:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportFollowupExcel(@Valid CustomerFollowupPageReqVO pageReqVO,
                                    HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PAGE_SIZE_NONE);
        List<CustomerFollowupRespVO> list = BeanUtils.toBean(
                followupService.getFollowupPage(pageReqVO).getList(), CustomerFollowupRespVO.class);
        ExcelUtils.write(response, "客户跟进.xls", "数据", CustomerFollowupRespVO.class, list);
    }
}
