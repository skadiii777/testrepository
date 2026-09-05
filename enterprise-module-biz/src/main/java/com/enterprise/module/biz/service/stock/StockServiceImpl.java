package com.enterprise.module.biz.service.stock;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.module.biz.controller.admin.stock.vo.stock.StockPageReqVO;
import com.enterprise.module.biz.controller.admin.stock.vo.stock.StockSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.stock.StockDO;
import com.enterprise.module.biz.dal.mysql.stock.StockMapper;
import com.enterprise.module.biz.enums.ErrorCodeConstants;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import com.enterprise.module.biz.dal.mysql.stockmove.StockMoveMapper; import com.enterprise.module.biz.dal.dataobject.stockmove.StockMoveDO;

import static com.enterprise.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.enterprise.module.biz.enums.ErrorCodeConstants.*;

/**
 * 库存 Service 实现类
 *
 * @author 企业管理平台
 */
@Service
@Validated
public class StockServiceImpl implements StockService {

    @Resource
    private StockMapper stockMapper;
    @Resource
    private StockMoveMapper stockMoveMapper;

    @Override
    public Long createStock(StockSaveReqVO createReqVO) {
        StockDO stock = BeanUtils.toBean(createReqVO, StockDO.class);
        stockMapper.insert(stock);
        return stock.getId();
    }


    /**
     * 库存变更并落流水（synchronized 单机串行 + SQL 条件兜底）
     */
    @Override
    public synchronized boolean changeStock(String productName, String warehouse, Long delta,
                                            String sourceType, String sourceCode) {
        String wh = warehouse == null ? "默认仓库" : warehouse;
        StockDO stock = stockMapper.selectByProductAndWarehouse(productName, wh);
        if (stock == null) {
            stock = StockDO.builder().productName(productName).warehouse(wh)
                    .quantity(0L).minQuantity(0L).build();
            stockMapper.insert(stock);
        }
        int rows = stockMapper.adjustQuantity(stock.getId(), delta);
        if (rows == 0) {
            return false;
        }
        Long balanceAfter = stockMapper.selectById(stock.getId()).getQuantity();
        StockMoveDO move = StockMoveDO.builder()
                .productName(productName).warehouse(wh)
                .moveType(delta >= 0 ? "1" : "2")
                .quantity(java.math.BigDecimal.valueOf(Math.abs(delta)))
                .balanceAfter(java.math.BigDecimal.valueOf(balanceAfter))
                .sourceType(sourceType == null ? "manual" : sourceType)
                .sourceCode(sourceCode == null ? "" : sourceCode)
                .build();
        stockMoveMapper.insert(move);
        return true;
    }

    @Override
    public Long findQuantity(String productName, String warehouse) {
        StockDO stock = stockMapper.selectByProductAndWarehouse(productName, warehouse);
        return stock == null || stock.getQuantity() == null ? 0L : stock.getQuantity();
    }

    @Override
    public java.util.List<StockDO> getLowStockList() {
        return stockMapper.selectList(new LambdaQueryWrapperX<StockDO>()
                .apply("quantity <= min_quantity")
                .orderByAsc(StockDO::getQuantity));
    }
    @Override
    public void updateStock(StockSaveReqVO updateReqVO) {
        validateStockExists(updateReqVO.getId());
        StockDO updateObj = BeanUtils.toBean(updateReqVO, StockDO.class);
        stockMapper.updateById(updateObj);
    }


    @Override
    public void deleteStock(Long id) {
        validateStockExists(id);
        stockMapper.deleteById(id);
    }

    private void validateStockExists(Long id) {
        if (stockMapper.selectById(id) == null) {
            throw exception(STOCK_NOT_EXISTS);
        }
    }

    @Override
    public StockDO getStock(Long id) {
        return stockMapper.selectById(id);
    }

    @Override
    public PageResult<StockDO> getStockPage(StockPageReqVO pageReqVO) {
        return stockMapper.selectPage(pageReqVO);
    }
}