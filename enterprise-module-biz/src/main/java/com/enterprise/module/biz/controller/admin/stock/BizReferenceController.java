package com.enterprise.module.biz.controller.admin.stock;
import com.enterprise.framework.common.pojo.CommonResult;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.enterprise.module.biz.dal.mysql.product.ProductMapper;
import com.enterprise.module.biz.dal.mysql.employee.EmployeeMapper;
import com.enterprise.module.biz.dal.mysql.warehouse.WarehouseMapper;
import com.enterprise.module.biz.dal.dataobject.product.ProductDO;
import com.enterprise.module.biz.dal.dataobject.employee.EmployeeDO;
import com.enterprise.module.biz.dal.dataobject.warehouse.WarehouseDO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;
import static com.enterprise.framework.common.pojo.CommonResult.success;
@RestController @RequestMapping("/biz/reference")
public class BizReferenceController {
    @Resource private ProductMapper products;
    @Resource private EmployeeMapper employees;
    @Resource private WarehouseMapper warehouses;
    public record Option(Long id, String name, String code) {}
    @GetMapping("/products")
    @PreAuthorize("@ss.hasAnyPermissions('biz:product:query','biz:sales:create','biz:sales:update','biz:purchase:create','biz:purchase:update','biz:stock:create','biz:stock:update','biz:stockcheck:create')")
    public CommonResult<List<Option>> products(@RequestParam(required=false) String name) {
        return success(products.selectList(new LambdaQueryWrapperX<ProductDO>().likeIfPresent(ProductDO::getProductName, name)
            .orderByAsc(ProductDO::getId).last("LIMIT 100")).stream().map(p -> new Option(p.getId(),p.getProductName(),p.getProductCode())).toList());
    }
    @GetMapping("/employees")
    @PreAuthorize("@ss.hasAnyPermissions('biz:employee:query','biz:quota:create','biz:quota:update','biz:leave:create','biz:leave:update')")
    public CommonResult<List<Option>> employees(@RequestParam(required=false) String name) {
        return success(employees.selectList(new LambdaQueryWrapperX<EmployeeDO>().likeIfPresent(EmployeeDO::getEmpName, name)
            .orderByAsc(EmployeeDO::getId).last("LIMIT 100")).stream().map(p -> new Option(p.getId(),p.getEmpName(),p.getEmpNo())).toList());
    }
    @GetMapping("/warehouses")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<List<Option>> warehouses() {
        return success(warehouses.selectList().stream().map(p -> new Option(p.getId(),p.getName(),null)).toList());
    }
    @PostMapping("/warehouses")
    @PreAuthorize("@ss.hasPermission('biz:stock:create')")
    public CommonResult<Long> createWarehouse(@RequestParam String name) {
        if (name.isBlank() || name.length() > 50) throw new IllegalArgumentException("仓库名称长度应为1至50字");
        WarehouseDO value = new WarehouseDO(); value.setName(name.trim()); warehouses.insert(value); return success(value.getId());
    }
}
