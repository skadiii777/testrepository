package com.enterprise.module.biz.controller.admin.returnorder;

import com.enterprise.framework.apilog.core.annotation.ApiAccessLog;
import com.enterprise.framework.common.pojo.CommonResult;
import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.module.biz.controller.admin.returnorder.vo.returnorder.ReturnPageReqVO;
import com.enterprise.module.biz.controller.admin.returnorder.vo.returnorder.ReturnRespVO;
import com.enterprise.module.biz.controller.admin.returnorder.vo.returnorder.ReturnSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.returnorder.ReturnDO;
import com.enterprise.module.biz.service.returnorder.ReturnService;
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

@Tag(name = "管理后台 - 退货单")
@RestController
@RequestMapping("/biz/return")
@Validated
public class ReturnController {

    @Resource
    private ReturnService returnService;

    @PostMapping("/create")
    @Operation(summary = "创建退货单")
    @PreAuthorize("@ss.hasPermission('biz:return:create')")
    public CommonResult<Long> createReturn(@Valid @RequestBody ReturnSaveReqVO createReqVO) {
        return success(returnService.createReturn(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新退货单（仅待退货）")
    @PreAuthorize("@ss.hasPermission('biz:return:update')")
    public CommonResult<Boolean> updateReturn(@Valid @RequestBody ReturnSaveReqVO updateReqVO) {
        returnService.updateReturn(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除退货单")
    @Parameter(name = "id", description = "退货单编号", required = true)
    @PreAuthorize("@ss.hasPermission('biz:return:delete')")
    public CommonResult<Boolean> deleteReturn(@RequestParam("id") Long id) {
        returnService.deleteReturn(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得退货单")
    @Parameter(name = "id", description = "退货单编号", required = true)
    @PreAuthorize("@ss.hasPermission('biz:return:query')")
    public CommonResult<ReturnRespVO> getReturn(@RequestParam("id") Long id) {
        return success(BeanUtils.toBean(returnService.getReturn(id), ReturnRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得退货单分页")
    @PreAuthorize("@ss.hasPermission('biz:return:query')")
    public CommonResult<PageResult<ReturnRespVO>> getReturnPage(@Valid ReturnPageReqVO pageReqVO) {
        PageResult<ReturnDO> pageResult = returnService.getReturnPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ReturnRespVO.class));
    }

    @PutMapping("/execute")
    @Operation(summary = "执行退货（联动库存）")
    @Parameter(name = "id", description = "退货单编号", required = true)
    @PreAuthorize("@ss.hasPermission('biz:return:update')")
    public CommonResult<Boolean> executeReturn(@RequestParam("id") Long id) {
        returnService.executeReturn(id);
        return success(true);
    }

    @PutMapping("/void")
    @Operation(summary = "作废退货单（仅待退货）")
    @Parameter(name = "id", description = "退货单编号", required = true)
    @PreAuthorize("@ss.hasPermission('biz:return:update')")
    public CommonResult<Boolean> voidReturn(@RequestParam("id") Long id) {
        returnService.voidReturn(id);
        return success(true);
    }

    @GetMapping("/returned-sum")
    @Operation(summary = "汇总指定单据的已退数量（不含已作废）")
    @Parameter(name = "returnType", description = "退货类型（1=销售退货 2=采购退货）", required = true)
    @Parameter(name = "orderId", description = "单据编号", required = true)
    @PreAuthorize("@ss.hasPermission('biz:return:query')")
    public CommonResult<Long> getReturnedSum(@RequestParam("returnType") String returnType,
                                             @RequestParam("orderId") Long orderId) {
        return success(returnService.getReturnedSumByOrder(returnType, orderId));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出退货单 Excel")
    @ApiAccessLog(operateType = EXPORT)
    @PreAuthorize("@ss.hasPermission('biz:return:query')")
    public void exportReturnExcel(@Valid ReturnPageReqVO pageReqVO,
                                  jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        pageReqVO.setPageSize(com.enterprise.framework.common.pojo.PageParam.PAGE_SIZE_NONE);
        List<ReturnRespVO> list = BeanUtils.toBean(
                returnService.getReturnPage(pageReqVO).getList(), ReturnRespVO.class);
        com.enterprise.framework.excel.core.util.ExcelUtils.write(response, "退货单.xls", "数据",
                ReturnRespVO.class, list);
    }

}
