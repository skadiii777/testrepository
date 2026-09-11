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
import org.springframework.validation.annotation.Validated;
import com.enterprise.module.biz.service.stock.StockService;

import static com.enterprise.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.enterprise.module.biz.enums.ErrorCodeConstants.*;

/**
 * 销售单 Service 实现类
 *
 * @author 企业管理平台
 */
@Service
@Validated
public class SalesServiceImpl implements SalesService {

    @Resource
    private SalesMapper salesMapper;
    @Resource
    private StockService stockService;



    @Override
    public Long createSales(SalesSaveReqVO createReqVO) {
        SalesDO sales = BeanUtils.toBean(createReqVO, SalesDO.class);
        sales.setStatus("0"); // 强制草稿，库存联动在"完成"流转时发生
        sales.setTotalAmount(java.math.BigDecimal.valueOf(sales.getQuantity()).multiply(sales.getPrice())); // 总金额服务端强算，不信前端
        salesMapper.insert(sales);
        return sales.getId();
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
        SalesDO sales = validateSalesExists(id);
        if (!"1".equals(sales.getStatus())) {
            throw exception(ORDER_STATUS_TRANSITION_INVALID);
        }
        // CAS 抢占状态：并发重复完成只有一个请求能改到，随后才动库存（失败回滚）
        if (salesMapper.updateStatusByCas(id, "1", "2") == 0) {
            throw exception(ORDER_STATUS_TRANSITION_INVALID);
        }
        validateStockEnough(sales.getProductName(), sales.getQuantity());
        boolean ok = stockService.changeStock(sales.getProductName(), "默认仓库", -sales.getQuantity(),
                "sales", sales.getSalesCode());
        if (!ok) {
            throw exception(STOCK_NOT_ENOUGH);
        }
        SalesDO update = new SalesDO();
        update.setId(id);
        update.setStatus("2");
        salesMapper.updateById(update);
    }

    @Override
    public void validateStockEnough(String productName, Long quantity) {
        Long qty = stockService.findQuantity(productName, "默认仓库");
        if (quantity == null || qty == null || qty < quantity) {
            throw exception(STOCK_NOT_ENOUGH);
        }
    }
    @Override
    public void updateSales(SalesSaveReqVO updateReqVO) {
        validateSalesExists(updateReqVO.getId());
        // 已完成单据与库存流水绑定，禁止修改；纠错走退货/红冲
        if ("2".equals(salesMapper.selectById(updateReqVO.getId()).getStatus())) {
            throw exception(ORDER_COMPLETED_LOCKED);
        }
        SalesDO updateObj = BeanUtils.toBean(updateReqVO, SalesDO.class);
        updateObj.setStatus(null); // 状态只能通过流转接口变更
        // 总金额服务端强算：数量/单价留空取库内原值，且不信前端传入的 totalAmount
        updateObj.setTotalAmount(null);
        SalesDO current = salesMapper.selectById(updateReqVO.getId());
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
    public void deleteSales(Long id) {
        validateSalesExists(id);
        // 已完成单据与库存流水绑定，禁止删除
        if ("2".equals(salesMapper.selectById(id).getStatus())) {
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