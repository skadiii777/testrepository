package com.enterprise.module.biz.service.wms;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.module.biz.controller.admin.wms.vo.wms.*;

public interface WmsLocationService {
    Long createLocation(WmsLocationSaveReqVO reqVO);
    void updateLocation(WmsLocationSaveReqVO reqVO);
    void deleteLocation(Long id);
    WmsLocationRespVO getLocation(Long id);
    PageResult<WmsLocationRespVO> getLocationPage(WmsLocationPageReqVO pageReqVO);
    java.util.List<WmsLocationRespVO> getSimpleLocationList(Long warehouseId);
}
