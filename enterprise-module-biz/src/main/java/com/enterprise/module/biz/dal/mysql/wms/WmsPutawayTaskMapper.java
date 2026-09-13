package com.enterprise.module.biz.dal.mysql.wms;

import com.enterprise.framework.mybatis.core.mapper.BaseMapperX;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.enterprise.module.biz.dal.dataobject.wms.WmsPutawayTaskDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface WmsPutawayTaskMapper extends BaseMapperX<WmsPutawayTaskDO> {

    default WmsPutawayTaskDO selectByPurchase(Long purchaseId) {
        return selectOne(new LambdaQueryWrapperX<WmsPutawayTaskDO>()
                .eq(WmsPutawayTaskDO::getPurchaseId, purchaseId));
    }

    default List<WmsPutawayTaskDO> selectOpenTasks(Long warehouseId, Long productId) {
        return selectList(new LambdaQueryWrapperX<WmsPutawayTaskDO>()
                .eq(WmsPutawayTaskDO::getStatus, 0)
                .eq(WmsPutawayTaskDO::getWarehouseId, warehouseId)
                .eq(WmsPutawayTaskDO::getProductId, productId)
                .orderByAsc(WmsPutawayTaskDO::getId));
    }

    /** 累加进度（守卫不超任务总量，返回 0 表示超量） */
    @Update("UPDATE biz_wms_putaway_task SET putaway_quantity = putaway_quantity + #{take} "
            + "WHERE id = #{id} AND putaway_quantity + #{take} <= quantity")
    int addProgress(@Param("id") Long id, @Param("take") Long take);

    @Update("UPDATE biz_wms_putaway_task SET status = 1 "
            + "WHERE id = #{id} AND putaway_quantity >= quantity AND status = 0")
    int completeIfDone(@Param("id") Long id);

}
