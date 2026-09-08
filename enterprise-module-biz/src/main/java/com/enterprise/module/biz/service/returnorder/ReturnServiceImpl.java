package com.enterprise.module.biz.service.returnorder;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.module.biz.controller.admin.returnorder.vo.returnorder.ReturnPageReqVO;
import com.enterprise.module.biz.controller.admin.returnorder.vo.returnorder.ReturnSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.purchase.PurchaseDO;
import com.enterprise.module.biz.dal.dataobject.returnorder.ReturnDO;
import com.enterprise.module.biz.dal.dataobject.sales.SalesDO;
import com.enterprise.module.biz.dal.mysql.purchase.PurchaseMapper;
import com.enterprise.module.biz.dal.mysql.returnorder.ReturnMapper;
import com.enterprise.module.biz.dal.mysql.sales.SalesMapper;
import com.enterprise.module.biz.enums.ErrorCodeConstants;
import com.enterprise.module.biz.service.stock.StockService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.enterprise.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.enterprise.module.biz.enums.ErrorCodeConstants.*;

/**
 * 退货单 Service 实现类
 *
 * 销售退货（客户退回）= 货物入库；采购退货（退回供应商）= 货物出库。
 * 累计退货数量（不含已作废）不可超过原单数量。
 *
 * @author 企业管理平台
 */
@Slf4j
@Service
@Validated
public class ReturnServiceImpl implements ReturnService {

    /** 单据状态：已完成 */
    private static final String ORDER_STATUS_COMPLETED = "2";
    /** 退货单状态：待退货/已退货/已作废 */
    private static final String RETURN_STATUS_PENDING = "0";
    private static final String RETURN_STATUS_RETURNED = "1";
    private static final String RETURN_STATUS_VOID = "3";
    /** 退货类型：1=销售退货 2=采购退货 */
    private static final String RETURN_TYPE_SALES = "1";
    private static final String RETURN_TYPE_PURCHASE = "2";

    @Resource
    private ReturnMapper returnMapper;
    @Resource
    private SalesMapper salesMapper;
    @Resource
    private PurchaseMapper purchaseMapper;
    @Resource
    private StockService stockService;

    @Override
    public Long createReturn(ReturnSaveReqVO createReqVO) {
        // 1.1 校验退货类型并加载原单（必须已完成：库存联动在"完成"流转）
        if (!RETURN_TYPE_SALES.equals(createReqVO.getReturnType())
                && !RETURN_TYPE_PURCHASE.equals(createReqVO.getReturnType())) {
            throw exception(RETURN_TYPE_INVALID);
        }
        ReturnDO ret = BeanUtils.toBean(createReqVO, ReturnDO.class);
        if (RETURN_TYPE_SALES.equals(createReqVO.getReturnType())) {
            SalesDO sales = salesMapper.selectById(createReqVO.getOrderId());
            if (sales == null) {
                throw exception(RETURN_ORDER_NOT_EXISTS);
            }
            validateOrderCompleted(sales.getStatus());
            ret.setOrderCode(sales.getSalesCode());
            ret.setPartyName(sales.getCustomerName());
            ret.setProductName(sales.getProductName());
            BigDecimal orderPrice = sales.getPrice() != null ? sales.getPrice() : BigDecimal.ZERO;
            ret.setPrice(createReqVO.getPrice() != null ? createReqVO.getPrice() : orderPrice);
            validateQuantity(createReqVO.getReturnType(), createReqVO.getOrderId(), sales.getQuantity(),
                    createReqVO.getQuantity(), null);
        } else {
            PurchaseDO purchase = purchaseMapper.selectById(createReqVO.getOrderId());
            if (purchase == null) {
                throw exception(RETURN_ORDER_NOT_EXISTS);
            }
            validateOrderCompleted(purchase.getStatus());
            ret.setOrderCode(purchase.getPurchaseCode());
            ret.setPartyName(purchase.getSupplierName());
            ret.setProductName(purchase.getProductName());
            BigDecimal orderPrice = purchase.getPrice() != null ? purchase.getPrice() : BigDecimal.ZERO;
            ret.setPrice(createReqVO.getPrice() != null ? createReqVO.getPrice() : orderPrice);
            validateQuantity(createReqVO.getReturnType(), createReqVO.getOrderId(), purchase.getQuantity(),
                    createReqVO.getQuantity(), null);
        }
        // 1.2 补全默认值
        if (ret.getWarehouse() == null || ret.getWarehouse().isEmpty()) {
            ret.setWarehouse("默认仓库");
        }
        ret.setTotalAmount(BigDecimal.valueOf(ret.getQuantity()).multiply(ret.getPrice()));
        ret.setStatus(RETURN_STATUS_PENDING);
        ret.setReturnNo(generateReturnNo(createReqVO.getReturnType()));
        returnMapper.insert(ret);
        return ret.getId();
    }

    @Override
    public void updateReturn(ReturnSaveReqVO updateReqVO) {
        ReturnDO exists = validateReturnExists(updateReqVO.getId());
        if (!RETURN_STATUS_PENDING.equals(exists.getStatus())) {
            throw exception(RETURN_STATUS_INVALID);
        }
        // 数量变化时重新校验累计退货（排除自身）
        if (updateReqVO.getQuantity() != null
                && !updateReqVO.getQuantity().equals(exists.getQuantity())) {
            Long orderQuantity = loadOrderQuantity(exists.getReturnType(), exists.getOrderId());
            validateQuantity(exists.getReturnType(), exists.getOrderId(), orderQuantity,
                    updateReqVO.getQuantity(), exists.getId());
        }
        ReturnDO updateObj = BeanUtils.toBean(updateReqVO, ReturnDO.class);
        // 关联单据与类型不可变更；总额按 数量 × 单价 重算
        updateObj.setReturnType(null);
        updateObj.setOrderId(null);
        if (updateObj.getPrice() == null) {
            updateObj.setPrice(exists.getPrice());
        }
        if (updateObj.getQuantity() != null) {
            updateObj.setTotalAmount(BigDecimal.valueOf(updateObj.getQuantity()).multiply(updateObj.getPrice()));
        } else {
            updateObj.setTotalAmount(null);
        }
        returnMapper.updateById(updateObj);
    }

    @Override
    public void deleteReturn(Long id) {
        validateReturnExists(id);
        returnMapper.deleteById(id);
    }

    @Override
    public ReturnDO getReturn(Long id) {
        return returnMapper.selectById(id);
    }

    @Override
    public PageResult<ReturnDO> getReturnPage(ReturnPageReqVO pageReqVO) {
        return returnMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeReturn(Long id) {
        ReturnDO ret = validateReturnExists(id);
        if (!RETURN_STATUS_PENDING.equals(ret.getStatus())) {
            throw exception(RETURN_STATUS_INVALID);
        }
        // 销售退货入库（+），采购退货出库（-）；出库不足时抛异常回滚
        long delta = RETURN_TYPE_SALES.equals(ret.getReturnType()) ? ret.getQuantity() : -ret.getQuantity();
        boolean ok = stockService.changeStock(ret.getProductName(), ret.getWarehouse(), delta,
                RETURN_TYPE_SALES.equals(ret.getReturnType()) ? "sales_return" : "purchase_return",
                ret.getReturnNo());
        if (!ok) {
            throw exception(STOCK_NOT_ENOUGH);
        }
        ReturnDO update = new ReturnDO();
        update.setId(id);
        update.setStatus(RETURN_STATUS_RETURNED);
        returnMapper.updateById(update);
    }

    @Override
    public void voidReturn(Long id) {
        ReturnDO ret = validateReturnExists(id);
        if (!RETURN_STATUS_PENDING.equals(ret.getStatus())) {
            throw exception(RETURN_STATUS_INVALID);
        }
        ReturnDO update = new ReturnDO();
        update.setId(id);
        update.setStatus(RETURN_STATUS_VOID);
        returnMapper.updateById(update);
    }

    @Override
    public Long getReturnedSumByOrder(String returnType, Long orderId) {
        return returnMapper.selectReturnedSumByOrder(returnType, orderId);
    }

    private void validateOrderCompleted(String status) {
        if (!ORDER_STATUS_COMPLETED.equals(status)) {
            throw exception(RETURN_ORDER_NOT_COMPLETED);
        }
    }

    /**
     * 校验累计退货数量：已退（不含已作废，可排除自身）+ 本次 ≤ 原单数量
     */
    private void validateQuantity(String returnType, Long orderId, Long orderQuantity,
                                  Long quantity, Long excludeId) {
        if (quantity == null || quantity <= 0) {
            throw exception(ErrorCodeConstants.RETURN_QTY_INVALID);
        }
        long returnedSum = returnMapper.selectValidListByOrder(returnType, orderId).stream()
                .filter(r -> !r.getId().equals(excludeId))
                .mapToLong(ReturnDO::getQuantity)
                .sum();
        if (orderQuantity == null || returnedSum + quantity > orderQuantity) {
            throw exception(RETURN_QTY_EXCEED,
                    String.valueOf(returnedSum), String.valueOf(orderQuantity));
        }
    }

    /**
     * 加载原单数量（更新时重新校验用）
     */
    private Long loadOrderQuantity(String returnType, Long orderId) {
        if (RETURN_TYPE_SALES.equals(returnType)) {
            SalesDO sales = salesMapper.selectById(orderId);
            return sales != null ? sales.getQuantity() : null;
        }
        PurchaseDO purchase = purchaseMapper.selectById(orderId);
        return purchase != null ? purchase.getQuantity() : null;
    }

    private ReturnDO validateReturnExists(Long id) {
        ReturnDO ret = returnMapper.selectById(id);
        if (ret == null) {
            throw exception(RETURN_NOT_EXISTS);
        }
        return ret;
    }

    /**
     * 生成退货单号：SR/PR + yyyyMMddHHmmss（销售退货=SR，采购退货=PR）
     */
    private String generateReturnNo(String returnType) {
        return (RETURN_TYPE_SALES.equals(returnType) ? "SR" : "PR")
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

}
