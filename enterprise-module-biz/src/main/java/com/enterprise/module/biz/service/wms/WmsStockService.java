package com.enterprise.module.biz.service.wms;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.module.biz.controller.admin.wms.vo.wms.*;
import com.enterprise.module.biz.dal.dataobject.wms.WmsLocationStockDO;

import java.util.List;

public interface WmsStockService {
    PageResult<WmsLocationStockDO> getStockPage(WmsStockPageReqVO pageReqVO);
    List<WmsUnassignedRespVO> getUnassigned(Long warehouseId);
    void putaway(WmsOperateReqVO reqVO);
    void remove(WmsOperateReqVO reqVO);
    void move(WmsOperateReqVO reqVO);
    PageResult<WmsMoveRespVO> getMovePage(WmsMovePageReqVO pageReqVO);
}
