package com.ruoyi.business.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.ruoyi.business.domain.Attendance;
import com.ruoyi.business.domain.Contract;
import com.ruoyi.business.domain.Customer;
import com.ruoyi.business.domain.Employee;
import com.ruoyi.business.domain.Leave;
import com.ruoyi.business.domain.Product;
import com.ruoyi.business.domain.Purchase;
import com.ruoyi.business.domain.Sales;
import com.ruoyi.business.domain.Stock;
import com.ruoyi.business.mapper.BizDashboardMapper;
import com.ruoyi.business.service.IAttendanceService;
import com.ruoyi.business.service.IContractService;
import com.ruoyi.business.service.ICustomerService;
import com.ruoyi.business.service.IEmployeeService;
import com.ruoyi.business.service.ILeaveService;
import com.ruoyi.business.service.IProductService;
import com.ruoyi.business.service.IPurchaseService;
import com.ruoyi.business.service.ISalesService;
import com.ruoyi.business.service.IStockService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;

/**
 * 首页数据看板
 *
 * @author biz
 */
@Controller
@RequestMapping("/biz/dashboard")
public class BizDashboardController extends BaseController
{
    private String prefix = "biz/dashboard";

    @Autowired
    private ICustomerService customerService;
    @Autowired
    private IProductService productService;
    @Autowired
    private IContractService contractService;
    @Autowired
    private IPurchaseService purchaseService;
    @Autowired
    private ISalesService salesService;
    @Autowired
    private IStockService stockService;
    @Autowired
    private IEmployeeService employeeService;
    @Autowired
    private IAttendanceService attendanceService;
    @Autowired
    private ILeaveService leaveService;
    @Autowired
    private BizDashboardMapper dashboardMapper;

    @GetMapping()
    public String dashboard()
    {
        return prefix + "/dashboard";
    }

    /** 核心指标卡 */
    @PostMapping("/panel")
    @ResponseBody
    public AjaxResult panel()
    {
        Map<String, Object> data = new HashMap<>();
        data.put("customerCount", customerService.selectCustomerList(new Customer()).size());
        data.put("productCount", productService.selectProductList(new Product()).size());
        data.put("contractCount", contractService.selectContractList(new Contract()).size());
        data.put("purchaseCount", purchaseService.selectPurchaseList(new Purchase()).size());
        data.put("salesCount", salesService.selectSalesList(new Sales()).size());
        data.put("stockCount", stockService.selectStockList(new Stock()).size());
        data.put("employeeCount", employeeService.selectEmployeeList(new Employee()).size());
        data.put("attendanceCount", attendanceService.selectAttendanceList(new Attendance()).size());
        Leave pending = new Leave();
        pending.setStatus("0");
        data.put("leavePending", leaveService.selectLeaveList(pending).size());
        data.put("lowStockCount", stockService.selectLowStockList().size());
        return success(data);
    }

    /** 近7日销售/采购金额趋势 */
    @PostMapping("/trend")
    @ResponseBody
    public AjaxResult trend()
    {
        java.time.LocalDate start = java.time.LocalDate.now().minusDays(6);
        Map<String, Object> salesMap = new HashMap<>();
        for (Map<String, Object> row : dashboardMapper.selectSalesSum(start.toString()))
        {
            salesMap.put(String.valueOf(row.get("date")), row.get("total"));
        }
        Map<String, Object> purchaseMap = new HashMap<>();
        for (Map<String, Object> row : dashboardMapper.selectPurchaseSum(start.toString()))
        {
            purchaseMap.put(String.valueOf(row.get("date")), row.get("total"));
        }
        List<String> dates = new ArrayList<>();
        List<Object> sales = new ArrayList<>();
        List<Object> purchase = new ArrayList<>();
        for (int i = 0; i < 7; i++)
        {
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

    /** 产品销售Top5 */
    @PostMapping("/productTop")
    @ResponseBody
    public AjaxResult productTop()
    {
        return success(dashboardMapper.selectProductTop(5));
    }

    /** 状态分布（合同/请假） */
    @PostMapping("/status")
    @ResponseBody
    public AjaxResult status()
    {
        Map<String, Object> data = new HashMap<>();
        data.put("contract", dashboardMapper.selectContractStatus());
        data.put("leave", dashboardMapper.selectLeaveStatus());
        return success(data);
    }
}
