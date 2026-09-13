package com.enterprise.module.biz.dal.dataobject.wms;

import com.enterprise.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * WMS 库位流水 DO（只增不改）
 */
@TableName("biz_wms_location_move")
@KeySequence("biz_wms_location_move_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsLocationMoveDO extends TenantBaseDO {

    @TableId
    private Long id;
    /** 动作类型（putaway上架 remove下架 move移库） */
    private String moveType;
    /** 仓库ID */
    private Long warehouseId;
    /** 产品ID */
    private Long productId;
    /** 产品名称（冗余） */
    private String productName;
    /** 数量（正数） */
    private Long quantity;
    /** 源库位ID（上架时为空=未分配区） */
    private Long fromLocationId;
    /** 源库位编码 */
    private String fromLocationCode;
    /** 目标库位ID（下架时为空=未分配区） */
    private Long toLocationId;
    /** 目标库位编码 */
    private String toLocationCode;
    /** 操作人 */
    private String operatorName;
    /** 备注 */
    private String remark;

}
