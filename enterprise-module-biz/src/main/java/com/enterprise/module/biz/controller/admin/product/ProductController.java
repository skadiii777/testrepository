package com.enterprise.module.biz.controller.admin.product;

import com.enterprise.framework.common.pojo.CommonResult;
import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.framework.excel.core.util.ExcelUtils;
import com.enterprise.framework.apilog.core.annotation.ApiAccessLog;
import com.enterprise.module.biz.controller.admin.product.vo.product.*;
import com.enterprise.module.biz.dal.dataobject.product.ProductDO;
import com.enterprise.module.biz.service.product.ProductService;
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

@Tag(name = "管理后台 - 产品")
@RestController
@RequestMapping("/biz/product")
@Validated
public class ProductController {

    @Resource
    private ProductService productService;

    @PostMapping("/create")
    @Operation(summary = "创建产品")
    @PreAuthorize("@ss.hasPermission('biz:product:create')")
    public CommonResult<Long> createProduct(@Valid @RequestBody ProductSaveReqVO createReqVO) {
        return success(productService.createProduct(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新产品")
    @PreAuthorize("@ss.hasPermission('biz:product:update')")
    public CommonResult<Boolean> updateProduct(@Valid @RequestBody ProductSaveReqVO updateReqVO) {
        productService.updateProduct(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除产品")
    @Parameter(name = "id", description = "主键", required = true)
    @PreAuthorize("@ss.hasPermission('biz:product:delete')")
    public CommonResult<Boolean> deleteProduct(@RequestParam("id") Long id) {
        productService.deleteProduct(id);
        return success(true);
    }
    @GetMapping("/get")
    @Operation(summary = "获得产品")
    @Parameter(name = "id", description = "主键", required = true)
    @PreAuthorize("@ss.hasPermission('biz:product:query')")
    public CommonResult<ProductRespVO> getProduct(@RequestParam("id") Long id) {
        ProductDO product = productService.getProduct(id);
        return success(BeanUtils.toBean(product, ProductRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得产品分页")
    @PreAuthorize("@ss.hasPermission('biz:product:query')")
    public CommonResult<PageResult<ProductRespVO>> getProductPage(@Valid ProductPageReqVO pageReqVO) {
        PageResult<ProductDO> pageResult = productService.getProductPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ProductRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出产品 Excel")
    @PreAuthorize("@ss.hasPermission('biz:product:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportProductExcel(@Valid ProductPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PAGE_SIZE_NONE);
        List<ProductRespVO> list = BeanUtils.toBean(productService.getProductPage(pageReqVO).getList(), ProductRespVO.class);
        ExcelUtils.write(response, "产品.xls", "数据", ProductRespVO.class, list);
    }
}