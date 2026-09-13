package com.enterprise.module.biz.dal.mysql.wms;

import com.enterprise.framework.mybatis.core.mapper.BaseMapperX;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.enterprise.module.biz.dal.dataobject.wms.WmsPickTaskDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface WmsPickTaskMapper extends BaseMapperX<WmsPickTaskDO> {

    default WmsPickTaskDO selectBySales(Long salesId) {
        return selectOne(new LambdaQueryWrapperX<WmsPickTaskDO>()
                .eq(WmsPickTaskDO::getSalesId, salesId));
    }

    default List<WmsPickTaskDO> selectOpenTasks(Long warehouseId, Long productId) {
        return selectList(new LambdaQueryWrapperX<WmsPickTaskDO>()
                .eq(WmsPickTaskDO::getStatus, 0)
                .eq(WmsPickTaskDO::getWarehouseId, warehouseId)
                .eq(WmsPickTaskDO::getProductId, productId)
                .orderByAsc(WmsPickTaskDO::getId));
    }

    @Update("UPDATE biz_wms_pick_task SET picked_quantity = picked_quantity + #{take} "
            + "WHERE id = #{id} AND picked_quantity + #{take} <= quantity")
    int addProgress(@Param("id") Long id, @Param("take") Long take);

    @Update("UPDATE biz_wms_pick_task SET status = 1 "
            + "WHERE id = #{id} AND picked_quantity >= quantity AND status = 0")
    int completeIfDone(@Param("id") Long id);

}
