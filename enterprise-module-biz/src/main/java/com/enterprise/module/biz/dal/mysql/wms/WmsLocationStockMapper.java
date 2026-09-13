package com.enterprise.module.biz.dal.mysql.wms;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.mybatis.core.mapper.BaseMapperX;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.enterprise.module.biz.controller.admin.wms.vo.wms.WmsStockPageReqVO;
import com.enterprise.module.biz.dal.dataobject.wms.WmsLocationStockDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface WmsLocationStockMapper extends BaseMapperX<WmsLocationStockDO> {

    default PageResult<WmsLocationStockDO> selectPage(WmsStockPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsLocationStockDO>()
                .eqIfPresent(WmsLocationStockDO::getWarehouseId, reqVO.getWarehouseId())
                .eqIfPresent(WmsLocationStockDO::getLocationId, reqVO.getLocationId())
                .likeIfPresent(WmsLocationStockDO::getProductName, reqVO.getProductName())
                .gt(WmsLocationStockDO::getQuantity, 0)
                .orderByAsc(WmsLocationStockDO::getWarehouseId)
                .orderByAsc(WmsLocationStockDO::getLocationCode));
    }

    default WmsLocationStockDO selectByLocationAndProduct(Long locationId, Long productId) {
        return selectOne(new LambdaQueryWrapperX<WmsLocationStockDO>()
                .eq(WmsLocationStockDO::getLocationId, locationId)
                .eq(WmsLocationStockDO::getProductId, productId));
    }

    /** 某仓库下某产品的库位合计（分配量） */
    default Long sumQuantityByWarehouseAndProduct(Long warehouseId, Long productId) {
        List<WmsLocationStockDO> list = selectList(new LambdaQueryWrapperX<WmsLocationStockDO>()
                .eq(WmsLocationStockDO::getWarehouseId, warehouseId)
                .eq(WmsLocationStockDO::getProductId, productId));
        return list.stream().mapToLong(WmsLocationStockDO::getQuantity).sum();
    }

    /** 数量调整（delta 可负）；库存不足时影响 0 行，调用方必须检查 */
    @Update("UPDATE biz_wms_location_stock SET quantity = quantity + #{delta} WHERE id = #{id} AND quantity + #{delta} >= 0")
    int adjustQuantity(@Param("id") Long id, @Param("delta") Long delta);

}
