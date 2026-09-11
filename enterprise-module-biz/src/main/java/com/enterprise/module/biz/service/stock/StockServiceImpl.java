package com.enterprise.module.biz.service.stock;
import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.enterprise.module.biz.controller.admin.stock.vo.stock.*;
import com.enterprise.module.biz.dal.dataobject.stock.StockDO;
import com.enterprise.module.biz.dal.dataobject.stockmove.StockMoveDO;
import com.enterprise.module.biz.dal.mysql.stock.StockMapper;
import com.enterprise.module.biz.dal.mysql.stockmove.StockMoveMapper;
import com.enterprise.module.biz.dal.mysql.product.ProductMapper;
import com.enterprise.module.biz.service.support.BizReferenceService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import static com.enterprise.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.enterprise.module.biz.enums.ErrorCodeConstants.*;

@Service
public class StockServiceImpl implements StockService {
    @Resource private StockMapper stockMapper;
    @Resource private StockMoveMapper stockMoveMapper;
    @Resource private ProductMapper productMapper;
    @Resource private BizReferenceService references;
    @Override @Transactional(rollbackFor=Exception.class)
    public Long createStock(StockSaveReqVO req) {
        var product = references.product(req.getProductId(),req.getProductName());
        var warehouse = references.warehouse(req.getWarehouseId(),req.getWarehouse());
        productMapper.selectForUpdate(product.getId());
        if (stockMapper.selectByProductAndWarehouse(product.getId(),warehouse.getId()) != null) throw exception(STOCK_DUPLICATE);
        if (req.getQuantity() != null && req.getQuantity() < 0) throw exception(STOCK_NOT_ENOUGH);
        StockDO stock = StockDO.builder().productId(product.getId()).productName(product.getProductName())
            .warehouseId(warehouse.getId()).warehouse(warehouse.getName()).quantity(0L)
            .minQuantity(req.getMinQuantity() == null ? 0L : req.getMinQuantity()).build();
        stockMapper.insert(stock);
        if (req.getQuantity() != null && req.getQuantity() != 0) changeStock(product.getId(),warehouse.getId(),req.getQuantity(),"opening",String.valueOf(stock.getId()));
        return stock.getId();
    }
    @Override @Transactional(rollbackFor=Exception.class)
    public boolean changeStock(Long productId, Long warehouseId, Long delta, String type, String code) {
        if (delta == null || delta == Long.MIN_VALUE) throw exception(STOCK_NOT_ENOUGH);
        var product = productMapper.selectForUpdate(productId);
        if (product == null) throw exception(MASTER_REFERENCE_INVALID);
        var warehouse = references.warehouse(warehouseId,null);
        StockDO stock = stockMapper.selectByProductAndWarehouse(productId,warehouseId);
        if (stock == null) {
            stock = StockDO.builder().productId(productId).warehouseId(warehouseId)
                .productName(product.getProductName()).warehouse(warehouse.getName()).quantity(0L).minQuantity(0L).build();
            stockMapper.insert(stock);
        }
        if (stockMapper.adjustQuantity(stock.getId(),delta) == 0) return false;
        Long after = stockMapper.selectById(stock.getId()).getQuantity();
        stockMoveMapper.insert(StockMoveDO.builder().productId(productId).warehouseId(warehouseId)
            .productName(product.getProductName()).warehouse(warehouse.getName())
            .moveType(delta >= 0 ? "1" : "2").quantity(BigDecimal.valueOf(Math.abs(delta)))
            .balanceAfter(BigDecimal.valueOf(after)).sourceType(type).sourceCode(code).build());
        return true;
    }
    @Override public Long findQuantity(Long productId, Long warehouseId) {
        var stock = stockMapper.selectByProductAndWarehouse(productId,warehouseId);
        return stock == null ? 0L : stock.getQuantity();
    }
    @Override public java.util.List<StockDO> getLowStockList() {
        return stockMapper.selectList(new LambdaQueryWrapperX<StockDO>().apply("quantity <= min_quantity").orderByAsc(StockDO::getQuantity));
    }
    @Override @Transactional(rollbackFor=Exception.class)
    public void updateStock(StockSaveReqVO req) {
        var stock = stockMapper.selectById(req.getId());
        if (stock == null) throw exception(STOCK_NOT_EXISTS);
        productMapper.selectForUpdate(stock.getProductId());
        stock = stockMapper.selectByProductAndWarehouse(stock.getProductId(),stock.getWarehouseId());
        if (req.getProductId() != null && !req.getProductId().equals(stock.getProductId())
            || req.getWarehouseId() != null && !req.getWarehouseId().equals(stock.getWarehouseId())
            || req.getQuantity() != null && !req.getQuantity().equals(stock.getQuantity())) throw exception(STOCK_IDENTITY_LOCKED);
        StockDO update = new StockDO(); update.setId(req.getId()); update.setMinQuantity(req.getMinQuantity()); stockMapper.updateById(update);
    }
    @Override public void deleteStock(Long id) { throw exception(STOCK_IDENTITY_LOCKED); }
    @Override public StockDO getStock(Long id) { return stockMapper.selectById(id); }
    @Override public PageResult<StockDO> getStockPage(StockPageReqVO req) { return stockMapper.selectPage(req); }
}
