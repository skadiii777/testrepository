package com.enterprise.module.biz.service.stockcheck;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.module.biz.controller.admin.stockcheck.vo.stockcheck.StockCheckCreateReqVO;
import com.enterprise.module.biz.controller.admin.stockcheck.vo.stockcheck.StockCheckPageReqVO;
import com.enterprise.module.biz.dal.dataobject.stockcheck.StockCheckDO;

/**
 * 库存盘点 Service 接口
 *
 * @author 企业管理平台
 */
public interface StockCheckService {

    /**
     * 创建盘点单（快照当前账面数量，不影响库存）
     *
     * @param createReqVO 盘点信息
     * @return 盘点单编号
     */
    Long createStockCheck(StockCheckCreateReqVO createReqVO);

    /**
     * 确认盘点：按实盘调整库存并写库存流水（确认时重新快照账面，防创建后库存变动）
     *
     * @param id 盘点单编号
     */
    void confirmStockCheck(Long id);

    /**
     * 删除盘点单（仅待确认状态可删除）
     *
     * @param id 盘点单编号
     */
    void deleteStockCheck(Long id);

    /**
     * 获得盘点单分页
     *
     * @param pageReqVO 分页查询
     * @return 分页结果
     */
    PageResult<StockCheckDO> getStockCheckPage(StockCheckPageReqVO pageReqVO);

}
