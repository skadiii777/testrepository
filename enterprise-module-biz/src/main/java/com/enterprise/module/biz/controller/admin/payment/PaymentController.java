package com.enterprise.module.biz.controller.admin.payment;

import com.enterprise.framework.apilog.core.annotation.ApiAccessLog;
import com.enterprise.framework.common.pojo.CommonResult;
import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.module.biz.controller.admin.payment.vo.payment.PaymentPageReqVO;
import com.enterprise.module.biz.controller.admin.payment.vo.payment.PaymentRespVO;
import com.enterprise.module.biz.controller.admin.payment.vo.payment.PaymentSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.payment.PaymentDO;
import com.enterprise.module.biz.service.payment.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

import static com.enterprise.framework.apilog.core.enums.OperateTypeEnum.DELETE;
import static com.enterprise.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.enterprise.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 收付款流水")
@RestController
@RequestMapping("/biz/payment")
@Validated
public class PaymentController {

    @Resource
    private PaymentService paymentService;

    @PostMapping("/create")
    @Operation(summary = "登记收付款")
    @PreAuthorize("@ss.hasPermission('biz:payment:create')")
    public CommonResult<Long> createPayment(@Valid @RequestBody PaymentSaveReqVO createReqVO) {
        return success(paymentService.createPayment(createReqVO));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除收付款流水")
    @Parameter(name = "id", description = "流水编号", required = true)
    @PreAuthorize("@ss.hasPermission('biz:payment:delete')")
    public CommonResult<Boolean> deletePayment(@RequestParam("id") Long id) {
        paymentService.deletePayment(id);
        return success(true);
    }

    @PostMapping("/reverse")
    @Operation(summary = "冲销收付款流水（保留原流水）")
    @PreAuthorize("@ss.hasPermission('biz:payment:reverse')")
    public CommonResult<Long> reversePayment(@Valid @RequestBody
            com.enterprise.module.biz.controller.admin.payment.vo.payment.PaymentReverseReqVO req) {
        return success(paymentService.reversePayment(req.getId(), req.getReason()));
    }

    @GetMapping("/page")
    @Operation(summary = "获得收付款流水分页")
    @PreAuthorize("@ss.hasPermission('biz:payment:query')")
    public CommonResult<PageResult<PaymentRespVO>> getPaymentPage(@Valid PaymentPageReqVO pageReqVO) {
        PageResult<PaymentDO> pageResult = paymentService.getPaymentPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, PaymentRespVO.class));
    }

    @GetMapping("/paid-sum")
    @Operation(summary = "汇总指定单据的已收/已付金额")
    @Parameter(name = "bizType", description = "单据类型（1=销售单 2=采购单）", required = true)
    @Parameter(name = "orderId", description = "单据编号", required = true)
    @PreAuthorize("@ss.hasPermission('biz:payment:query')")
    public CommonResult<BigDecimal> getPaidSum(@RequestParam("bizType") String bizType,
                                               @RequestParam("orderId") Long orderId) {
        return success(paymentService.getPaidSumByOrder(bizType, orderId));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出收付款流水 Excel")
    @ApiAccessLog(operateType = EXPORT)
    @PreAuthorize("@ss.hasPermission('biz:payment:query')")
    public void exportPaymentExcel(@Valid PaymentPageReqVO pageReqVO,
                                   jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        pageReqVO.setPageSize(com.enterprise.framework.common.pojo.PageParam.PAGE_SIZE_NONE);
        java.util.List<PaymentRespVO> list = BeanUtils.toBean(
                paymentService.getPaymentPage(pageReqVO).getList(), PaymentRespVO.class);
        com.enterprise.framework.excel.core.util.ExcelUtils.write(response, "收付款流水.xls", "数据",
                PaymentRespVO.class, list);
    }

}
