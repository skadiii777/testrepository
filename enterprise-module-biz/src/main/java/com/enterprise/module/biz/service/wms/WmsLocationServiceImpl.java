package com.enterprise.module.biz.service.wms;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.module.biz.controller.admin.wms.vo.wms.*;
import com.enterprise.module.biz.dal.dataobject.wms.WmsLocationDO;
import com.enterprise.module.biz.dal.dataobject.wms.WmsLocationStockDO;
import com.enterprise.module.biz.dal.mysql.wms.WmsLocationMapper;
import com.enterprise.module.biz.dal.mysql.wms.WmsLocationStockMapper;
import com.enterprise.module.biz.enums.ErrorCodeConstants;
import com.enterprise.module.biz.service.support.BizReferenceService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static com.enterprise.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
@Validated
public class WmsLocationServiceImpl implements WmsLocationService {

    @Resource
    private WmsLocationMapper locationMapper;
    @Resource
    private WmsLocationStockMapper locationStockMapper;
    @Resource
    private BizReferenceService references;

    @Override
    public Long createLocation(WmsLocationSaveReqVO reqVO) {
        var warehouse = references.warehouse(reqVO.getWarehouseId(), null);
        validateCodeUnique(reqVO.getWarehouseId(), reqVO.getCode(), null);
        WmsLocationDO location = BeanUtils.toBean(reqVO, WmsLocationDO.class);
        location.setWarehouseName(warehouse.getName());
        if (location.getStatus() == null) {
            location.setStatus(0);
        }
        locationMapper.insert(location);
        return location.getId();
    }

    @Override
    public void updateLocation(WmsLocationSaveReqVO reqVO) {
        WmsLocationDO exists = validateLocationExists(reqVO.getId());
        validateCodeUnique(reqVO.getWarehouseId(), reqVO.getCode(), reqVO.getId());
        var warehouse = references.warehouse(reqVO.getWarehouseId(), null);
        WmsLocationDO update = BeanUtils.toBean(reqVO, WmsLocationDO.class);
        update.setWarehouseName(warehouse.getName());
        locationMapper.updateById(update);
    }

    @Override
    public void deleteLocation(Long id) {
        validateLocationExists(id);
        if (locationStockMapper.selectCount(new LambdaQueryWrapper<WmsLocationStockDO>()
                .eq(WmsLocationStockDO::getLocationId, id)
                .gt(WmsLocationStockDO::getQuantity, 0)) > 0) {
            throw exception(ErrorCodeConstants.WMS_LOCATION_HAS_STOCK);
        }
        locationMapper.deleteById(id);
    }

    @Override
    public WmsLocationRespVO getLocation(Long id) {
        return BeanUtils.toBean(validateLocationExists(id), WmsLocationRespVO.class);
    }

    @Override
    public PageResult<WmsLocationRespVO> getLocationPage(WmsLocationPageReqVO pageReqVO) {
        PageResult<WmsLocationDO> page = locationMapper.selectPage(pageReqVO);
        return BeanUtils.toBean(page, WmsLocationRespVO.class);
    }

    @Override
    public List<WmsLocationRespVO> getSimpleLocationList(Long warehouseId) {
        return BeanUtils.toBean(locationMapper.selectSimpleList(warehouseId), WmsLocationRespVO.class);
    }

    private WmsLocationDO validateLocationExists(Long id) {
        WmsLocationDO location = locationMapper.selectById(id);
        if (location == null) {
            throw exception(ErrorCodeConstants.WMS_LOCATION_NOT_EXISTS);
        }
        return location;
    }

    private void validateCodeUnique(Long warehouseId, String code, Long excludeId) {
        WmsLocationDO exists = locationMapper.selectByWarehouseAndCode(warehouseId, code);
        if (exists != null && (excludeId == null || !exists.getId().equals(excludeId))) {
            throw exception(ErrorCodeConstants.WMS_LOCATION_DUPLICATE);
        }
    }
}
