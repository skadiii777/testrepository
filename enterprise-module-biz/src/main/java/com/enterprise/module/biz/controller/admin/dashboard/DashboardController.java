package com.enterprise.module.biz.controller.admin.dashboard;

import com.enterprise.framework.common.pojo.CommonResult;
import com.enterprise.module.biz.dal.mysql.contract.ContractMapper;
import com.enterprise.module.biz.dal.mysql.leave.LeaveMapper;
import com.enterprise.module.biz.dal.mysql.purchase.PurchaseMapper;
import com.enterprise.module.biz.dal.mysql.sales.SalesMapper;
import com.enterprise.module.biz.dal.mysql.stock.StockMapper;
import com.enterprise.module.biz.dal.mysql.customer.CustomerMapper;
import com.enterprise.module.biz.dal.mysql.product.ProductMapper;
import com.enterprise.module.biz.dal.mysql.employee.EmployeeMapper;
import com.enterprise.module.biz.dal.mysql.expense.ExpenseMapper;
import com.enterprise.module.biz.dal.mysql.payment.PaymentMapper;
import com.enterprise.module.biz.dal.dataobject.payment.PaymentDO;
import com.enterprise.module.biz.dal.dataobject.leave.LeaveDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

import static com.enterprise.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 数据看板")
@RestController
@RequestMapping("/biz/dashboard")
@Validated
public class DashboardController {

    @Resource
    private CustomerMapper customerMapper;
    @Resource
    private ProductMapper productMapper;
    @Resource
    private EmployeeMapper employeeMapper;
    @Resource
    private SalesMapper salesMapper;
    @Resource
    private PurchaseMapper purchaseMapper;
    @Resource
    private StockMapper stockMapper;
    @Resource
    private LeaveMapper leaveMapper;
    @Resource
    private ExpenseMapper expenseMapper;
    @Resource
    private PaymentMapper paymentMapper;
    @Resource
    private ContractMapper contractMapper;
    @Resource
    private com.enterprise.module.biz.service.digest.BizDigestService digestService;

    @PostMapping("/panel")
    @Operation(summary = "核心指标卡")
    @PreAuthorize("@ss.hasPermission('biz:dashboard:query')")
    public CommonResult<Map<String, Object>> panel() {
        Map<String, Object> data = new HashMap<>();
        data.put("customerCount", customerMapper.selectCount());
        data.put("productCount", productMapper.selectCount());
        data.put("employeeCount", employeeMapper.selectCount());
        data.put("salesCount", salesMapper.selectCount());
        data.put("purchaseCount", purchaseMapper.selectCount());
        LeaveDO pending = new LeaveDO();
        pending.setStatus("0");
        data.put("leavePending", leaveMapper.selectCount(new QueryWrapper<LeaveDO>().eq("status", "0")));
        data.put("expensePending", expenseMapper.selectCount(new QueryWrapper<com.enterprise.module.biz.dal.dataobject.expense.ExpenseDO>().eq("status", "0")));
        // 库存预警数
        data.put("lowStockCount", stockMapper.selectList(new QueryWrapper<com.enterprise.module.biz.dal.dataobject.stock.StockDO>()
                .apply("quantity <= min_quantity")).size());
        // 本月收付款合计（payment_date 为 yyyy-MM-dd 字符串，按前缀匹配月份）
        String monthPrefix = String.format("%04d-%02d", LocalDate.now().getYear(), LocalDate.now().getMonthValue());
        data.put("monthReceived", paymentMapper.selectList(new QueryWrapper<PaymentDO>()
                .eq("payment_type", "1").likeRight("payment_date", monthPrefix)).stream()
                .map(PaymentDO::getAmount).reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
        data.put("monthPaid", paymentMapper.selectList(new QueryWrapper<PaymentDO>()
                .eq("payment_type", "2").likeRight("payment_date", monthPrefix)).stream()
                .map(PaymentDO::getAmount).reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
        return success(data);
    }

    @PostMapping("/trend")
    @Operation(summary = "近7日销售/采购金额趋势")
    @PreAuthorize("@ss.hasPermission('biz:dashboard:query')")
    public CommonResult<Map<String, Object>> trend() {
        LocalDate start = LocalDate.now().minusDays(6);
        Map<String, Object> salesMap = new HashMap<>();
        for (Map<String, Object> row : salesMapper.selectSumByDate(start.toString())) {
            salesMap.put(String.valueOf(row.get("date")), row.get("total"));
        }
        Map<String, Object> purchaseMap = new HashMap<>();
        for (Map<String, Object> row : purchaseMapper.selectSumByDate(start.toString())) {
            purchaseMap.put(String.valueOf(row.get("date")), row.get("total"));
        }
        List<String> dates = new ArrayList<>();
        List<Object> sales = new ArrayList<>();
        List<Object> purchase = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            String d = start.plusDays(i).toString();
            dates.add(d.substring(5));
            sales.add(salesMap.getOrDefault(d, 0));
            purchase.add(purchaseMap.getOrDefault(d, 0));
        }
        Map<String, Object> data = new HashMap<>();
        data.put("dates", dates);
        data.put("sales", sales);
        data.put("purchase", purchase);
        return success(data);
    }

    @PostMapping("/productTop")
    @Operation(summary = "产品销售Top5")
    @PreAuthorize("@ss.hasPermission('biz:dashboard:query')")
    public CommonResult<List<Map<String, Object>>> productTop() {
        return success(salesMapper.selectProductTop(5));
    }

    @PostMapping("/weekly-digest")
    @Operation(summary = "手动发送上周经营周报（站内信）")
    @PreAuthorize("@ss.hasPermission('biz:dashboard:query')")
    public CommonResult<String> sendWeeklyDigest() {
        return success(digestService.sendWeeklyDigest());
    }

    @PostMapping("/status")
    @Operation(summary = "状态分布（合同/请假）")
    @PreAuthorize("@ss.hasPermission('biz:dashboard:query')")
    public CommonResult<Map<String, Object>> status() {
        Map<String, Object> data = new HashMap<>();
        data.put("contract", contractMapper.selectStatusCount());
        data.put("leave", leaveMapper.selectStatusCount());
        return success(data);
    }
}