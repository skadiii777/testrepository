package com.enterprise.module.biz.dal.mysql.stock;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.mybatis.core.mapper.BaseMapperX;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.enterprise.module.biz.controller.admin.stock.vo.stock.StockPageReqVO;
import com.enterprise.module.biz.dal.dataobject.stock.StockDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
@Mapper
public interface StockMapper extends BaseMapperX<StockDO> {

    /**
     * 分页查询
     */
    default PageResult<StockDO> selectPage(StockPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<StockDO>()
                .likeIfPresent(StockDO::getProductName, reqVO.getProductName())
                .eqIfPresent(StockDO::getWarehouse, reqVO.getWarehouse())
                .betweenIfPresent(StockDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(StockDO::getId));
    }

    /**
     * 原子增减库存（delta 正数入库/负数出库），数量不足时更新 0 行
     */
    @Update("UPDATE biz_stock SET quantity = quantity + #{delta} "
            + "WHERE id = #{id} AND quantity + #{delta} >= 0 AND deleted = 0")
    int adjustQuantity(@Param("id") Long id, @Param("delta") Long delta);

    /**
     * 按产品+仓库精确查询
     */
    default StockDO selectByProductAndWarehouse(String productName, String warehouse) {
        return selectOne(new LambdaQueryWrapperX<StockDO>()
                .eq(StockDO::getProductName, productName)
                .eq(StockDO::getWarehouse, warehouse)
                .last("LIMIT 1"));
    }
}