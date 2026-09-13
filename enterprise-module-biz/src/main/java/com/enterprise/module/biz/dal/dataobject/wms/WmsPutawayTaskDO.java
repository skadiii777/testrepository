package com.enterprise.module.biz.dal.dataobject.wms;

import com.enterprise.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * WMS 上架任务 DO（采购入库 → 上架到库位）
 */
@TableName("biz_wms_putaway_task")
@KeySequence("biz_wms_putaway_task_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsPutawayTaskDO extends TenantBaseDO {

    @TableId
    private Long id;
    /** 采购单ID */
    private Long purchaseId;
    /** 采购单号 */
    private String purchaseCode;
    /** 产品ID */
    private Long productId;
    /** 产品名称（冗余） */
    private String productName;
    /** 仓库ID */
    private Long warehouseId;
    /** 仓库名称（冗余） */
    private String warehouseName;
    /** 入库数量 */
    private Long quantity;
    /** 已上架累计 */
    private Long putawayQuantity;
    /** 状态（0待上架 1已完成） */
    private Integer status;

}
