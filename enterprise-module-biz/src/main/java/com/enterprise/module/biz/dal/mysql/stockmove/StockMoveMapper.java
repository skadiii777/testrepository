package com.enterprise.module.biz.dal.mysql.stockmove;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.mybatis.core.mapper.BaseMapperX;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.enterprise.module.biz.controller.admin.stockmove.vo.stockmove.StockMovePageReqVO;
import com.enterprise.module.biz.dal.dataobject.stockmove.StockMoveDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StockMoveMapper extends BaseMapperX<StockMoveDO> {

    /**
     * 分页查询
     */
    default PageResult<StockMoveDO> selectPage(StockMovePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<StockMoveDO>()
                .likeIfPresent(StockMoveDO::getProductName, reqVO.getProductName())
                .eqIfPresent(StockMoveDO::getWarehouse, reqVO.getWarehouse())
                .eqIfPresent(StockMoveDO::getMoveType, reqVO.getMoveType())
                .eqIfPresent(StockMoveDO::getSourceType, reqVO.getSourceType())
                .likeIfPresent(StockMoveDO::getSourceCode, reqVO.getSourceCode())
                .betweenIfPresent(StockMoveDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(StockMoveDO::getId));
    }

}