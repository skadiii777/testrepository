package com.enterprise.module.biz.service.stockmove;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.module.biz.controller.admin.stockmove.vo.stockmove.StockMovePageReqVO;
import com.enterprise.module.biz.dal.dataobject.stockmove.StockMoveDO;
import com.enterprise.module.biz.dal.mysql.stockmove.StockMoveMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 库存流水 Service 实现类（只增不改）
 *
 * @author 企业管理平台
 */
@Service
@Validated
public class StockMoveServiceImpl implements StockMoveService {

    @Resource
    private StockMoveMapper stockMoveMapper;

    @Override
    public void record(StockMoveDO move) {
        stockMoveMapper.insert(move);
    }

    @Override
    public StockMoveDO getStockMove(Long id) {
        return stockMoveMapper.selectById(id);
    }

    @Override
    public PageResult<StockMoveDO> getStockMovePage(StockMovePageReqVO pageReqVO) {
        return stockMoveMapper.selectPage(pageReqVO);
    }
}