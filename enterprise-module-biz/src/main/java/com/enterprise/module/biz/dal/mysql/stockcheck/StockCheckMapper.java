package com.enterprise.module.biz.dal.mysql.stockcheck;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.enterprise.framework.mybatis.core.mapper.BaseMapperX;
import com.enterprise.module.biz.controller.admin.stockcheck.vo.stockcheck.StockCheckPageReqVO;
import com.enterprise.module.biz.dal.dataobject.stockcheck.StockCheckDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存盘点单 Mapper
 *
 * @author 企业管理平台
 */
@Mapper
public interface StockCheckMapper extends BaseMapperX<StockCheckDO> {
    /** Current read; caller must hold a transaction. Tenant and logical-delete filters still apply. */
    default StockCheckDO selectForUpdate(Long id) {
        return selectOne(new LambdaQueryWrapperX<StockCheckDO>().eq(StockCheckDO::getId, id).last("FOR UPDATE"));
    }


    default PageResult<StockCheckDO> selectPage(StockCheckPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<StockCheckDO>()
                .eqIfPresent(StockCheckDO::getProductId, reqVO.getProductId())
                .eqIfPresent(StockCheckDO::getWarehouseId, reqVO.getWarehouseId())
                .likeIfPresent(StockCheckDO::getProductName, reqVO.getProductName())
                .eqIfPresent(StockCheckDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(StockCheckDO::getCheckDate, reqVO.getCheckDateRange())
                .orderByDesc(StockCheckDO::getId));
    }

}
