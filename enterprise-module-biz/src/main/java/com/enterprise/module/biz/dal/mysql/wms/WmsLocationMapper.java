package com.enterprise.module.biz.dal.mysql.wms;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.mybatis.core.mapper.BaseMapperX;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.enterprise.module.biz.controller.admin.wms.vo.wms.WmsLocationPageReqVO;
import com.enterprise.module.biz.dal.dataobject.wms.WmsLocationDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WmsLocationMapper extends BaseMapperX<WmsLocationDO> {

    default PageResult<WmsLocationDO> selectPage(WmsLocationPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsLocationDO>()
                .eqIfPresent(WmsLocationDO::getWarehouseId, reqVO.getWarehouseId())
                .likeIfPresent(WmsLocationDO::getCode, reqVO.getCode())
                .likeIfPresent(WmsLocationDO::getName, reqVO.getName())
                .eqIfPresent(WmsLocationDO::getType, reqVO.getType())
                .eqIfPresent(WmsLocationDO::getStatus, reqVO.getStatus())
                .orderByAsc(WmsLocationDO::getWarehouseId)
                .orderByAsc(WmsLocationDO::getCode));
    }

    default WmsLocationDO selectByWarehouseAndCode(Long warehouseId, String code) {
        return selectOne(new LambdaQueryWrapperX<WmsLocationDO>()
                .eq(WmsLocationDO::getWarehouseId, warehouseId)
                .eq(WmsLocationDO::getCode, code));
    }

    /** 启用库位精简列表（库存动作下拉用，可按仓库过滤） */
    default List<WmsLocationDO> selectSimpleList(Long warehouseId) {
        return selectList(new LambdaQueryWrapperX<WmsLocationDO>()
                .eq(WmsLocationDO::getStatus, 0)
                .eqIfPresent(WmsLocationDO::getWarehouseId, warehouseId)
                .orderByAsc(WmsLocationDO::getCode));
    }

}
