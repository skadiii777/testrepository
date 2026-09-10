package com.enterprise.module.biz.controller.admin.business;

import com.enterprise.framework.apilog.core.annotation.ApiAccessLog;
import com.enterprise.framework.common.pojo.CommonResult;
import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.module.biz.controller.admin.business.vo.business.BusinessFunnelRespVO;
import com.enterprise.module.biz.controller.admin.business.vo.business.BusinessPageReqVO;
import com.enterprise.module.biz.controller.admin.business.vo.business.BusinessRespVO;
import com.enterprise.module.biz.controller.admin.business.vo.business.BusinessSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.business.BusinessDO;
import com.enterprise.module.biz.service.business.BusinessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.enterprise.framework.apilog.core.enums.OperateTypeEnum.DELETE;
import static com.enterprise.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.enterprise.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 商机")
@RestController
@RequestMapping("/biz/business")
@Validated
public class BusinessController {

    @Resource
    private BusinessService businessService;

    @PostMapping("/create")
    @Operation(summary = "创建商机")
    @PreAuthorize("@ss.hasPermission('biz:business:create')")
    public CommonResult<Long> createBusiness(@Valid @RequestBody BusinessSaveReqVO createReqVO) {
        return success(businessService.createBusiness(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新商机（赢单/输单为终局）")
    @PreAuthorize("@ss.hasPermission('biz:business:update')")
    public CommonResult<Boolean> updateBusiness(@Valid @RequestBody BusinessSaveReqVO updateReqVO) {
        businessService.updateBusiness(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除商机")
    @Parameter(name = "id", description = "商机编号", required = true)
    @PreAuthorize("@ss.hasPermission('biz:business:delete')")
    public CommonResult<Boolean> deleteBusiness(@RequestParam("id") Long id) {
        businessService.deleteBusiness(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得商机")
    @Parameter(name = "id", description = "商机编号", required = true)
    @PreAuthorize("@ss.hasPermission('biz:business:query')")
    public CommonResult<BusinessRespVO> getBusiness(@RequestParam("id") Long id) {
        return success(BeanUtils.toBean(businessService.getBusiness(id), BusinessRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得商机分页")
    @PreAuthorize("@ss.hasPermission('biz:business:query')")
    public CommonResult<PageResult<BusinessRespVO>> getBusinessPage(@Valid BusinessPageReqVO pageReqVO) {
        PageResult<BusinessDO> pageResult = businessService.getBusinessPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, BusinessRespVO.class));
    }

    @PostMapping("/convert-to-contract")
    @Operation(summary = "赢单商机一键转合同（带入客户/金额/负责人，合同编号自动生成）")
    @Parameter(name = "id", description = "商机编号", required = true)
    @PreAuthorize("@ss.hasPermission('biz:business:update')")
    public CommonResult<Long> convertToContract(@RequestParam("id") Long id,
            @Valid @RequestBody com.enterprise.module.biz.controller.admin.business.vo.business.BusinessContractConvertReqVO convertReqVO) {
        return success(businessService.convertToContract(id, convertReqVO));
    }

    @GetMapping("/funnel-stats")
    @Operation(summary = "销售漏斗统计（各阶段数量与预期金额）")
    @PreAuthorize("@ss.hasPermission('biz:business:query')")
    public CommonResult<List<BusinessFunnelRespVO>> getFunnelStats() {
        return success(businessService.getFunnelStats());
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得商机的精简列表（下拉用）")
    @PreAuthorize("@ss.hasPermission('biz:business:query')")
    public CommonResult<List<BusinessRespVO>> getSimpleBusinessList() {
        List<BusinessDO> list = businessService.getBusinessPage(
                new BusinessPageReqVO() {{ setPageNo(1); setPageSize(100); }}).getList();
        return success(BeanUtils.toBean(list, BusinessRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出商机 Excel")
    @ApiAccessLog(operateType = EXPORT)
    @PreAuthorize("@ss.hasPermission('biz:business:query')")
    public void exportBusinessExcel(@Valid BusinessPageReqVO pageReqVO,
                                    jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        pageReqVO.setPageSize(com.enterprise.framework.common.pojo.PageParam.PAGE_SIZE_NONE);
        List<BusinessRespVO> list = BeanUtils.toBean(
                businessService.getBusinessPage(pageReqVO).getList(), BusinessRespVO.class);
        com.enterprise.framework.excel.core.util.ExcelUtils.write(response, "商机.xls", "数据",
                BusinessRespVO.class, list);
    }

}
