package com.enterprise.module.biz.dal.mysql.wms;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.mybatis.core.mapper.BaseMapperX;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.enterprise.module.biz.controller.admin.wms.vo.wms.WmsMovePageReqVO;
import com.enterprise.module.biz.dal.dataobject.wms.WmsLocationMoveDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WmsLocationMoveMapper extends BaseMapperX<WmsLocationMoveDO> {

    default PageResult<WmsLocationMoveDO> selectPage(WmsMovePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsLocationMoveDO>()
                .eqIfPresent(WmsLocationMoveDO::getWarehouseId, reqVO.getWarehouseId())
                .likeIfPresent(WmsLocationMoveDO::getProductName, reqVO.getProductName())
                .eqIfPresent(WmsLocationMoveDO::getMoveType, reqVO.getMoveType())
                .orderByDesc(WmsLocationMoveDO::getId));
    }

}
