package com.enterprise.module.biz.dal.dataobject.wms;

import com.enterprise.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * WMS 拣货任务 DO（销售出库 → 从库位拣货下架）
 */
@TableName("biz_wms_pick_task")
@KeySequence("biz_wms_pick_task_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsPickTaskDO extends TenantBaseDO {

    @TableId
    private Long id;
    /** 销售单ID */
    private Long salesId;
    /** 销售单号 */
    private String salesCode;
    /** 产品ID */
    private Long productId;
    /** 产品名称（冗余） */
    private String productName;
    /** 仓库ID */
    private Long warehouseId;
    /** 仓库名称（冗余） */
    private String warehouseName;
    /** 出库数量 */
    private Long quantity;
    /** 已拣货累计 */
    private Long pickedQuantity;
    /** 状态（0待拣货 1已完成） */
    private Integer status;

}
