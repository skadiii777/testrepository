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

    default PageResult<StockCheckDO> selectPage(StockCheckPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<StockCheckDO>()
                .likeIfPresent(StockCheckDO::getProductName, reqVO.getProductName())
                .eqIfPresent(StockCheckDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(StockCheckDO::getCheckDate, reqVO.getCheckDateRange())
                .orderByDesc(StockCheckDO::getId));
    }

}
