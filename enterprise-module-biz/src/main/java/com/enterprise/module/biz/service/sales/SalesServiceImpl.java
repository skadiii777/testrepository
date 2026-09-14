package com.enterprise.module.biz.service.sales;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.module.biz.controller.admin.sales.vo.sales.SalesPageReqVO;
import com.enterprise.module.biz.controller.admin.sales.vo.sales.SalesSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.sales.SalesDO;
import com.enterprise.module.biz.dal.mysql.sales.SalesMapper;
import com.enterprise.module.biz.enums.ErrorCodeConstants;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import com.enterprise.module.biz.dal.dataobject.product.ProductDO;
import com.enterprise.module.biz.dal.mysql.product.ProductMapper;
import com.enterprise.module.biz.service.fms.FmsVoucherService;
import com.enterprise.module.biz.service.wms.WmsTaskService;
import com.enterprise.module.biz.service.stock.StockService;
import com.enterprise.module.biz.service.support.BizDocumentNo;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;

import static com.enterprise.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.enterprise.module.biz.enums.ErrorCodeConstants.*;

/**
 * 销售单 Service 实现类
 *
 * @author 企业管理平台
 */
@Slf4j
@Service
@Validated
public class SalesServiceImpl implements SalesService {
    private static final String ACC_INVENTORY = "1405";
    private static final String ACC_COST = "6401";

    @Resource private com.enterprise.module.biz.service.support.BizReferenceService references;

    @Resource
    private SalesMapper salesMapper;
    @Resource
    private StockService stockService;
    @Resource
    private ProductMapper productMapper;
    @Resource
    private FmsVoucherService fmsVoucherService;
    @Resource
    private WmsTaskService wmsTaskService;
    @Resource
    private com.enterprise.module.biz.service.credit.CreditService creditService;
    @Resource
    private com.enterprise.module.system.api.user.AdminUserApi adminUserApi;



    @Override
    public Long createSales(SalesSaveReqVO createReqVO) {
        SalesDO sales = BeanUtils.toBean(createReqVO, SalesDO.class);
        var product = references.product(sales.getProductId(), sales.getProductName());
        var warehouse = references.warehouse(sales.getWarehouseId(), sales.getWarehouse());
        sales.setProductId(product.getId()); sales.setProductName(product.getProductName());
        sales.setWarehouseId(warehouse.getId()); sales.setWarehouse(warehouse.getName());
        if (!org.springframework.util.StringUtils.hasText(sales.getSalesCode())) {
            sales.setSalesCode(BizDocumentNo.nextShort("XS")); // 留空自动生成，唯一键兜底
        }
        if (!org.springframework.util.StringUtils.hasText(sales.getEmpName())) {
            var user = adminUserApi.getUser(com.enterprise.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId());
            sales.setEmpName(user != null ? user.getNickname() : null); // 归属人默认登录人
        }
        sales.setStatus("0"); // 强制草稿，库存联动在"完成"流转时发生
        sales.setTotalAmount(java.math.BigDecimal.valueOf(sales.getQuantity()).multiply(sales.getPrice())); // 总金额服务端强算，不信前端
        salesMapper.insert(sales);
        return sales.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAndCompleteSales(SalesSaveReqVO createReqVO) {
        Long id = createSales(createReqVO);
        transitionSales(id, "confirm");
        completeSales(id);
        return id;
    }

    @Override
    public void transitionSales(Long id, String action) {
        SalesDO sales = validateSalesExists(id);
        String from = sales.getStatus();
        String to;
        if ("confirm".equals(action)) {
            if (!"0".equals(from)) {
                throw exception(ORDER_STATUS_TRANSITION_INVALID);
            }
            // 信用额度校验：确认即占用应收
            creditService.checkCredit(sales.getCustomerName(), sales.getTotalAmount());
            to = "1";
        } else if ("void".equals(action)) {
            if ("2".equals(from) || "3".equals(from)) {
                throw exception(ORDER_STATUS_TRANSITION_INVALID);
            }
            to = "3";
        } else {
            throw exception(ORDER_STATUS_TRANSITION_INVALID);
        }
        // CAS 状态流转：并发下仅一个请求成功，防止重复流转
        if (salesMapper.updateStatusByCas(id, from, to) == 0) {
            throw exception(ORDER_STATUS_TRANSITION_INVALID);
        }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void completeSales(Long id) {
        SalesDO sales = salesMapper.selectForUpdate(id);
        if (sales == null) throw exception(SALES_NOT_EXISTS);
        if (!"1".equals(sales.getStatus())) {
            throw exception(ORDER_STATUS_TRANSITION_INVALID);
        }
        // CAS 抢占状态：并发重复完成只有一个请求能改到，随后才动库存（失败回滚）
        if (salesMapper.updateStatusByCas(id, "1", "2") == 0) {
            throw exception(ORDER_STATUS_TRANSITION_INVALID);
        }
        boolean ok = stockService.changeStock(sales.getProductId(), sales.getWarehouseId(), -sales.getQuantity(),
                "sales", sales.getSalesCode());
        if (!ok) {
            throw exception(STOCK_NOT_ENOUGH);
        }
        // 自动凭证：销售出库结转成本 借主营业务成本 / 贷库存商品（按产品标准成本；未设成本则跳过并告警）
        ProductDO product = productMapper.selectById(sales.getProductId());
        BigDecimal cost = product != null ? product.getCost() : null;
        if (cost == null || cost.signum() <= 0) {
            log.warn("[completeSales] 产品 {} 未设置成本，销售单 {} 跳过结转凭证，请设置产品成本后手工补录",
                    sales.getProductId(), sales.getSalesCode());
        } else {
            fmsVoucherService.createSimplePosted("sales", id, LocalDate.now(),
                    "销售出库结转成本 " + sales.getSalesCode() + (sales.getCustomerName() != null ? "｜" + sales.getCustomerName() : ""),
                    ACC_COST, ACC_INVENTORY,
                    cost.multiply(BigDecimal.valueOf(sales.getQuantity())));
        }
        // WMS：生成拣货任务（销售完成后主库存已扣，库位待拣货下架）
        wmsTaskService.createPickTask(sales);
        SalesDO update = new SalesDO();
        update.setId(id);
        update.setStatus("2");
        salesMapper.updateById(update);
    }

    @Override
    public void validateStockEnough(String productName, Long quantity) {
        Long qty = stockService.findQuantity(references.product(null, productName).getId(), references.warehouse(null, null).getId());
        if (quantity == null || qty == null || qty < quantity) {
            throw exception(STOCK_NOT_ENOUGH);
        }
    }
    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void updateSales(SalesSaveReqVO updateReqVO) {
        SalesDO exist = salesMapper.selectForUpdate(updateReqVO.getId());
        if (exist == null) throw exception(SALES_NOT_EXISTS);
        // 已完成单据与库存流水绑定，禁止修改；纠错走退货/红冲
        if ("2".equals(exist.getStatus())) {
            throw exception(ORDER_COMPLETED_LOCKED);
        }
        SalesDO updateObj = BeanUtils.toBean(updateReqVO, SalesDO.class);
        var product = references.product(updateReqVO.getProductId() != null ? updateReqVO.getProductId() : exist.getProductId(), updateReqVO.getProductName());
        var warehouse = references.warehouse(updateReqVO.getWarehouseId() != null ? updateReqVO.getWarehouseId() : exist.getWarehouseId(), updateReqVO.getWarehouse());
        updateObj.setProductId(product.getId()); updateObj.setProductName(product.getProductName());
        updateObj.setWarehouseId(warehouse.getId()); updateObj.setWarehouse(warehouse.getName());
        updateObj.setStatus(null); // 状态只能通过流转接口变更
        // 总金额服务端强算：数量/单价留空取库内原值，且不信前端传入的 totalAmount
        updateObj.setTotalAmount(null);
        SalesDO current = exist;
        if (current != null) {
            Long qty = updateObj.getQuantity() != null ? updateObj.getQuantity() : current.getQuantity();
            java.math.BigDecimal price = updateObj.getPrice() != null ? updateObj.getPrice() : current.getPrice();
            if (qty != null && price != null) {
                updateObj.setTotalAmount(java.math.BigDecimal.valueOf(qty).multiply(price));
                updateObj.setQuantity(qty);
                updateObj.setPrice(price);
            }
        }
        salesMapper.updateById(updateObj);
    }


    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void deleteSales(Long id) {
        SalesDO locked = salesMapper.selectForUpdate(id);
        if (locked == null) throw exception(SALES_NOT_EXISTS);
        // 已完成单据与库存流水绑定，禁止删除
        if ("2".equals(locked.getStatus())) {
            throw exception(ORDER_COMPLETED_LOCKED);
        }
        salesMapper.deleteById(id);
    }

    private SalesDO validateSalesExists(Long id) {
        SalesDO sales = salesMapper.selectById(id);
        if (sales == null) {
            throw exception(SALES_NOT_EXISTS);
        }
        return sales;
    }

    @Override
    public SalesDO getSales(Long id) {
        return salesMapper.selectById(id);
    }

    @Override
    public PageResult<SalesDO> getSalesPage(SalesPageReqVO pageReqVO) {
        return salesMapper.selectPage(pageReqVO);
    }
}