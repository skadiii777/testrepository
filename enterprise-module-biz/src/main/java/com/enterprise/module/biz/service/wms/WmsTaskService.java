package com.enterprise.module.biz.service.wms;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.module.biz.controller.admin.wms.vo.wms.WmsTaskPageReqVO;
import com.enterprise.module.biz.controller.admin.wms.vo.wms.WmsTaskRespVO;
import com.enterprise.module.biz.dal.dataobject.sales.SalesDO;
import com.enterprise.module.biz.dal.dataobject.purchase.PurchaseDO;

public interface WmsTaskService {
    PageResult<WmsTaskRespVO> getTaskPage(String type, WmsTaskPageReqVO reqVO);

    /** 采购单完成入库后调用：生成上架任务（同单幂等） */
    void createPutawayTask(PurchaseDO purchase);

    /** 销售单完成出库后调用：生成拣货任务（同单幂等） */
    void createPickTask(SalesDO sales);

    /** 上架成功后按 FIFO 消耗上架任务（部分上架支持，同事务） */
    void consumePutaway(Long warehouseId, Long productId, Long quantity);

    /** 下架成功后按 FIFO 消耗拣货任务（部分拣货支持，同事务） */
    void consumePick(Long warehouseId, Long productId, Long quantity);
}
